package com.radomar.vkclient.custom_view;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

import com.radomar.vkclient.R;

/**
 * Created by Radomar on 09.02.2016
 */
public class MyFab extends FloatingActionButton {

    private static final int[] STATE_POST_PRESENT = {R.attr.state_post_present};

    private boolean mIsPostPresent = false;

    public MyFab(Context context) {
        super(context);
    }

    public MyFab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {

        if (mIsPostPresent) {
            final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
            mergeDrawableStates(drawableState, STATE_POST_PRESENT);
            return drawableState;
        } else {
            return super.onCreateDrawableState(extraSpace);
        }
    }

    public void setIsPostPresent(boolean isPostPresent) {
        if (mIsPostPresent != isPostPresent) {
            this.mIsPostPresent = isPostPresent;

            refreshDrawableState();
        }
    }
}
