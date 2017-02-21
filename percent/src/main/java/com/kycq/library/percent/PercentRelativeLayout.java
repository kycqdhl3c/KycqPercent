package com.kycq.library.percent;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class PercentRelativeLayout extends RelativeLayout {
	private final PercentLayoutHelper mPercentLayoutHelper;

	public PercentRelativeLayout(Context context) {
		super(context);
		mPercentLayoutHelper = new PercentLayoutHelper(this, null);
	}

	public PercentRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPercentLayoutHelper = new PercentLayoutHelper(this, attrs);
	}

	public PercentRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mPercentLayoutHelper = new PercentLayoutHelper(this, attrs);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public PercentRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		mPercentLayoutHelper = new PercentLayoutHelper(this, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mPercentLayoutHelper.adjustViewHost(widthMeasureSpec, heightMeasureSpec);
		mPercentLayoutHelper.adjustChildren();

		widthMeasureSpec = mPercentLayoutHelper.getWidthMeasureSpec();
		heightMeasureSpec = mPercentLayoutHelper.getHeightMeasureSpec();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mPercentLayoutHelper.handleMeasuredStateTooSmall()) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mPercentLayoutHelper.restoreOriginalParams();
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	public static class LayoutParams extends RelativeLayout.LayoutParams
			implements PercentLayoutHelper.PercentLayoutParams {
		private PercentLayoutHelper.PercentLayoutInfo mPercentLayoutInfo;

		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
			mPercentLayoutInfo = PercentLayoutHelper.generatePercentLayoutInfo(context, attrs);
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}

		@Override
		public PercentLayoutHelper.PercentLayoutInfo getPercentLayoutInfo() {
			if (mPercentLayoutInfo == null) {
				mPercentLayoutInfo = new PercentLayoutHelper.PercentLayoutInfo();
			}
			return mPercentLayoutInfo;
		}

		@Override
		protected void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
			PercentLayoutHelper.fetchWidthAndHeight(this, a, widthAttr, heightAttr);
		}
	}
}
