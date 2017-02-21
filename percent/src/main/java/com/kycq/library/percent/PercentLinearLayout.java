package com.kycq.library.percent;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class PercentLinearLayout extends LinearLayout {
	private final PercentLayoutHelper mPercentLayoutHelper;

	public PercentLinearLayout(Context context) {
		super(context);
		mPercentLayoutHelper = new PercentLayoutHelper(this, null);
	}

	public PercentLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPercentLayoutHelper = new PercentLayoutHelper(this, attrs);
	}

	public PercentLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mPercentLayoutHelper = new PercentLayoutHelper(this, attrs);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public PercentLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
		if (getOrientation() == HORIZONTAL) {
			return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		} else if (getOrientation() == VERTICAL) {
			return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}
		return null;
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	public static class LayoutParams extends LinearLayout.LayoutParams
			implements PercentLayoutHelper.PercentLayoutParams {
		private PercentLayoutHelper.PercentLayoutInfo mPercentLayoutInfo;

		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
			mPercentLayoutInfo = PercentLayoutHelper.generatePercentLayoutInfo(context, attrs);
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(int width, int height, float weight) {
			super(width, height, weight);
		}

		public LayoutParams(ViewGroup.LayoutParams p) {
			super(p);
		}

		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}

		public LayoutParams(LayoutParams source) {
			this((LinearLayout.LayoutParams) source);
			mPercentLayoutInfo = source.mPercentLayoutInfo;
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
