package com.kycq.library.percent;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PercentLayoutHelper {
	/** 非法值 */
	private static final int ILLEGAL = -1;
	
	private final ViewGroup mViewHost;
	
	private PercentLayoutInfo mViewHostPercentLayoutInfo;
	private int mWidthMeasureSpec = ILLEGAL;
	private int mHeightMeasureSpec = ILLEGAL;
	
	private static int mScreenWidth = ILLEGAL;
	private static int mScreenHeight = ILLEGAL;
	
	public PercentLayoutHelper(ViewGroup viewHost, AttributeSet attrs) {
		mViewHost = viewHost;
		
		mViewHostPercentLayoutInfo = generatePercentLayoutInfo(viewHost.getContext(), attrs);
		
		DisplayMetrics displayMetrics = viewHost.getContext().getResources().getDisplayMetrics();
		mScreenWidth = displayMetrics.widthPixels;
		mScreenHeight = displayMetrics.heightPixels;
	}
	
	public int getWidthMeasureSpec() {
		if (mWidthMeasureSpec == ILLEGAL) {
			throw new RuntimeException("must invoke adjustViewHost() method before getWidthMeasureSpec()");
		}
		return mWidthMeasureSpec;
	}
	
	public int getHeightMeasureSpec() {
		if (mWidthMeasureSpec == ILLEGAL) {
			throw new RuntimeException("must invoke adjustViewHost() method before getHeightMeasureSpec()");
		}
		return mHeightMeasureSpec;
	}
	
	public void adjustViewHost(int widthMeasureSpec, int heightMeasureSpec) {
		if (mViewHostPercentLayoutInfo == null) {
			mWidthMeasureSpec = widthMeasureSpec;
			mHeightMeasureSpec = heightMeasureSpec;
			return;
		}
		
		ViewGroup viewHostParent = (ViewGroup) mViewHost.getParent();
		
		int viewWidth = viewHostParent.getMeasuredWidth() - viewHostParent.getPaddingLeft() - viewHostParent.getPaddingRight();
		int viewHeight = viewHostParent.getMeasuredHeight() - viewHostParent.getPaddingTop() - viewHostParent.getPaddingBottom();
		
		ViewGroup.LayoutParams hostParams = mViewHost.getLayoutParams();
		if (hostParams instanceof ViewGroup.MarginLayoutParams) {
			mViewHostPercentLayoutInfo.fillMarginLayoutParams(
					mViewHost,
					(ViewGroup.MarginLayoutParams) hostParams,
					viewWidth, viewHeight
			);
		} else {
			mViewHostPercentLayoutInfo.fillLayoutParams(
					hostParams,
					viewWidth, viewHeight
			);
		}
		
		if (hostParams.width == ViewGroup.LayoutParams.MATCH_PARENT
				|| hostParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
			mWidthMeasureSpec = widthMeasureSpec;
		} else {
			mWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(hostParams.width, View.MeasureSpec.EXACTLY);
		}
		if (hostParams.height == ViewGroup.LayoutParams.MATCH_PARENT
				|| hostParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
			mHeightMeasureSpec = heightMeasureSpec;
		} else {
			mHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(hostParams.height, View.MeasureSpec.EXACTLY);
		}
		mViewHostPercentLayoutInfo.fillViewPadding(
				mViewHost,
				hostParams.width, hostParams.height
		);
	}
	
	public void adjustChildren() {
		if (mWidthMeasureSpec == ILLEGAL || mHeightMeasureSpec == ILLEGAL) {
			throw new RuntimeException("must invoke adjustViewHost() method before adjustChildren()");
		}
		
		int viewHostWidth = View.MeasureSpec.getSize(mWidthMeasureSpec) - mViewHost.getPaddingLeft() - mViewHost.getPaddingRight();
		int viewHostHeight = View.MeasureSpec.getSize(mHeightMeasureSpec) - mViewHost.getPaddingTop() - mViewHost.getPaddingBottom();
		
		int childCount = mViewHost.getChildCount();
		for (int index = 0; index < childCount; index++) {
			View view = mViewHost.getChildAt(index);
			ViewGroup.LayoutParams params = view.getLayoutParams();
			if (params instanceof PercentLayoutParams) {
				PercentLayoutInfo percentLayoutInfo = ((PercentLayoutParams) params).getPercentLayoutInfo();
				if (percentLayoutInfo != null) {
					if (params instanceof ViewGroup.MarginLayoutParams) {
						percentLayoutInfo.fillMarginLayoutParams(
								view,
								(ViewGroup.MarginLayoutParams) params,
								viewHostWidth, viewHostHeight);
					} else {
						percentLayoutInfo.fillLayoutParams(
								params,
								viewHostWidth, viewHostHeight
						);
					}
					percentLayoutInfo.fillViewPadding(
							view,
							params.width, params.height
					);
				}
			}
		}
	}
	
	public boolean handleMeasuredStateTooSmall() {
		boolean needsSecondMeasure = false;
		
		int childCount = mViewHost.getChildCount();
		for (int index = 0; index < childCount; index++) {
			View view = mViewHost.getChildAt(index);
			ViewGroup.LayoutParams params = view.getLayoutParams();
			if (params instanceof PercentLayoutParams) {
				PercentLayoutInfo percentLayoutInfo = ((PercentLayoutParams) params).getPercentLayoutInfo();
				if (shouldHandleMeasuredWidthTooSmall(view, percentLayoutInfo)) {
					needsSecondMeasure = true;
					params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
				}
				if (shouldHandleMeasuredHeightTooSmall(view, percentLayoutInfo)) {
					needsSecondMeasure = true;
					params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
				}
			}
		}
		return needsSecondMeasure;
	}
	
	private static boolean shouldHandleMeasuredWidthTooSmall(View view, PercentLayoutInfo percentLayoutInfo) {
		if (percentLayoutInfo.widthPercent.percentMode == PercentParamsInfo.NONE_PERCENT) {
			return false;
		}
		
		int state = ViewCompat.getMeasuredWidthAndState(view) & ViewCompat.MEASURED_STATE_MASK;
		
		return state == ViewCompat.MEASURED_STATE_TOO_SMALL
				&& percentLayoutInfo.widthPercent.percentValue >= 0
				&& percentLayoutInfo.mPreservedParams.width == ViewGroup.LayoutParams.WRAP_CONTENT;
	}
	
	private static boolean shouldHandleMeasuredHeightTooSmall(View view, PercentLayoutInfo percentLayoutInfo) {
		if (percentLayoutInfo.heightPercent.percentMode == PercentParamsInfo.NONE_PERCENT) {
			return false;
		}
		
		int state = ViewCompat.getMeasuredHeightAndState(view) & ViewCompat.MEASURED_STATE_MASK;
		return state == ViewCompat.MEASURED_STATE_TOO_SMALL
				&& percentLayoutInfo.heightPercent.percentValue >= 0
				&& percentLayoutInfo.mPreservedParams.height == ViewGroup.LayoutParams.WRAP_CONTENT;
	}
	
	public void restoreOriginalParams() {
		int childCount = mViewHost.getChildCount();
		for (int index = 0; index < childCount; index++) {
			View view = mViewHost.getChildAt(index);
			ViewGroup.LayoutParams params = view.getLayoutParams();
			if (params instanceof PercentLayoutParams) {
				PercentLayoutInfo percentLayoutInfo = ((PercentLayoutParams) params).getPercentLayoutInfo();
				if (percentLayoutInfo != null) {
					if (params instanceof ViewGroup.MarginLayoutParams) {
						percentLayoutInfo.restoreMarginLayoutParams((ViewGroup.MarginLayoutParams) params);
					} else {
						percentLayoutInfo.restoreLayoutParams(params);
					}
				}
			}
		}
	}
	
	public static PercentLayoutInfo generatePercentLayoutInfo(Context context, AttributeSet attrs) {
		if (attrs == null) {
			return null;
		}
		
		PercentLayoutInfo percentLayoutInfo = new PercentLayoutInfo();
		
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PercentLayout);
		
		// widthPercent
		String widthPercentStr = array.getString(R.styleable.PercentLayout_percent_width);
		percentLayoutInfo.widthPercent.readPercentParamsInfo(widthPercentStr);
		// heightPercent
		String heightPercentStr = array.getString(R.styleable.PercentLayout_percent_height);
		percentLayoutInfo.heightPercent.readPercentParamsInfo(heightPercentStr);
		
		// marginPercent
		String marginPercentStr = array.getString(R.styleable.PercentLayout_percent_margin);
		percentLayoutInfo.marginPercent.readPercentParamsInfo(marginPercentStr);
		// marginLeftPercent
		String leftMarginPercentStr = array.getString(R.styleable.PercentLayout_percent_marginLeft);
		percentLayoutInfo.leftMarginPercent.readPercentParamsInfo(leftMarginPercentStr);
		// marginTopPercent
		String topMarginPercentStr = array.getString(R.styleable.PercentLayout_percent_marginTop);
		percentLayoutInfo.topMarginPercent.readPercentParamsInfo(topMarginPercentStr);
		// marginRightPercent
		String rightMarginPercentStr = array.getString(R.styleable.PercentLayout_percent_marginRight);
		percentLayoutInfo.rightMarginPercent.readPercentParamsInfo(rightMarginPercentStr);
		// marginBottomPercent
		String bottomMarginPercentStr = array.getString(R.styleable.PercentLayout_percent_marginBottom);
		percentLayoutInfo.bottomMarginPercent.readPercentParamsInfo(bottomMarginPercentStr);
		// marginStartPercent
		String startMarginPercentStr = array.getString(R.styleable.PercentLayout_percent_marginStart);
		percentLayoutInfo.startMarginPercent.readPercentParamsInfo(startMarginPercentStr);
		// marginEndPercent
		String endMarginPercentStr = array.getString(R.styleable.PercentLayout_percent_marginEnd);
		percentLayoutInfo.endMarginPercent.readPercentParamsInfo(endMarginPercentStr);
		
		// paddingPercent
		String paddingPercentStr = array.getString(R.styleable.PercentLayout_percent_padding);
		percentLayoutInfo.paddingPercent.readPercentParamsInfo(paddingPercentStr);
		// paddingLeftPercent
		String leftPaddingPercentStr = array.getString(R.styleable.PercentLayout_percent_paddingLeft);
		percentLayoutInfo.leftPaddingPercent.readPercentParamsInfo(leftPaddingPercentStr);
		// paddingTopPercent
		String topPaddingPercentStr = array.getString(R.styleable.PercentLayout_percent_paddingTop);
		percentLayoutInfo.topPaddingPercent.readPercentParamsInfo(topPaddingPercentStr);
		// paddingRightPercent
		String rightPaddingPercentStr = array.getString(R.styleable.PercentLayout_percent_paddingRight);
		percentLayoutInfo.rightPaddingPercent.readPercentParamsInfo(rightPaddingPercentStr);
		// paddingBottomPercent
		String bottomPaddingPercentStr = array.getString(R.styleable.PercentLayout_percent_paddingBottom);
		percentLayoutInfo.bottomPaddingPercent.readPercentParamsInfo(bottomPaddingPercentStr);
		
		array.recycle();
		
		return percentLayoutInfo;
	}
	
	public static void fetchWidthAndHeight(ViewGroup.LayoutParams params, TypedArray array, int widthAttr, int heightAttr) {
		params.width = array.getLayoutDimension(widthAttr, 0);
		params.height = array.getLayoutDimension(heightAttr, 0);
	}
	
	public static class PercentLayoutInfo {
		PercentParamsInfo widthPercent = new PercentParamsInfo();
		PercentParamsInfo heightPercent = new PercentParamsInfo();
		
		PercentParamsInfo marginPercent = new PercentParamsInfo();
		PercentParamsInfo leftMarginPercent = new PercentParamsInfo();
		PercentParamsInfo topMarginPercent = new PercentParamsInfo();
		PercentParamsInfo rightMarginPercent = new PercentParamsInfo();
		PercentParamsInfo bottomMarginPercent = new PercentParamsInfo();
		PercentParamsInfo startMarginPercent = new PercentParamsInfo();
		PercentParamsInfo endMarginPercent = new PercentParamsInfo();
		
		PercentParamsInfo paddingPercent = new PercentParamsInfo();
		PercentParamsInfo leftPaddingPercent = new PercentParamsInfo();
		PercentParamsInfo topPaddingPercent = new PercentParamsInfo();
		PercentParamsInfo rightPaddingPercent = new PercentParamsInfo();
		PercentParamsInfo bottomPaddingPercent = new PercentParamsInfo();
		PercentParamsInfo startPaddingPercent = new PercentParamsInfo();
		PercentParamsInfo endPaddingPercent = new PercentParamsInfo();
		
		final ViewGroup.MarginLayoutParams mPreservedParams;
		
		public PercentLayoutInfo() {
			mPreservedParams = new ViewGroup.MarginLayoutParams(0, 0);
		}
		
		void fillLayoutParams(ViewGroup.LayoutParams params,
		                      int widthHint, int heightHint) {
			mPreservedParams.width = params.width;
			mPreservedParams.height = params.height;
			
			params.width = getSizeByPercent(widthHint, heightHint, widthPercent, params.width);
			params.height = getSizeByPercent(widthHint, heightHint, heightPercent, params.height);
		}
		
		void restoreLayoutParams(ViewGroup.LayoutParams params) {
			params.width = mPreservedParams.width;
			params.height = mPreservedParams.height;
		}
		
		void fillMarginLayoutParams(View view, ViewGroup.MarginLayoutParams params,
		                            int widthHint, int heightHint) {
			fillLayoutParams(params, widthHint, heightHint);
			
			mPreservedParams.leftMargin = params.leftMargin;
			mPreservedParams.topMargin = params.topMargin;
			mPreservedParams.rightMargin = params.rightMargin;
			mPreservedParams.bottomMargin = params.bottomMargin;
			
			MarginLayoutParamsCompat.setMarginStart(
					mPreservedParams,
					MarginLayoutParamsCompat.getMarginStart(params)
			);
			MarginLayoutParamsCompat.setMarginEnd(
					mPreservedParams,
					MarginLayoutParamsCompat.getMarginEnd(params)
			);
			
			if (marginPercent.percentMode != PercentParamsInfo.NONE_PERCENT) {
				int marginSize = getSizeByPercent(widthHint, heightHint, marginPercent, 0);
				params.leftMargin = marginSize;
				params.topMargin = marginSize;
				params.rightMargin = marginSize;
				params.bottomMargin = marginSize;
			}
			
			params.leftMargin = getSizeByPercent(widthHint, heightHint, leftMarginPercent, params.leftMargin);
			params.topMargin = getSizeByPercent(widthHint, heightHint, topMarginPercent, params.topMargin);
			params.rightMargin = getSizeByPercent(widthHint, heightHint, rightMarginPercent, params.rightMargin);
			params.bottomMargin = getSizeByPercent(widthHint, heightHint, bottomMarginPercent, params.bottomMargin);
			
			if (startMarginPercent.percentMode != PercentParamsInfo.NONE_PERCENT) {
				MarginLayoutParamsCompat.setMarginStart(
						params,
						getSizeByPercent(
								widthHint, heightHint,
								startMarginPercent,
								MarginLayoutParamsCompat.getMarginStart(params)
						)
				);
			}
			if (endMarginPercent.percentMode != PercentParamsInfo.NONE_PERCENT) {
				MarginLayoutParamsCompat.setMarginEnd(
						params,
						getSizeByPercent(
								widthHint, heightHint,
								endMarginPercent,
								MarginLayoutParamsCompat.getMarginEnd(params)
						)
				);
			}
			if (startMarginPercent.percentMode != PercentParamsInfo.NONE_PERCENT
					|| endMarginPercent.percentMode != PercentParamsInfo.NONE_PERCENT) {
				MarginLayoutParamsCompat.resolveLayoutDirection(params,
						ViewCompat.getLayoutDirection(view));
			}
		}
		
		void restoreMarginLayoutParams(ViewGroup.MarginLayoutParams params) {
			restoreLayoutParams(params);
			
			params.leftMargin = mPreservedParams.leftMargin;
			params.topMargin = mPreservedParams.topMargin;
			params.rightMargin = mPreservedParams.rightMargin;
			params.bottomMargin = mPreservedParams.bottomMargin;
			
			MarginLayoutParamsCompat.setMarginStart(
					params,
					MarginLayoutParamsCompat.getMarginStart(mPreservedParams)
			);
			MarginLayoutParamsCompat.setMarginEnd(
					params,
					MarginLayoutParamsCompat.getMarginEnd(mPreservedParams)
			);
		}
		
		void fillViewPadding(View view, int viewWidth, int viewHeight) {
			int leftPadding = view.getPaddingLeft();
			int topPadding = view.getPaddingTop();
			int rightPadding = view.getPaddingRight();
			int bottomPadding = view.getPaddingBottom();
			
			if (paddingPercent.percentMode != PercentParamsInfo.NONE_PERCENT) {
				int paddingSize = getSizeByPercent(viewWidth, viewHeight, paddingPercent, 0);
				leftPadding = paddingSize;
				topPadding = paddingSize;
				rightPadding = paddingSize;
				bottomPadding = paddingSize;
			}
			
			leftPadding = getSizeByPercent(viewWidth, viewHeight, leftPaddingPercent, leftPadding);
			topPadding = getSizeByPercent(viewWidth, viewHeight, topPaddingPercent, topPadding);
			rightPadding = getSizeByPercent(viewWidth, viewHeight, rightPaddingPercent, rightPadding);
			bottomPadding = getSizeByPercent(viewWidth, viewHeight, bottomPaddingPercent, bottomPadding);
			
			view.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
			
			if (startPaddingPercent.percentMode != PercentParamsInfo.NONE_PERCENT) {
				int startPadding = getSizeByPercent(viewWidth, viewHeight, startPaddingPercent, ViewCompat.getPaddingStart(view));
				ViewCompat.setPaddingRelative(view, startPadding, topPadding, ViewCompat.getPaddingEnd(view), bottomPadding);
			}
			if (endPaddingPercent.percentMode != PercentParamsInfo.NONE_PERCENT) {
				int endPadding = getSizeByPercent(viewWidth, viewHeight, endPaddingPercent, ViewCompat.getPaddingEnd(view));
				ViewCompat.setPaddingRelative(view, ViewCompat.getPaddingStart(view), topPadding, endPadding, bottomPadding);
			}
		}
		
		private int getSizeByPercent(int widthHint, int heightHint,
		                             PercentParamsInfo percentParamsInfo, int defaultValue) {
			switch (percentParamsInfo.percentMode) {
				case PercentParamsInfo.W_PERCENT:
					return (int) (widthHint * percentParamsInfo.percentValue);
				case PercentParamsInfo.H_PERCENT:
					return (int) (heightHint * percentParamsInfo.percentValue);
				case PercentParamsInfo.SW_PERCENT:
					return (int) (mScreenWidth * percentParamsInfo.percentValue);
				case PercentParamsInfo.SH_PERCENT:
					return (int) (mScreenHeight * percentParamsInfo.percentValue);
				case PercentParamsInfo.NONE_PERCENT:
				default:
					return defaultValue;
			}
		}
	}
	
	private static class PercentParamsInfo {
		private static final String REGEX_PERCENT = "^([-]?\\d*([.]\\d+)?)%([s]?[wh])$";
		
		private static final String W = "w";
		private static final String H = "h";
		private static final String SW = "sw";
		private static final String SH = "sh";
		
		private static final int NONE_PERCENT = 0;
		private static final int W_PERCENT = 1;
		private static final int H_PERCENT = 2;
		private static final int SW_PERCENT = 3;
		private static final int SH_PERCENT = 4;
		
		float percentValue;
		int percentMode = NONE_PERCENT;
		
		private void readPercentParamsInfo(String percentStr) {
			if (percentStr == null) {
				return;
			}
			
			Pattern pattern = Pattern.compile(REGEX_PERCENT);
			Matcher matcher = pattern.matcher(percentStr);
			if (!matcher.matches()) {
				throw new RuntimeException("the value '" + percentStr + "' of layout_xxxPercent invalid!");
			}
			
			String percentValue = matcher.group(1);
			this.percentValue = Float.parseFloat(percentValue) / 100f;
			
			String percentMode = matcher.group(3);
			if (W.equals(percentMode)) {
				this.percentMode = PercentParamsInfo.W_PERCENT;
			} else if (H.equals(percentMode)) {
				this.percentMode = PercentParamsInfo.H_PERCENT;
			} else if (SW.equals(percentMode)) {
				this.percentMode = PercentParamsInfo.SW_PERCENT;
			} else if (SH.equals(percentMode)) {
				this.percentMode = PercentParamsInfo.SH_PERCENT;
			} else {
				throw new RuntimeException("the " + percentStr + " must be endWith [" + PercentParamsInfo.W + "|" + PercentParamsInfo.H + "|" + PercentParamsInfo.SW + "|" + PercentParamsInfo.SH + "]");
			}
		}
	}
	
	public interface PercentLayoutParams {
		PercentLayoutInfo getPercentLayoutInfo();
	}
	
}
