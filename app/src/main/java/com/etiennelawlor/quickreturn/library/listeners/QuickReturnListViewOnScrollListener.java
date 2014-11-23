package com.etiennelawlor.quickreturn.library.listeners;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.etiennelawlor.quickreturn.library.enums.QuickReturnType;
import com.johnsimon.payback.util.Resource;

public class QuickReturnListViewOnScrollListener extends RecyclerView.OnScrollListener {

    // region Member Variables
    private int mMinFooterTranslation;
    private int mMinHeaderTranslation;
    private int mHeaderDiffTotal = 0;
    private int mFooterDiffTotal = 0;
    private int mHeaderMaxHeight = 0;
    private View mHeader;
    private View mHeaderImage;
    private View mFooter;
    private QuickReturnType mQuickReturnType;
    private boolean mCanSlideInIdleScrollState = false;
    // endregion

    // region Constructors
    public QuickReturnListViewOnScrollListener(QuickReturnType quickReturnType, View headerView, int headerTranslation, View footerView, int footerTranslation, View image){
        mQuickReturnType = quickReturnType;
        mHeader =  headerView;
        mMinHeaderTranslation = headerTranslation;
        mFooter =  footerView;
        mMinFooterTranslation = footerTranslation;
        mHeaderImage = image;
        mHeaderMaxHeight = image.getLayoutParams().height;
    }
    // endregion

	@Override
	public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
		if (newState == RecyclerView.SCROLL_STATE_IDLE && mCanSlideInIdleScrollState){

			int midHeader = -mMinHeaderTranslation/2;
			int midFooter = mMinFooterTranslation/2;

			switch (mQuickReturnType) {
				case HEADER:
					if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
						ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), 0);
						anim.setDuration(100);
						anim.start();
						mHeaderDiffTotal = 0;
					} else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
						ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), mMinHeaderTranslation);
						anim.setDuration(100);
						anim.start();
						mHeaderDiffTotal = mMinHeaderTranslation;
					}
					break;
				case FOOTER:
					if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
						ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), 0);
						anim.setDuration(100);
						anim.start();
						mFooterDiffTotal = 0;
					} else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
						ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), mMinFooterTranslation);
						anim.setDuration(100);
						anim.start();
						mFooterDiffTotal = -mMinFooterTranslation;
					}
					break;
				case BOTH:
					if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
						ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), 0);
						anim.setDuration(100);
						anim.start();
						mHeaderDiffTotal = 0;
					} else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
						ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), mMinHeaderTranslation);
						anim.setDuration(100);
						anim.start();
						mHeaderDiffTotal = mMinHeaderTranslation;
					}

					if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
						ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), 0);
						anim.setDuration(100);
						anim.start();
						mFooterDiffTotal = 0;
					} else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
						ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), mMinFooterTranslation);
						anim.setDuration(100);
						anim.start();
						mFooterDiffTotal = -mMinFooterTranslation;
					}
					break;
				case TWITTER:
					if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
						ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), 0);
						anim.setDuration(100);
						anim.start();
						mHeaderDiffTotal = 0;
					} else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
						ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), mMinHeaderTranslation);
						anim.setDuration(100);
						anim.start();
						mHeaderDiffTotal = mMinHeaderTranslation;
					}

					if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
						ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), 0);
						anim.setDuration(100);
						anim.start();
						mFooterDiffTotal = 0;
					} else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
						ObjectAnimator anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter.getTranslationY(), mMinFooterTranslation);
						anim.setDuration(100);
						anim.start();
						mFooterDiffTotal = -mMinFooterTranslation;
					}
					break;
			}

		}
	}

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

		dy *= -1;

		if(dy != 0){
			switch (mQuickReturnType){
				case HEADER:
					if(dy < 0){ // scrolling down
						mHeaderDiffTotal = Math.max(mHeaderDiffTotal + dy, mMinHeaderTranslation);
					} else { // scrolling up
						mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + dy, mMinHeaderTranslation), 0);
					}

                    Log.d("PAY BACK", mHeaderMaxHeight - (mHeaderDiffTotal) + "");

					mHeader.setTranslationY(mHeaderDiffTotal);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mHeaderImage.getLayoutParams();
                    params.height =  mHeaderMaxHeight - (mHeaderDiffTotal);
                    mHeaderImage.setLayoutParams(params);
					break;
				case FOOTER:
					if(dy < 0){ // scrolling down
						mFooterDiffTotal = Math.max(mFooterDiffTotal + dy, -mMinFooterTranslation);
					} else { // scrolling up
						mFooterDiffTotal = Math.min(Math.max(mFooterDiffTotal + dy, -mMinFooterTranslation), 0);
					}

					mFooter.setTranslationY(-mFooterDiffTotal);
					break;
				case BOTH:
					if(dy < 0){ // scrolling down
						mHeaderDiffTotal = Math.max(mHeaderDiffTotal + dy, mMinHeaderTranslation);
						mFooterDiffTotal = Math.max(mFooterDiffTotal + dy, -mMinFooterTranslation);
					} else { // scrolling up
						mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + dy, mMinHeaderTranslation), 0);
						mFooterDiffTotal = Math.min(Math.max(mFooterDiffTotal + dy, -mMinFooterTranslation), 0);
					}

					mHeader.setTranslationY(mHeaderDiffTotal);
					mFooter.setTranslationY(-mFooterDiffTotal);
					break;

				default:
					break;
			}
		}
	}

    public void setCanSlideInIdleScrollState(boolean canSlideInIdleScrollState){
        mCanSlideInIdleScrollState = canSlideInIdleScrollState;
    }
}
