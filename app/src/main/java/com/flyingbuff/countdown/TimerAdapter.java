package com.flyingbuff.countdown;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Aayush on 8/8/2016.
 */
public class TimerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final long TIMER_DELETE_DELAY = 3000;
	private final int TIMER_TYPE_FIRST = 0;
	private final DatabaseHelper dbHelper;
	private final Context context;

	SortedList<Timer> timerList;
	HashMap<Integer, ArrayList<String>> tagSet;

	HashSet<Integer> removalPendingTimers;
	HashMap<Integer, Runnable> deleteTimerRunnables;

	Handler deleteTimerHandler;
	private int selectedItem;

	public TimerAdapter(Context context, ArrayList<Timer> timerList) {
		this.dbHelper = new DatabaseHelper(context);
		tagSet = new HashMap<>();

		deleteTimerHandler = new Handler();
		deleteTimerRunnables = new HashMap<>();

		removalPendingTimers = new HashSet<>();

		this.timerList = new SortedList<>(Timer.class, new SortedList.Callback<Timer>() {
			@Override
			public int compare(Timer o1, Timer o2) {
				return Timer.REMAINING_TIME_COMPARATOR.compare(o1, o2);
			}

			@Override
			public void onChanged(int position, int count) {
				notifyItemRangeChanged(position, count);
			}

			@Override
			public boolean areContentsTheSame(Timer oldItem, Timer newItem) {
//                boolean elapsed = oldItem.getElapsedTime() == newItem.getElapsedTime();
//                boolean missed = oldItem.isMissed() == newItem.isMissed();
//                boolean stopped = oldItem.isStopped() == newItem.isStopped();
//                boolean paused = oldItem.isPaused() == newItem.isPaused();
//
//                return elapsed || missed || stopped;

				return oldItem.getId() == newItem.getId();
			}

			@Override
			public boolean areItemsTheSame(Timer item1, Timer item2) {
				return item1.getId() == item2.getId();
			}

			@Override
			public void onInserted(int position, int count) {
				notifyItemRangeInserted(position, count);
			}

			@Override
			public void onRemoved(int position, int count) {
				notifyItemRangeRemoved(position, count);
			}

			@Override
			public void onMoved(int fromPosition, int toPosition) {
				notifyItemMoved(fromPosition, toPosition);
			}
		});

		this.timerList.addAll(timerList);
		this.context = context;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context context = parent.getContext();

		LayoutInflater li = LayoutInflater.from(context);
		View view = li.inflate(R.layout.layout_timer_list_item, parent, false);

		return new TimerHolder(view);
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder h, final int position) {
//		TimerHolder holder = (TimerHolder) h;
		bindTimerView((TimerHolder) h);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	public int getItemCount() {
		return timerList.size();
	}

	private void bindTimerView(final TimerHolder holder) {
		final int position = holder.getAdapterPosition();

		Timer timer = getItem(position);
		int id = (int) getItemId(position);

		if (tagSet.get(id) == null) tagSet.put(id, dbHelper.retrieveTags(id));

		ArrayList<String> strings = tagSet.get(id);
		String[] tags = strings.toArray(new String[strings.size()]);
		if (tags.length == 0) tags = new String[]{"No Tags"};

		if (removalPendingTimers.contains(id)) {
			holder.timerUndoDeleteView.setVisibility(View.VISIBLE);
			holder.timerBackground.setVisibility(View.INVISIBLE);

			holder.timerUndoDeleteView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					OnTimerDeleteCancelled(holder.getAdapterPosition());
				}
			});
		} else {
			holder.timerBackground.setVisibility(View.VISIBLE);
			holder.timerUndoDeleteView.setVisibility(View.INVISIBLE);
		}
		String name = timer.getName();

		final boolean paused = timer.isPaused();
		final boolean stopped = timer.isStopped();
		boolean missed = timer.isMissed();

		name += " " + position;
		if (name.isEmpty()) holder.timerName.setVisibility(View.GONE);
		else holder.timerName.setVisibility(View.VISIBLE);

		holder.timerName.setText(name);

		if (stopped) {
			holder.timerRemainingTime.setText(Timer.humanize(timer.getDuration()));
			holder.timerEndDateTime.setVisibility(View.GONE);
		} else {
			holder.timerRemainingTime.setText(timer.humanize());
			holder.timerEndDateTime.setText(timer.humanizeEndDateTime("ends on"));

			holder.timerEndDateTime.setVisibility(View.VISIBLE);
		}

		if (!timer.isNotify()) {
			holder.timerIndicatorNotify.setAlpha(.25f);
			holder.timerIndicatorSilent.setAlpha(.25f);
		} else if (timer.isSilent()) holder.timerIndicatorSilent.setAlpha(.25f);

		if (timer.isNotify()) {
			holder.timerIndicatorNotify.setAlpha(1f);
			if (!timer.isSilent()) holder.timerIndicatorSilent.setAlpha(1f);
		}

		if (!timer.isRepeat()) holder.timerIndicatorRepeat.setAlpha(.25f);
		else holder.timerIndicatorRepeat.setAlpha(1f);

		holder.timerTagContainer.removeAllViews();
		for (String tag : tags) {
			@SuppressLint("InflateParams") TextView tagView = (TextView) LayoutInflater.from(context)
					.inflate(R.layout.template_tag, null);
			tagView.setTag(tag);
			tagView.setText(tag);
			holder.timerTagContainer.addView(tagView);
		}

		int progress = timer.getProgress();

		ObjectAnimator animation = ObjectAnimator.ofInt(
				holder.timerProgress,
				"progress",
				progress
		);

		animation.setDuration(500);
		animation.setInterpolator(new DecelerateInterpolator());
		animation.start();

		holder.timerPauseToggle.setOnCheckedChangeListener(null);
		holder.timerPauseToggle.setChecked(stopped || paused);
		holder.timerPauseToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
				OnTimerToggled(holder.getAdapterPosition(), checked);
			}
		});

//        @ColorInt
//        int backgroundColor = sele
// ctedItem == id ?
//                ContextCompat.getColor(context, R.color.colorCardSelected) :
//                ContextCompat.getColor(context, R.color.white);
//
//        holder.timerBackground.setBackgroundColor(backgroundColor);

		if (missed) {
			int newColor = ContextCompat.getColor(context, R.color.colorCardHighlight);
			int oldColor = ContextCompat.getColor(context, R.color.white);

			ValueAnimator colorAnimation = ValueAnimator.ofObject(
					new ArgbEvaluator(),
					oldColor,
					newColor,
					oldColor
			);

			colorAnimation.setDuration(1000); // milliseconds
			colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animator) {
					holder.timerBackground.setBackgroundColor((int) animator.getAnimatedValue());
				}

			});
			colorAnimation.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					OnMissedTimerViewed(holder.getAdapterPosition());
				}

				@Override
				public void onAnimationCancel(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}
			});
			colorAnimation.start();
		}

		holder.timerBackground.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onItemClicked(v, holder.getAdapterPosition());
				Log.d("CardView", "Clicked");
			}
		});
	}

	public Timer getItem(int position) {
		if (position >= 0)
			return timerList.get(position);
		else return null;
	}

	public void OnTimerDeleteCancelled(int position) {
		int id = (int) getItemId(position);
		if (id == RecyclerView.NO_ID) return;

		removalPendingTimers.remove(id);
		Runnable deleteTimerRunnable = deleteTimerRunnables.get(id);
		deleteTimerHandler.removeCallbacks(deleteTimerRunnable);

		notifyItemChanged(position);
	}

	public void OnTimerToggled(int position, boolean isOn) {
		Timer timer = timerList.get(position);
		if (!isOn) {
			if (timer.isStopped())
				timer.startTimer();
			else
				timer.resumeTimer();
		} else timer.pauseTimer();

		changeTimer(timer, position);
	}

	public void OnMissedTimerViewed(int position) {
		if (position == -1) return;

		Timer timer = timerList.get(position);

		timer.setMissed(false);
		timer.saveTimer();

		changeTimer(timer, position);
	}

	protected void onItemClicked(View view, int position) {

	}

	public void changeTimer(Timer timer, int position) {
		timerList.updateItemAt(position, timer);
	}

	public Timer getTimerFromId(int id) {
		for (int i = 0; i < timerList.size(); i++) {
			Timer timer = timerList.get(i);
			if (timer.getId() == id) return timer;
		}
		return null;
	}

	public void insertTimer(Timer timer) {
		timerList.add(timer);
	}

	public void removeTimer(Timer timer) {
		timerList.remove(timer);
	}

	public Timer removeTimerAt(int position) {
		return timerList.removeItemAt(position);
	}

	public void onItemSwiped(int position) {
		Log.i("TimerAdapter", "Item Swiped Called");
		final Timer timer = getItem(position);
		final int id = timer.getId();

		notifyItemRemoved(position);
		notifyItemInserted(position);

		removalPendingTimers.add(id);

		Runnable deleteTimerRunnable = new Runnable() {
			@Override
			public void run() {
				timerList.remove(timer);
				removalPendingTimers.remove(id);
				timer.deleteTimer();
				onTimerDeleted();
			}
		};

		deleteTimerRunnables.put(id, deleteTimerRunnable);
		deleteTimerHandler.postDelayed(deleteTimerRunnable, TIMER_DELETE_DELAY);
	}

	protected void onTimerDeleted() {

	}

	public void setSelected(int selectedItem) {
		int oldItem = this.selectedItem;
		this.selectedItem = selectedItem;

		if (oldItem != -1) notifyItemChanged(getPositionFromId(oldItem));
		if (selectedItem != -1) notifyItemChanged(getPositionFromId(selectedItem));
	}

	public int getPositionFromId(int id) {
		for (int i = 0; i < timerList.size(); i++) {
			Timer timer = timerList.get(i);
			if (timer.getId() == id) return i;
		}
		return -1;
	}
}
