package com.langf.efence.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class NoScrollViewPager extends ViewPager {

    private boolean isScrollEnable = false;

    public NoScrollViewPager(Context context) {
        super(context);
    }

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (viewPagerTouchEvent != null) {
                    viewPagerTouchEvent.onTouchDown();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (viewPagerTouchEvent != null) {
                    viewPagerTouchEvent.onTouchUp();
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return this.isScrollEnable && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return this.isScrollEnable && super.onInterceptHoverEvent(event);
    }

    public void setScrollEnable(boolean isScrollEnable) {
        this.isScrollEnable = isScrollEnable;
    }

    private OnViewPagerTouchEvent viewPagerTouchEvent;

    public void setOnViewPagerTouchEventListener(OnViewPagerTouchEvent viewPagerTouchEvent) {
        this.viewPagerTouchEvent = viewPagerTouchEvent;
    }

    public interface OnViewPagerTouchEvent {
        void onTouchDown();

        void onTouchUp();
    }
}
