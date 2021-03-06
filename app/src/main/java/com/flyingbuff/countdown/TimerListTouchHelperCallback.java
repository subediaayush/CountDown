package com.flyingbuff.countdown;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

/**
 * Created by Aayush on 8/18/2016.
 */
public class TimerListTouchHelperCallback extends ItemTouchHelper.SimpleCallback {
	private final Context context;
	Drawable background;
	Drawable deleteIcon;
	int deleteIconMargin;

	private TimerAdapter timerAdapter;

	public TimerListTouchHelperCallback(Context context, TimerAdapter timerAdapter, int dragDirs, int swipeDirs) {
		super(dragDirs, swipeDirs);

		this.timerAdapter = timerAdapter;

		this.context = context;
		background = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDeletedLayout));
		deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_trash);
		deleteIconMargin = (int) context.getResources().getDimension(R.dimen.ic_clear_margin);
	}

	@Override
	public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		TimerHolder holder = (TimerHolder) viewHolder;
		int id = (int) timerAdapter.getItemId(holder.getAdapterPosition());

		if (timerAdapter.removalPendingTimers.contains(id) || holder.getAdapterPosition() == 0)
			return 0;
		else
			return super.getSwipeDirs(recyclerView, viewHolder);
	}

	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
		return false;
	}

	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {


	}

	@Override
	public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
		View itemView = viewHolder.itemView;

		// not sure why, but this method get's called for viewholder that are already swiped away
		if (viewHolder.getAdapterPosition() == -1) {
			// not interested in those
			return;
		}

		// draw red background
//        background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
//        background.draw(c);

		// draw x mark
		int itemHeight = itemView.getBottom() - itemView.getTop();
		int intrinsicWidth = deleteIcon.getIntrinsicWidth();
		int intrinsicHeight = deleteIcon.getIntrinsicWidth();

		int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
		int deleteIconRight = itemView.getRight() - deleteIconMargin;
		int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
		int deleteIconBottom = deleteIconTop + intrinsicHeight;
		deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);

		deleteIcon.draw(c);

		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
	}
}
