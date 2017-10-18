package com.jdqm.tapelibrary;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by Jdqm on 2017-10-16.
 */

public class DisplayUtil {

    public static float dp2px(float dpValue, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, context.getResources().getDisplayMetrics());
    }

    public static float sp2px(float spValue, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spValue, context.getResources().getDisplayMetrics());
    }
}
