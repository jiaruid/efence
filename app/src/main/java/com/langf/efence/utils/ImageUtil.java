package com.langf.efence.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * Created by dujr on 16-11-8.
 */
public class ImageUtil {

    public static void measureLayout(final View view, final float scale) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            private boolean hasMeasured;
            public boolean onPreDraw() {
                if (hasMeasured == false) {
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    int width = view.getMeasuredWidth();
                    params.height = (int) (width*scale);
                    Logger.d( "poster's width = " + width + ", heigth = " + params.height);
                    view.setLayoutParams(params);
                    hasMeasured = true;
                }
                return true;
            }
        });
    }
}
