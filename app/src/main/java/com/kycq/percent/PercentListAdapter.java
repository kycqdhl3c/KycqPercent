package com.kycq.percent;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kycq.percent.databinding.ItemPercentFrameLayoutListBinding;
import com.kycq.percent.databinding.ItemPercentLinearLayoutListBinding;
import com.kycq.percent.databinding.ItemPercentRelativeLayoutListBinding;

class PercentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	/** 相对布局 */
	private static final int RELATIVE_LAYOUT = 0;
	/** 帧布局 */
	private static final int FRAME_LAYOUT = 1;
	/** 线性布局 */
	private static final int LINEAR_LAYOUT = 2;
	
	private LayoutInflater mInflater;
	
	PercentListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getItemViewType(int position) {
		if (position % 3 == 0) {
			return RELATIVE_LAYOUT;
		} else if (position % 3 == 1) {
			return FRAME_LAYOUT;
		}
		return LINEAR_LAYOUT;
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == RELATIVE_LAYOUT) {
			ItemPercentRelativeLayoutListBinding dataBinding = DataBindingUtil.inflate(mInflater, R.layout.item_percent_relative_layout_list, parent, false);
			return new RelativeLayoutHolder(dataBinding.getRoot());
		} else if (viewType == FRAME_LAYOUT) {
			ItemPercentFrameLayoutListBinding dataBinding = DataBindingUtil.inflate(mInflater, R.layout.item_percent_frame_layout_list, parent, false);
			return new FrameLayoutHolder(dataBinding.getRoot());
		} else if (viewType == LINEAR_LAYOUT) {
			ItemPercentLinearLayoutListBinding dataBinding = DataBindingUtil.inflate(mInflater, R.layout.item_percent_linear_layout_list, parent, false);
			return new LinearLayoutHolder(dataBinding.getRoot());
		}
		return null;
	}
	
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		
	}
	
	@Override
	public int getItemCount() {
		return 3;
	}
	
	private static class RelativeLayoutHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		
		RelativeLayoutHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
		}
		
		@Override
		public void onClick(View view) {
		}
	}
	
	private static class FrameLayoutHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		
		FrameLayoutHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
		}
		
		@Override
		public void onClick(View view) {
		}
	}
	
	private static class LinearLayoutHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		
		LinearLayoutHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
		}
		
		@Override
		public void onClick(View view) {
		}
	}
	
}
