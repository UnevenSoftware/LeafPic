package com.horaapps.leafpic.Views;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.horaapps.leafpic.utils.Measure;

/**
 * Created by dnld on 06/03/16.
 */
public class FabScrollBehaviour extends FloatingActionButton.Behavior {

    public FabScrollBehaviour(Context context, AttributeSet attributeSet) {
         super();
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0)
            child.animate().translationY(child.getHeight()*2).setInterpolator(new AccelerateInterpolator(2)).start();
        else
            child.animate().translationY(-Measure.getNavigationBarSize(coordinatorLayout.getContext()).y).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
}
