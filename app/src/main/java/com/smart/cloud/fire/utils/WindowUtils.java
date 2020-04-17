package com.smart.cloud.fire.utils;

import android.app.Activity;
import android.content.Context;
import android.view.WindowManager;

public class WindowUtils {

    public static void backgroundAlpha(Context mContext,float bgAlpha) {
        if (((Activity)mContext) == null) {
            return;
        }
        WindowManager.LayoutParams lp = ((Activity)mContext).getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        ((Activity)mContext).getWindow().setAttributes(lp);
    }
}
