package com.sm.music.Override;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class UnclickableHorizontalScrollView extends HorizontalScrollView {
    public UnclickableHorizontalScrollView(Context context) {
        super(context);

    }

    public UnclickableHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        return false;
    }
}
