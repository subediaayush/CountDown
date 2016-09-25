package com.flyingbuff.countdown;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Aayush on 9/21/2016.
 */

public class HighlightTimerFragment extends Fragment {
	WaveViewPlus timerProgress;
	TextView timerName;
	TextView timerDuration;
	private int initProgress;
	private boolean updatePending = false;
	private ObjectAnimator updateProgress;
	private Timer mTimer;

	private Boolean miniMode;
	private ImageView timerPlayPause;
	private ImageView timerNotify;
	private ImageView timerTone;
	private ImageView timerRepeat;
	private View timerInfoContainer;
	private ObjectAnimator playPauseAnimator;

	private HighlightTimerListener listener;
	private ImageView timerDelete;

	private BroadcastReceiver alarmReceiver;

	public HighlightTimerFragment() {
		initProgress = 0;

	}

	public static HighlightTimerFragment newInstance(Boolean miniMode) {

		Bundle args = new Bundle();

		HighlightTimerFragment fragment = new HighlightTimerFragment();
		fragment.miniMode = miniMode;

		fragment.setArguments(args);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_highlight_timer, container, false);

		timerProgress = (WaveViewPlus) view.findViewById(R.id.highlight_timer_progress);
		timerName = (TextView) view.findViewById(R.id.highlight_timer_name);
		timerDuration = (TextView) view.findViewById(R.id.highlight_timer_duration);

		timerPlayPause = (ImageView) view.findViewById(R.id.highlight_play_pause);
		timerNotify = (ImageView) view.findViewById(R.id.highlight_notify);
		timerTone = (ImageView) view.findViewById(R.id.highlight_tone);
		timerRepeat = (ImageView) view.findViewById(R.id.highlight_repeat);

		timerDelete = (ImageView) view.findViewById(R.id.highlight_delete);

		timerInfoContainer = view.findViewById(R.id.highlight_info_container);

		playPauseAnimator = ObjectAnimator.ofFloat(
				timerPlayPause,
				"alpha",
				1, 0
		);

		playPauseAnimator.setDuration(600);
		playPauseAnimator.setRepeatCount(ValueAnimator.INFINITE);
		playPauseAnimator.setRepeatMode(ValueAnimator.REVERSE);

		timerPlayPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mTimer.isStopped() || mTimer.isMissed()) mTimer.startTimer();
				else if (mTimer.isPaused()) mTimer.resumeTimer();
				else mTimer.pauseTimer();

				updateData();
			}
		});

		timerDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog adb = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog)
						.setTitle("Confirm")
						.setMessage("Are you sure you want to delete " + mTimer.getName())
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								mTimer.deleteTimer();

								if (listener != null) listener.onTimerDeleted(mTimer);

							}
						}).setNegativeButton("No", null)
						.show();
			}
		});

		timerProgress.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) listener.onTimerClicked(mTimer);
			}
		});

		alarmReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int timerId = intent.getIntExtra(Countdown.KEY_TIMER_TAG, -1);

				if (timerId == mTimer.getId()) {
					DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
					mTimer = databaseHelper.loadTimer(timerId);
					updateData();
				}
			}
		};

		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (updatePending) {
			updatePending = false;
			updateData();
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		IntentFilter filter = new IntentFilter();
		filter.addAction(AlarmReceiver.ACTION_SILENT_ALARM);
		filter.addAction(AlarmReceiver.ACTION_NOISY_ALARM);

		LocalBroadcastManager.getInstance(getContext())
				.registerReceiver(alarmReceiver, filter);
	}

	@Override
	public void onStop() {
		super.onStop();

		LocalBroadcastManager.getInstance(getContext())
				.unregisterReceiver(alarmReceiver);
	}

	public void setInitialProgress(int progress) {
		this.initProgress = progress;
	}

	public void setTimer(final Timer timer) {
		if (mTimer != null && mTimer.getId() != timer.getId()) {
			timerInfoContainer.animate().setDuration(300)
					.alpha(0)
					.withEndAction(new Runnable() {
						@Override
						public void run() {
							mTimer = timer;
							updateData();

							timerInfoContainer.animate().setDuration(300)
									.alpha(1);
						}
					});
		} else {
			mTimer = timer;
			updateData();
		}
	}

	public void updateData() {
		if (getView() == null) {
			updatePending = true;
			return;
		}

		timerName.setText(mTimer.getName());
		if (mTimer.isStopped() || mTimer.isMissed()) {
			timerDuration.setText(Timer.humanize(mTimer.getDuration()));
		} else {
			timerDuration.setText(Timer.humanize(mTimer.getRemainingTime()));
		}


		if (mTimer.isStopped() || mTimer.isMissed() || mTimer.isPaused()) {
			timerPlayPause.setImageResource(R.drawable.ic_play);
			if (playPauseAnimator.isRunning()) playPauseAnimator.end();
			timerPlayPause.setAlpha(1f);
		} else {
			timerPlayPause.setImageResource(R.drawable.ic_pause);
			if (!playPauseAnimator.isRunning()) playPauseAnimator.start();

		}

		if (mTimer.isNotify() && miniMode) {
			timerNotify.setVisibility(View.VISIBLE);
			if (!mTimer.isSilent()) timerTone.setVisibility(View.VISIBLE);
			else timerTone.setVisibility(View.GONE);
		} else {
			timerNotify.setVisibility(View.GONE);
			timerTone.setVisibility(View.GONE);
		}

		if (mTimer.isRepeat() && miniMode) {
			timerRepeat.setVisibility(View.VISIBLE);
		} else {
			timerRepeat.setVisibility(View.GONE);
		}

		animateProgress();
	}

	private void animateProgress() {

		updateProgress = ObjectAnimator.ofInt(
				timerProgress,
				"progress",
				100 - mTimer.getProgress()
		);

		updateProgress.setDuration(700);
		updateProgress.setInterpolator(new AccelerateDecelerateInterpolator());
		updateProgress.start();
	}

	public void setTimerListener(HighlightTimerListener listener) {
		this.listener = listener;
	}

	public interface HighlightTimerListener {
		void onTimerDeleted(Timer timer);

		void onTimerClicked(Timer timer);
	}
}
