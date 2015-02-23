package com.johnsimon.payback.util;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;

public class AnimationUtils {

    public static void animateIn(final View view, Resources resources) {
        animateIn(view, resources, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void animateIn(final View view, Resources resources, int delay) {
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        view.setTranslationY(Resource.getPx(60, resources));
        view.setAlpha(0f);

        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY", 0f);
        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(view, "alpha", 1f);

        animY.setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime));
        animAlpha.setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime));

        animY.setStartDelay(delay);
        animAlpha.setStartDelay(delay);

        if (Resource.isLOrAbove()) {
            PathInterpolator pathInterpolator = new PathInterpolator(0.1f, 0.4f, 0.5f, 1f);
            animY.setInterpolator(pathInterpolator);
            animAlpha.setInterpolator(pathInterpolator);
        } else {
            DecelerateInterpolator pathInterpolator = new DecelerateInterpolator();
            animY.setInterpolator(pathInterpolator);
            animAlpha.setInterpolator(pathInterpolator);
        }

        animAlpha.start();
        animY.start();

        animY.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {

            }
        });
    }

}
