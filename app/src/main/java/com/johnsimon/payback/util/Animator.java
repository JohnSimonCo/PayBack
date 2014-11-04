package com.johnsimon.payback.util;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

/**
 * Created by John on 2014-11-04.
 */
public class Animator {
	public static void expand(final View v) {
		expand(v, true, 4);
	}

	public static void expand(final View v, boolean b) {
		expand(v, b, 4);
	}

	public static void expand(final View v, boolean animate, int msPerDp) {
		v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		final int targtetHeight = v.getMeasuredHeight();

		if (animate) {
			v.getLayoutParams().height = 0;
			v.setVisibility(View.VISIBLE);
			Animation a = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					v.getLayoutParams().height = interpolatedTime == 1
							? LinearLayout.LayoutParams.WRAP_CONTENT
							: (int) (targtetHeight * interpolatedTime);
					v.requestLayout();
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};

			// 0.333dp/ms
			a.setDuration((int) (targtetHeight / v.getContext().getResources().getDisplayMetrics().density) * msPerDp);
			v.startAnimation(a);
		} else {
			v.setVisibility(View.VISIBLE);
			v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
			v.requestLayout();
		}
	}

	public static void collapse(final View v) {
		collapse(v, true, 4);
	}

	public static void collapse(final View v, boolean b) {
		collapse(v, b, 4);
	}

	public static void collapse(final View v, boolean animate, int msPerDp) {
		final int initialHeight = v.getMeasuredHeight();

		if (animate) {
			Animation a = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					if (interpolatedTime == 1) {
						v.setVisibility(View.GONE);
					} else {
						v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
						v.requestLayout();
					}
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};

			// 0.333dp/ms
			a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density) * msPerDp);
			v.startAnimation(a);
		} else {
			v.setVisibility(View.GONE);
			v.getLayoutParams().height = 0;
			v.requestLayout();
		}
	}

	public static void doListAnimation(final View view, int offset) {
		view.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		view.setAlpha(0f);

		ObjectAnimator animAlpha = ObjectAnimator.ofFloat(view,
				"alpha", 1);
		animAlpha.setDuration(450);
		animAlpha.setStartDelay(offset);
		animAlpha.start();

		view.setRotation(20f);

		ObjectAnimator rotation = ObjectAnimator.ofFloat(view,
				"rotation", 0f);
		rotation.setDuration(350);
		rotation.setStartDelay(offset);
		rotation.start();

		view.setTranslationY(620f);

		ObjectAnimator animY = ObjectAnimator.ofFloat(view,
				"translationY", 0);
		animY.setDuration(450);
		animY.setStartDelay(offset);

		animY.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(android.animation.Animator animation) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		});

		animY.start();
	}
}
