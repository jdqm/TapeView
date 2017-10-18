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

    //刻度线颜色
    private int calibrationColor = Color.WHITE;

    //字体颜色
    private int textColor = Color.WHITE;

    //三角形颜色
    private int triangleColor = Color.WHITE;

    private float textSize = 14.0f; //sp

    //刻度线的宽度
    private float calibrationWidth;

    //刻度线的高度
    private float calibrationHeight;

    //三角形高度
    private float triangleHeight = 18.0f; //dp

    //当前View的宽度
    private int width;

    //宽度的中间值
    private int middle;

    //最小值
    private float minValue;

    //最大值
    private float maxValue;

    //当前值
    private float value;

    //每一格代表的值
    private float per;

    //当前刻度与最小值的距离 (minValue-value)*gapWidth
    private float offset;

    //当前刻度与最新值的最大距离 (minValue-maxValue)*gapWidth
    private float maxOffset;

    //两个刻度之间的距离
    private float gapWidth = 20.0f;

    //总的刻度数量
    private int totalCalibration;

    //上次滑动位置x坐标
    private float lastX;

    //被认为是快速滑动的最小速度
    private float minFlingVelocity;

    private Scroller scroller;

    //滑动的距离
    private float dx;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private VelocityTracker velocityTracker;

    private OnValueChangeListener onValueChangeListener;

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
        super(context);
        init(context);
    }

    public TapeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TapeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init(context);
    }

    private void init(Context context) {
        minFlingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        scroller = new Scroller(context);
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
        calibrationHeight = ta.getDimension(R.styleable.TapeView_calibrationHeight, DisplayUtil.dp2px(calibrationHeight, context));
        triangleColor = ta.getColor(R.styleable.TapeView_triangleColor, triangleColor);
        triangleHeight = ta.getDimension(R.styleable.TapeView_triangleHeight, DisplayUtil.dp2px(triangleHeight, context));
        textColor = ta.getColor(R.styleable.TapeView_textColor, textColor);
        textSize = ta.getDimension(R.styleable.TapeView_textSize, DisplayUtil.sp2px(textSize, context));
        per = ta.getFloat(R.styleable.TapeView_per, per);
        gapWidth = ta.getDimension(R.styleable.TapeView_gapWidth, DisplayUtil.dp2px(gapWidth, context));
        minValue = ta.getFloat(R.styleable.TapeView_minValue, minValue);
        maxValue = ta.getFloat(R.styleable.TapeView_maxValue, maxValue);
        value = ta.getFloat(R.styleable.TapeView_value, value);
        ta.recycle();
    }

    public void setValue(float value, float minValue, float maxValue, float per) {
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.per = per;
        offset = (minValue - value) / per * gapWidth;
        maxOffset = (minValue - maxValue) * gapWidth;
        totalCalibration = (int) ((maxValue - minValue) / per + 1);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        middle = width / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(bgColor);
        drawCalibration(canvas);
        drawTriangle(canvas);
    }

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
        paint.setColor(calibrationColor);
        paint.setStrokeWidth(calibrationWidth);
        paint.setTextSize(20);

        //当前画的刻度的位置
        float currentCalibration;
        float height;
        String value;

        //计算出第一个刻度，直接跳过前面不需要画的可读
        int distance = (int) (middle + offset);
        int start = 0;
        if (distance < 0) {
            start = (int) (-distance / gapWidth);
        }

        for (int i = start; i < totalCalibration; i++) {

            currentCalibration = middle + offset + i * gapWidth;

            if (currentCalibration < 0) {
                continue;
            }

            if (currentCalibration > width) {
                break;
            }

            if (i % 10 == 0) {
                height = 60;
            } else if (i % 5 == 0) {
                height = 40;
            } else {
                height = 20;
            }
            if (i % 10 == 0) {
                value = String.valueOf((int) (minValue + i));
                canvas.drawText(value, currentCalibration - paint.measureText(value) / 2, height + triangleHeight + 30, paint);
            }
            canvas.drawLine(currentCalibration, 0, currentCalibration, height, paint);

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
                dx = lastX - x;
                changeMoveAndValue();
                break;
            case MotionEvent.ACTION_UP:
                countMoveEnd();
                countVelocityTracker();
                return false;

        }
        lastX = x;
        return true;
    }

    /**
     * 滑动结束后，若是指针在2条刻度之间时，改变mOffset 让指针正好在刻度上。
     */
    private void countMoveEnd() {

        offset -= dx;
        if (offset <= maxOffset) {
            offset = maxOffset;
        } else if (offset >= 0) {
            offset = 0;
        }

        lastX = 0;
        dx = 0;

        value = minValue + Math.round(Math.abs(offset) / gapWidth) * per;
        offset = (minValue - value) / per * gapWidth;
        if (onValueChangeListener != null) {
            onValueChangeListener.onChange(value);
        }
        postInvalidate();
    }


    private void countVelocityTracker() {
        velocityTracker.computeCurrentVelocity(1000);
        float xVelocity = velocityTracker.getXVelocity(); //计算水平方向的速度（单位秒）

        //大于这个值才会被认为是fling
        if (Math.abs(xVelocity) > minFlingVelocity) {
            Log.d(TAG, "countVelocityTracker: " + xVelocity);
            scroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        }
    }


    private void changeMoveAndValue() {
        offset -= dx;
        if (offset <= maxOffset) {
            offset = maxOffset;
            dx = 0;
            scroller.forceFinished(true);
        } else if (offset >= 0) {
            offset = 0;
            dx = 0;
            scroller.forceFinished(true);
        }
        value = minValue + Math.round(Math.abs(offset) / gapWidth);
        if (onValueChangeListener != null) {
            onValueChangeListener.onChange(value);
        }
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {  //返回true表示滑动还没有结束
            if (scroller.getCurrX() == scroller.getFinalX()) {
                countMoveEnd();
            } else {
                int x = scroller.getCurrX();
                dx = lastX - x;
                changeMoveAndValue();
                lastX = x;
            }
        }
    }

}
