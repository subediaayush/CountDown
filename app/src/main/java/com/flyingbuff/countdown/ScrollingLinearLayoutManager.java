package com.flyingbuff.countdown;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * Created by Aayush on 8/29/2016.
 */
public class ScrollingLinearLayoutManager extends LinearLayoutManager {
	public ScrollingLinearLayoutManager(Context context) {
		super(context);
	}

	@Override
	public void smoothScrollToPosition(RecyclerView recyclerView,
	                                   RecyclerView.State state, final int position) {

		RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext());

		smoothScroller.setTargetPosition(position);
		startSmoothScroll(smoothScroller);
	}

	private class TopSnappedSmoothScroller extends LinearSmoothScroller {
		private static final float MILLISECONDS_PER_INCH = 50;

		public TopSnappedSmoothScroller(Context context) {
			super(context);

		}

		@Override
		public PointF computeScrollVectorForPosition(int targetPosition) {
			return ScrollingLinearLayoutManager.this
					.computeScrollVectorForPosition(targetPosition);
		}

		@Override
		protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
			return MILLISECONDS_PER_INCH / (float) displayMetrics.densityDpi;
		}

		@Override
		protected int getVerticalSnapPreference() {
			return SNAP_TO_START;
		}

	}

}
