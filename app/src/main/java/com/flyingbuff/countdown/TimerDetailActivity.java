package com.flyingbuff.countdown;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class TimerDetailActivity extends AppCompatActivity {

	WaveViewPlus timerProgress;

	FloatingActionButton fab;

	TextView detailStart;
	TextView detailEnd;
	TextView detailNotify;
	TextView detailRepeat;
	FlexboxLayout detailTimerTag;

	Bundle args;
	Handler uiHandler = new Handler();
	private boolean inEditMode = false;
	private DatabaseHelper databaseHelper;
	private Timer timer;
	private HighlightTimerFragment highlightTimer;
	Runnable uiRunnable = new Runnable() {
		@Override
		public void run() {
			if (!(timer.isMissed() || timer.isPaused() || timer.isStopped()))
				highlightTimer.updateData();
			uiHandler.postDelayed(this, 1000);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer_detail);

		setupEditTransition();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		timer = getIntent().getExtras().getParcelable(Countdown.KEY_TIMER);

		if (null == timer) {
			finishAfterTransition();
			return;
		}

		getSupportActionBar().setTitle(timer.getName());

		detailStart = (TextView) findViewById(R.id.timer_start);
		detailEnd = (TextView) findViewById(R.id.timer_end);
		detailNotify = (TextView) findViewById(R.id.timer_notify);
		detailRepeat = (TextView) findViewById(R.id.timer_repeat);
		detailTimerTag = (FlexboxLayout) findViewById(R.id.timer_tags);

		highlightTimer = HighlightTimerFragment.newInstance(false);
		highlightTimer.setTimerListener(new HighlightTimerFragment.HighlightTimerListener() {
			@Override
			public void onTimerDeleted(Timer timer) {
				getIntent().putExtra(Countdown.KEY_TIMER_DELETED, true);
				setResult(RESULT_OK, getIntent());
				finishAfterTransition();
			}

			@Override
			public void onTimerClicked(Timer timer) {

			}
		});

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.highlight_timer_container, highlightTimer)
				.commit();

		highlightTimer.setInitialProgress(
				(int) ((100 / 3f) + getIntent().getExtras().getInt(Countdown.KEY_INITIAL_PROGRESS))
		);
		highlightTimer.setTimer(timer);

		databaseHelper = new DatabaseHelper(this);

		fab = (FloatingActionButton) findViewById(R.id.fab);

		setupDetailView();

		fab.setImageResource(R.drawable.ic_fab_edit);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Slide slide = new Slide();
				slide.setSlideEdge(Gravity.START);
				getWindow().setExitTransition(slide);

				slide.excludeTarget(android.R.id.statusBarBackground, true);
				slide.excludeTarget(android.R.id.navigationBarBackground, true);

				Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
						TimerDetailActivity.this,
						fab,
						"fab"
				).toBundle();

				Intent editIntent = new Intent(TimerDetailActivity.this, EditTimerActivity.class);
				editIntent.putExtra(Countdown.KEY_TIMER, timer);
				startActivityForResult(editIntent, Countdown.ACTIVITY_EDIT_TIMER, bundle);
			}
		});
	}

	private void setupDetailView() {
		detailStart.setText(String.format(
				"Started on %s",
				Timer.humanizeDateTime(timer.getStart())
		));

		if (timer.isStopped() || timer.isMissed()) {
			detailEnd.setText(String.format(
					"Ended on %s",
					Timer.humanizeDateTime(DateTime.now().getMillis() + timer.getRemainingTime())
			));
		} else {
			detailEnd.setText(String.format(
					"Ends on %s",
					Timer.humanizeDateTime(DateTime.now().getMillis() + timer.getRemainingTime())
			));
		}

		if (timer.isNotify()) {
			if (timer.isSilent())
				detailNotify.setText("Silent Notification");
			else {
				Uri tone = timer.getTone(this);
				Ringtone r = RingtoneManager.getRingtone(this, tone);
				if (r == null)
					r = RingtoneManager.getRingtone(this, Settings.System.DEFAULT_ALARM_ALERT_URI);

				detailNotify.setText(r.getTitle(this));
			}
		} else {
			detailNotify.setText("No Notification");
		}

		if (timer.isRepeat()) detailRepeat.setText("Repeats automatically");
		else detailRepeat.setText("Doesnot repeat");

		ArrayList<String> tags = timer.getTags(this);
		if (tags.isEmpty()) tags.add("No tags");

		updateTagContainer(tags);
	}


	private void setupEditTransition() {
		Transition transition = new Explode();

		transition.excludeTarget(android.R.id.statusBarBackground, true);
		transition.excludeTarget(android.R.id.navigationBarBackground, true);

//		transition.excludeChildren(R.id.highlight_timer_background_progress, true);

		getWindow().setEnterTransition(transition);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Boolean edited = getIntent().getBooleanExtra(Countdown.KEY_TIMER_EDITED, false);

				if (edited) {
					int id = ((Timer) getIntent().getParcelableExtra(Countdown.KEY_TIMER)).getId();
					Intent result = new Intent();
					result.putExtra(Countdown.KEY_TIMER_ID, id);
					setResult(RESULT_OK, result);
					finishAfterTransition();

					return true;
				}
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
			case Countdown.ACTIVITY_EDIT_TIMER:
				if (resultCode == RESULT_OK) {
					timer = intent.getExtras().getParcelable(Countdown.KEY_TIMER);
					getIntent().putExtra(Countdown.KEY_TIMER, timer);
					getIntent().putExtra(Countdown.KEY_TIMER_EDITED, true);

					highlightTimer.setTimer(timer);

					setupDetailView();
				}
		}

	}

	@Override
	public void onBackPressed() {
		Boolean edited = getIntent().getBooleanExtra(Countdown.KEY_TIMER_EDITED, false);

		if (edited) {
			int id = ((Timer) getIntent().getParcelableExtra(Countdown.KEY_TIMER)).getId();
			Intent result = new Intent();
			result.putExtra(Countdown.KEY_TIMER_ID, id);
			setResult(RESULT_OK, result);
			finishAfterTransition();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		uiHandler.removeCallbacks(uiRunnable);
	}

	@Override
	protected void onResume() {
		super.onResume();

		uiHandler.post(uiRunnable);
	}

	private void updateTagContainer(ArrayList<String> tags) {
		Context context = this;

		if (tags == null) tags = new ArrayList<>();

		detailTimerTag.removeAllViews();

		for (String tag : tags) {
			View tagView = LayoutInflater.from(context)
					.inflate(R.layout.template_tag_large, null);

			tagView.setLayoutParams(new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					getResources().getDimensionPixelSize(R.dimen.dimen_group_input_height))
			);

			TextView tagText = (TextView) tagView.findViewById(R.id.tag);
			tagText.setTag(tag);
			tagText.setText(tag);

			detailTimerTag.addView(tagView);
		}
	}


}
