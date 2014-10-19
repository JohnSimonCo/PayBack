package com.johnsimon.payback;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.Toast;

public class FloatingActionButton extends View {

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private final Paint mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap mBitmap;
    private int mScreenHeight;
    private int mColor;
    private float mCurrentY;
    private boolean mHidden = false;
    private Rect mRect = null;

	public boolean mActive = true;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingActionButton);
        mColor = a.getColor(R.styleable.FloatingActionButton_fab_color, Color.WHITE);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setColor(mColor);
        float radius, dx, dy;
        radius = a.getFloat(R.styleable.FloatingActionButton_fab_shadowRadius, 10.0f);
        dx = a.getFloat(R.styleable.FloatingActionButton_fab_shadowDx, 0.0f);
        dy = a.getFloat(R.styleable.FloatingActionButton_fab_shadowDy, 3.5f);
        int color = a.getInteger(R.styleable.FloatingActionButton_fab_shadowColor, Color.argb(100, 0, 0, 0));
        mButtonPaint.setShadowLayer(radius, dx, dy, color);

        Drawable drawable = a.getDrawable(R.styleable.FloatingActionButton_fab_drawable);
        if (null != drawable) {
            mBitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        WindowManager mWindowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenHeight = size.y;
    }

    public void setColor(int color) {
        mColor = color;
        mButtonPaint.setColor(mColor);
        invalidate();
    }

    public void setDrawable(Drawable drawable) {
        mBitmap = ((BitmapDrawable) drawable).getBitmap();
        invalidate();
    }

	public void setActive(boolean active) {
		mActive = active;
		if (active) {
			setAlpha(1f);
		} else {
			setAlpha(0.6f);
		}
	}

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), mButtonPaint);
        if (null != mBitmap) {
            canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2,
                    (getHeight() - mBitmap.getHeight()) / 2, mDrawablePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

		if (!mActive) return super.onTouchEvent(event);

        int color;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            color = mColor;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN){
            color = darkenColor(mColor);
            mRect = new Rect(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
        } else if(event.getAction() == MotionEvent.ACTION_MOVE){
            if(!mRect.contains(this.getLeft() + (int) event.getX(), this.getTop() + (int) event.getY())){
                color = mColor;
            } else {
                color = darkenColor(mColor);
            }
        } else {
            color = darkenColor(mColor);
        }

        mButtonPaint.setColor(color);
        invalidate();
        return super.onTouchEvent(event);
    }

    public void hide(boolean hide) {
        if (mHidden != hide) {
            float offset;
            if (mHidden) {
                offset = mCurrentY;
            } else {
                mCurrentY = getY();
                offset = mScreenHeight;
            }
            mHidden = hide;
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "Y", offset);
            animator.setInterpolator(mInterpolator);
            animator.start();
        }
    }

    public int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        if (mColor == getResources().getColor(android.R.color.white)) {
            hsv[2] *= 0.8f;
        } else {
            hsv[2] *= 1.2f;
        }

        return Color.HSVToColor(hsv);
    }
}