package com.jdqm.tapelibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;


/**
 * Created by Jdqm on 2017-10-16.
 */

public class TapeView extends View {

    private static final String TAG = "TapeView";

    private int bgColor = Color.parseColor("#FBE40C");

    private int calibrationColor = Color.WHITE;

    private int textColor = Color.WHITE;

    private int triangleColor = Color.WHITE;

    private float textSize = 14.0f; //sp

    private float textY;

    //刻度线的宽度
    private float calibrationWidth = 1.0f; //dp

    //短的刻度线的高度
    private float calibrationShort = 20; //dp

    //长的刻度线的高度
    private float calibrationLong = 35; //dp

    private float triangleHeight = 18.0f; //dp

    //当前View的宽度
    private int width;

    //宽度的中间值
    private int middle;

    //刻度尺最小值
    private float minValue = 0;

    //最大值
    private float maxValue = 100;

    //刻度尺当前值
    private float value = 0;

    //每一格代表的值
    private float per = 1;

    //两条长的刻度线之间的 per 数量
    private int perCount = 10;

    //当前刻度与最小值的距离 (minValue-value)/per*gapWidth
    private float offset;

    //当前刻度与最新值的最大距离 (minValue-maxValue)/per*gapWidth
    private float maxOffset;

    //两个刻度之间的距离
    private float gapWidth = 10.0f; //dp

    //总的刻度数量
    private int totalCalibration;

    private float lastX;

    //被认为是快速滑动的最小速度
    private float minFlingVelocity;

    private Scroller scroller;

    private float dx;

    private Paint paint;

    //速度追踪器
    private VelocityTracker velocityTracker;

    private OnValueChangeListener onValueChangeListener;

    private Context mContext;

    /**
     * 回调接口
     */
    public interface OnValueChangeListener {
        void onChange(float value);
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }

    public TapeView(Context context) {
        super(context, null);
    }

    public TapeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TapeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttrs(context, attrs);
        init(context);
        calculateAttr();
    }

    private void init(Context context) {
        minFlingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        scroller = new Scroller(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
    }

    private void calculateAttr() {
        textY = calibrationLong + DisplayUtil.dp2px(30, mContext);
        offset = (value - minValue) * 10.0f / per * gapWidth;
        maxOffset = (maxValue - minValue) * 10.0f / per * gapWidth;
        totalCalibration = (int) ((maxValue - minValue) * 10.0f / per + 1);
    }

    /**
     * 读取布局文件中的自定义属性
     *
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TapeView);
        bgColor = ta.getColor(R.styleable.TapeView_bgColor, bgColor);
        calibrationColor = ta.getColor(R.styleable.TapeView_calibrationColor, calibrationColor);
        calibrationWidth = ta.getDimension(R.styleable.TapeView_calibrationWidth, DisplayUtil.dp2px(calibrationWidth, context));
        calibrationLong = ta.getDimension(R.styleable.TapeView_calibrationLong, DisplayUtil.dp2px(calibrationLong, context));
        calibrationShort = ta.getDimension(R.styleable.TapeView_calibrationShort, DisplayUtil.dp2px(calibrationShort, context));
        triangleColor = ta.getColor(R.styleable.TapeView_triangleColor, triangleColor);
        triangleHeight = ta.getDimension(R.styleable.TapeView_triangleHeight, DisplayUtil.dp2px(triangleHeight, context));
        textColor = ta.getColor(R.styleable.TapeView_textColor, textColor);
        textSize = ta.getDimension(R.styleable.TapeView_textSize, DisplayUtil.sp2px(textSize, context));
        per = ta.getFloat(R.styleable.TapeView_per, per);
        per *= 10.0f;
        perCount = ta.getInt(R.styleable.TapeView_perCount, perCount);
        gapWidth = ta.getDimension(R.styleable.TapeView_gapWidth, DisplayUtil.dp2px(gapWidth, context));
        minValue = ta.getFloat(R.styleable.TapeView_minValue, minValue);
        maxValue = ta.getFloat(R.styleable.TapeView_maxValue, maxValue);
        value = ta.getFloat(R.styleable.TapeView_value, value);
        ta.recycle();
    }


    /**
     * 初始化配置参数
     *
     * @param value 当前值
     * @param minValue 最小值
     * @param maxValue 最大值
     * @param per 每一隔所代表的值
     * @param perCount 相邻两条长刻度线之间被分成的隔数量
     */
    public void setValue(float value, float minValue, float maxValue, float per, int perCount) {
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;

        //浮点数在计算容易丢失精度，放大10倍
        this.per = per * 10.0f;
        this.perCount = perCount;
        calculateAttr();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        middle = width / 2;
        int mode = MeasureSpec.getMode(heightMeasureSpec);

        //当在布局文件设置高度为wrap_content时，默认为80dp(如果不处理效果和math_parent效果一样)，宽度就不处理了
        if (mode == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) DisplayUtil.dp2px(80, mContext), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(bgColor);
        drawCalibration(canvas);
        drawTriangle(canvas);
    }

    /**
     * 由于没有画三角形的api，这里通过ath来构造
     *
     * @param canvas
     */
    private void drawTriangle(Canvas canvas) {
        paint.setColor(triangleColor);
        Path path = new Path();
        path.moveTo(getWidth() / 2 - triangleHeight / 2, 0);
        path.lineTo(getWidth() / 2, triangleHeight / 2);
        path.lineTo(getWidth() / 2 + triangleHeight / 2, 0);
        path.close();
        canvas.drawPath(path, paint);
    }

    /**
     * 在画的时候首先找到第一根刻度画的x坐标，接着加上gapWidth接着画下一根，当x超出当前View的宽度则停止
     *
     * @param canvas
     */
    private void drawCalibration(Canvas canvas) {

        //当前画的刻度的位置
        float currentCalibration;
        float height;
        String value;

        //计算出左边第一个刻度，直接跳过前面不需要画的可读
        int distance = (int) (middle - offset);
        int left = 0;
        if (distance < 0) {
            left = (int) (-distance / gapWidth);
        }
        currentCalibration = middle - offset + left * gapWidth;
        while (currentCalibration < width * 10 && left < totalCalibration) {

            //边缘的一根刻度不画
            if (currentCalibration == 0) {
                left++;
                currentCalibration = middle - offset + left * gapWidth;
                continue;
            }
            if (left % perCount == 0) {
                //长的刻度宽度是短的两倍
                paint.setStrokeWidth(calibrationWidth * 2);
                height = calibrationLong;

                value = String.valueOf(minValue + left * per / 10.0f);
                if (value.endsWith(".0")) {
                    value = value.substring(0, value.length()-2);
                }
                paint.setColor(textColor);
                canvas.drawText(value, currentCalibration - paint.measureText(value) / 2, textY, paint);
            } else {
                paint.setStrokeWidth(calibrationWidth);
                height = calibrationShort;
            }

            paint.setColor(calibrationColor);
            canvas.drawLine(currentCalibration, 0, currentCalibration, height, paint);

            left++;
            currentCalibration = middle - offset + left * gapWidth;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                scroller.forceFinished(true);
                lastX = x;
                dx = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: " + x);
                dx = lastX - x;
                validateValue();
                break;
            case MotionEvent.ACTION_UP:
                smoothMoveToCalibration();
                calculateVelocity();
                return false;
            default:
                return false;

        }
        lastX = x;
        return true;
    }

    /**
     * 滑动结束后，若是指针在2条刻度之间时，需要让指针指向最近的可读
     */
    private void smoothMoveToCalibration() {
        offset += dx;
        if (offset < 0) {
            offset = 0;
        } else if (offset > maxOffset) {
            offset = maxOffset;
        }
        lastX = 0;
        dx = 0;
        value = minValue + Math.round(Math.abs(offset) / gapWidth) * per / 10.0f;
        offset = (value - minValue) * 10.0f / per * gapWidth;
        if (onValueChangeListener != null) {
            onValueChangeListener.onChange(value);
        }
        postInvalidate();
    }


    /**
     * 计算水平速度 像素/秒
     */
    private void calculateVelocity() {
        velocityTracker.computeCurrentVelocity(1000);
        float xVelocity = velocityTracker.getXVelocity(); //计算水平方向的速度（单位秒）
        Log.d(TAG, "xVelocity: " + xVelocity);

        //大于这个值才会被认为是fling
        if (Math.abs(xVelocity) > minFlingVelocity) {
            scroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
            invalidate();
        }
    }


    /**
     * 根据滑动距离，重新计算offset，注意它的有效范围
     */
    private void validateValue() {
        offset += dx;
        if (offset < 0) {
            offset = 0;
            dx = 0;
            scroller.forceFinished(true);
        } else if (offset > maxOffset) {
            offset = maxOffset;
            dx = 0;
            scroller.forceFinished(true);
        }
        value = minValue + Math.round(Math.abs(offset) / gapWidth) * per / 10.0f;
        if (onValueChangeListener != null) {
            onValueChangeListener.onChange(value);
        }
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        //返回true表示滑动还没有结束
        if (scroller.computeScrollOffset()) {
            if (scroller.getCurrX() == scroller.getFinalX()) {
                smoothMoveToCalibration();
            } else {
                int x = scroller.getCurrX();
                dx = lastX - x;
                validateValue();
                lastX = x;
            }
        }
    }

    public float getValue() {
        return value;
    }
}
