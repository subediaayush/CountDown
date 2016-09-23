package com.flyingbuff.countdown;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.transition.Transition;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewFlipper;

import com.google.android.flexbox.FlexboxLayout;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;

public class TimerDetailActivity extends AppCompatActivity {

	TextView addEndTime;
	TextView addEndDate;
	TextView addEndTimer;
	TextView metaInfo;
	EditText addName;
	Switch addNotify;
	TextView addTone;
	Switch addAutoDelete;
	View timerTagPlaceholder;
	FlexboxLayout addTimerTag;
	DatePickerDialog datePicker;

	WaveViewPlus timerProgress;

	FloatingActionButton fab;

	TextView detailStart;
	TextView detailEnd;
	TextView detailNotify;
	TextView detailRepeat;
	FlexboxLayout detailTimerTag;
	Bundle args;
	private boolean isShowingTimer = true;
	private boolean inEditMode = false;
	private DatabaseHelper databaseHelper;

	private Timer timer;
	private ViewFlipper optionsPanel;
	private boolean dateTimeChanged = false;
	private boolean tagListChanged = false;

	Handler uiHandler = new Handler();
	Runnable uiRunnable = new Runnable() {
		@Override
		public void run() {
			if (!(timer.isMissed() || timer.isPaused() || timer.isStopped()))
				highlightTimer.updateData();
			uiHandler.postDelayed(this, 1000);
		}
	};
	private HighlightTimerFragment highlightTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer_detail);

		setupWindowTransition();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		timer = getIntent().getExtras().getParcelable(Countdown.KEY_TIMER);

		if (null == timer) {
			finish();
			return;
		}

		getSupportActionBar().setTitle(timer.getName());

		highlightTimer = HighlightTimerFragment.newInstance(false);
		highlightTimer.setTimerListener(new HighlightTimerFragment.HighlightTimerListener() {
			@Override
			public void onTimerDeleted(Timer timer) {
				setResult(RESULT_CANCELED, getIntent());
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

		optionsPanel = (ViewFlipper) findViewById(R.id.timer_options_panel);

		setupSummary();

		setupEditView();
		setupDetailView();

		optionsPanel.setDisplayedChild(0);
		fab.setImageResource(R.drawable.ic_fab_edit);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = TimerDetailActivity.this;
//				optionsPanel.startFlipping();
				if (inEditMode) {
//					optionsPanel.setInAnimation(context, R.anim.anim_slide_in_left);
//					optionsPanel.setOutAnimation(context, R.anim.anim_slide_out_right);
//					fab.setImageResource(R.drawable.ic_fab_edit);
					updateTimer();
//					setupDetailView();
					setResult(RESULT_OK, getIntent());

					finishAfterTransition();
				} else {
					optionsPanel.setInAnimation(context, R.anim.anim_slide_in_right);
					optionsPanel.setOutAnimation(context, R.anim.anim_slide_out_left);
					fab.setImageResource(R.drawable.ic_fab_save);
					setupEditView();
					optionsPanel.showNext();
				}
				inEditMode = !inEditMode;
			}
		});

	}

	private void setupSummary() {

	}

	private void setupDetailView() {
		detailStart = (TextView) findViewById(R.id.timer_start);
		detailEnd = (TextView) findViewById(R.id.timer_end);
		detailNotify = (TextView) findViewById(R.id.timer_notify);
		detailRepeat = (TextView) findViewById(R.id.timer_repeat);
		detailTimerTag = (FlexboxLayout) findViewById(R.id.timer_tags);

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

		updateTagContainer(detailTimerTag, tags);
	}


	private void setupWindowTransition() {
		Transition transition = new Explode();

		transition.excludeTarget(android.R.id.statusBarBackground, true);
		transition.excludeTarget(android.R.id.navigationBarBackground, true);

//		transition.excludeChildren(R.id.highlight_timer_background_progress, true);

		getWindow().setEnterTransition(transition);
	}

	private void updateTimer() {
		int id = timer.getId();
		String name = args.getString(Countdown.KEY_TIMER_NAME);
		long end = args.getLong(Countdown.KEY_TIMER_END);
		Boolean notify = args.getBoolean(Countdown.KEY_TIMER_NOTIFY);
		Uri tone = args.getParcelable(Countdown.KEY_TIMER_TONE);
		Boolean repeat = args.getBoolean(Countdown.KEY_TIMER_REPEAT);
		ArrayList<String> tags = args.getStringArrayList(Countdown.KEY_TIMER_TAG);

		if (tone == null) tone = Uri.EMPTY;
		if (tags == null) tags = new ArrayList<>();

		long endDate = DateTime.now().plus(end).getMillis();

//		Timer timer = new Timer(name, endDate, repeat, notify, notify && tone.equals(Uri.EMPTY));
		ContentValues args = new ContentValues();
		if (dateTimeChanged) {
			args.put(Countdown.COLUMN_DURATION, end);
			args.put(Countdown.COLUMN_ELAPSED, 0);

			long now = DateTime.now().getMillis();
			args.put(Countdown.COLUMN_PAUSED_AT, now);
			args.put(Countdown.COLUMN_RESUMED_AT, now);
		}

		args.put(Countdown.COLUMN_NAME, name);
		args.put(Countdown.COLUMN_NOTIFY, notify);
		args.put(Countdown.COLUMN_SILENT, notify && tone.equals(Uri.EMPTY));
		args.put(Countdown.COLUMN_REPEAT, repeat);

		databaseHelper.editTimer(id, args);

		if (tagListChanged) {
			timer.clearTags(this);
			timer.setTags(this, tags);
		}

		timer = databaseHelper.loadTimer(id);

		getIntent().putExtra(Countdown.KEY_TIMER, timer);
		getIntent().putExtra(Countdown.KEY_TIMER_EDITED, true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (inEditMode) {
					optionsPanel.setInAnimation(this, R.anim.anim_slide_in_left);
					optionsPanel.setOutAnimation(this, R.anim.anim_slide_out_right);
					optionsPanel.showNext();

					inEditMode = !inEditMode;

					fab.setImageResource(R.drawable.ic_fab_edit);
				} else {
					updateTimer();
					setResult(RESULT_OK, getIntent());
					finishAfterTransition();
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == Countdown.ACTIVITY_ACTION_RINGTONE &&
				resultCode == Activity.RESULT_OK) {
			Log.i("Add Timer", "Ringtone obtained");
			Bundle data = intent.getExtras();
			Uri uri = data.getParcelable(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

			if (uri != null) {
				args.putParcelable(Countdown.KEY_TIMER_TONE, uri);

				Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
				String title = ringtone.getTitle(this);
				Log.i("AddTimerFragment", title);
			} else {
				args.putParcelable(Countdown.KEY_TIMER_TONE, Uri.EMPTY);
			}
			updateRingtoneContainer();
		}
		if (requestCode == Countdown.ACTIVITY_TAGS && resultCode == Activity.RESULT_OK) {
			Bundle data = intent.getExtras();
			ArrayList<String> tags = data.getStringArrayList(Countdown.KEY_TAG);

			if (tags != null) args.putStringArrayList(Countdown.KEY_TIMER_TAG, tags);
			tagListChanged = true;
			updateTagContainer();
		}
	}

	@Override
	public void onBackPressed() {
		if (inEditMode) {
			optionsPanel.setInAnimation(this, R.anim.anim_slide_in_left);
			optionsPanel.setOutAnimation(this, R.anim.anim_slide_out_right);
			optionsPanel.showNext();

			inEditMode = !inEditMode;

			fab.setImageResource(R.drawable.ic_fab_edit);
		} else {
			super.onBackPressed();

			updateTimer();

			setResult(RESULT_OK, getIntent());

			finishAfterTransition();
		}
	}

	private void updateSummary() {
		long duration = args.getLong(Countdown.KEY_TIMER_END, Countdown.MILLIS_IN_HOUR);
		StringBuilder summary = new StringBuilder();

		if (isShowingTimer) {
			DateTime endDateTime = DateTime.now().plus(duration);

			LocalDate endDate = endDateTime.toLocalDate();
			LocalDate today = LocalDate.now();

			summary.append("at ");
			summary.append(endDateTime.toString("hh:mm a"));
			if (endDate.isAfter(today.plusDays(1))) {
				summary.append(", ");
				if (endDate.isAfter(today.plusDays(5)))
					summary.append(endDate.toString("dd MMM"));
				else
					summary.append(endDate.toString("EEEE"));
				if (endDate.getYear() != today.getYear())
					summary.append(", ")
							.append(endDate.toString("yyyy"));
			} else if (endDate.isAfter(today))
				summary.append(" tommorow");
			metaInfo.setText(summary.toString());
		} else {
			summary.append("after ");

			summary.append(Timer.humanize(duration));

			metaInfo.setText(summary.toString());
		}
	}

	private void initViews() {
		updateEndTimeContainers();

		addName.setText(args.getString(Countdown.KEY_TIMER_NAME));
		addNotify.setChecked(args.getBoolean(Countdown.KEY_TIMER_NOTIFY));
		addAutoDelete.setChecked(args.getBoolean(Countdown.KEY_TIMER_REPEAT));

		updateRingtoneContainer();
		updateTagContainer();
		refreshViews();
	}

	private void refreshViews() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				updateEndTimeContainers();
			}
		}, 3000);
	}

	private void initArguments() {
		if (null == args) args = new Bundle();

		if (!args.containsKey(Countdown.KEY_TIMER_END)) {
			long remaining = timer.getRemainingTime();
			if (remaining <= 0) remaining = Countdown.MILLIS_IN_HOUR;

			args.putLong(Countdown.KEY_TIMER_END, remaining);
		}

		if (!args.containsKey(Countdown.KEY_TIMER_NAME))
			args.putString(Countdown.KEY_TIMER_NAME, timer.getName());

		if (!args.containsKey(Countdown.KEY_TIMER_NOTIFY))
			args.putBoolean(Countdown.KEY_TIMER_NOTIFY, timer.isNotify());

		if (!args.containsKey(Countdown.KEY_TIMER_TONE)) {
			args.putParcelable(Countdown.KEY_TIMER_TONE, timer.getTone(this));
		}

		if (!args.containsKey(Countdown.KEY_TIMER_TAG))
			args.putStringArrayList(Countdown.KEY_TIMER_TAG, timer.getTags(this));

		if (!args.containsKey(Countdown.KEY_TIMER_REPEAT))
			args.putBoolean(Countdown.KEY_TIMER_REPEAT, timer.isRepeat());
	}

	private void updateEndTimeContainers() {
		long timerDuration = args.getLong(Countdown.KEY_TIMER_END, Countdown.MILLIS_IN_HOUR);

		DateTime endDateTime = DateTime.now().plus(timerDuration);

		String endTime = endDateTime.toString("hh : mm a");
		String endDate = endDateTime.toString("MMM dd, yyyy");

		String endTimer = Timer.humanize(timerDuration);

		addEndTime.setText(endTime);
		addEndDate.setText(endDate);
		addEndTimer.setText(endTimer);

		updateSummary();
	}

	private void toggleVisibility(View first, View second) {
		final View visible, invisible;
		if (first.getVisibility() == View.VISIBLE) {
			visible = first;
			invisible = second;
		} else {
			visible = second;
			invisible = first;
		}

		invisible.setVisibility(View.VISIBLE);
		invisible.setAlpha(0);

		Interpolator d = new DecelerateInterpolator();
		Interpolator a = new AccelerateInterpolator();
		int duration = 200;

		int initialHeight = visible.getHeight();
		int finalHeight = invisible.getHeight();

		visible.animate().setDuration(duration).setInterpolator(a)
				.alpha(0).withEndAction(new Runnable() {
			@Override
			public void run() {
				visible.setVisibility(View.GONE);
			}
		});
		invisible.animate().setDuration(duration).setInterpolator(d)
				.alpha(1);
	}

	private void setupEditView() {
		final View toggleInputGroup = findViewById(R.id.group_input_toggle);

		final View groupDateTime = findViewById(R.id.group_time_date);
		addEndTime = (TextView) findViewById(R.id.add_end_time);
		addEndDate = (TextView) findViewById(R.id.add_end_date);

		final View groupTimer = findViewById(R.id.group_timer);
		addEndTimer = (TextView) findViewById(R.id.add_end_timer);

		metaInfo = (TextView) findViewById(R.id.input_detail);

		addName = (EditText) findViewById(R.id.add_name);
		addNotify = (Switch) findViewById(R.id.add_notify);
		addTone = (TextView) findViewById(R.id.add_tone);
		addAutoDelete = (Switch) findViewById(R.id.add_auto_repeat);

		addTimerTag = (FlexboxLayout) findViewById(R.id.add_tags_container);

		toggleInputGroup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleVisibility(groupDateTime, groupTimer);
				isShowingTimer = !isShowingTimer;
				updateSummary();
			}
		});

		initArguments();
		initViews();

		addEndTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				long duration = args.getLong(
						Countdown.KEY_TIMER_END,
						Countdown.MILLIS_IN_HOUR
				);

				final DateTime currentTime = DateTime.now();
				final DateTime endTime = currentTime.plus(duration);

				int hour = endTime.getHourOfDay(),
						min = endTime.getMinuteOfHour();

				new TimePickerDialog(
						TimerDetailActivity.this,
						new TimePickerDialog.OnTimeSetListener() {
							@Override
							public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
								long obtained = new LocalTime(hourOfDay, minute, currentTime.getSecondOfMinute()).getMillisOfDay();
								long now = currentTime.getMillisOfDay();

								long duration = Countdown.normalize(obtained) - Countdown.normalize(now);

								if (duration < 0)
									duration += Countdown.MILLIS_IN_DAY;

								if (duration == 0)
									return;

								args.putLong(Countdown.KEY_TIMER_END, duration);
								updateEndTimeContainers();
								dateTimeChanged = true;
							}
						},
						hour,
						min,
						false
				).show();
			}
		});

		addEndDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				long duration = args.getLong(
						Countdown.KEY_TIMER_END,
						Countdown.MILLIS_IN_HOUR
				);

				final DateTime currentTime = DateTime.now();
				final DateTime endTime = currentTime.plus(duration);

				int year = endTime.getYear(),
						month = endTime.getMonthOfYear() - 1,
						day = endTime.getDayOfMonth();

				datePicker = new DatePickerDialog(
						TimerDetailActivity.this,
						new DatePickerDialog.OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
								LocalDate obtainedDate = new LocalDate(year, monthOfYear + 1, dayOfMonth);

								long obtained = endTime.withDate(obtainedDate).getMillis();
								long now = currentTime.getMillis();

								long duration = obtained - now;

								if (duration < 0)
									new AlertDialog.Builder(TimerDetailActivity.this)
											.setTitle("Given date already passed")
											.setMessage("Want to try again?")
											.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													datePicker.show();
												}
											})
											.setNegativeButton("No", null)
											.setNeutralButton("Use Tommorow", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													LocalDate tomorrow = LocalDate.now().plusDays(1);

													long obtained = endTime.withDate(tomorrow).getMillis();
													long now = currentTime.getMillis();

													long duration = obtained - now;

													args.putLong(Countdown.KEY_TIMER_END, duration);
													dateTimeChanged = true;
													updateEndTimeContainers();
												}
											}).show();

								else if (duration > 0) {
									args.putLong(Countdown.KEY_TIMER_END, duration);
									dateTimeChanged = true;
								}
								updateEndTimeContainers();
							}
						}, year
						, month
						, day
				);
				datePicker.show();
			}
		});

		addEndTimer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				long duration = args.getLong(
						Countdown.KEY_TIMER_END,
						Countdown.MILLIS_IN_HOUR
				);

				new TimeDurationPickerDialog(
						TimerDetailActivity.this,
						new TimeDurationPickerDialog.OnDurationSetListener() {
							@Override
							public void onDurationSet(long duration) {
								if (duration == 0) return;

								args.putLong(Countdown.KEY_TIMER_END, duration);
								dateTimeChanged = true;
								updateEndTimeContainers();
							}
						},
						duration
				).show();

			}
		});

		addName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					v.clearFocus();
					InputMethodManager imm = (InputMethodManager) TimerDetailActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});

		addName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String obtainedText = s.toString();
				args.putString(Countdown.KEY_TIMER_NAME, obtainedText);
			}
		});

		addNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				addTone.setEnabled(isChecked);
				args.putBoolean(Countdown.KEY_TIMER_NOTIFY, isChecked);
			}
		});

		addTone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Uri tone = args.getParcelable(Countdown.KEY_TIMER_TONE);

				Intent toneChooser = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
				toneChooser.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
				toneChooser.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, tone);

				startActivityForResult(toneChooser, Countdown.ACTIVITY_ACTION_RINGTONE);
				// show chooser
			}
		});

		addAutoDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				args.putBoolean(Countdown.KEY_TIMER_REPEAT, isChecked);
			}
		});

		findViewById(R.id.add_tags).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent searchIntent = new Intent(TimerDetailActivity.this, SearchTagActivity.class);
				searchIntent.putExtra(Countdown.KEY_TAG, args.getStringArrayList(Countdown.KEY_TIMER_TAG));
				startActivityForResult(searchIntent, Countdown.ACTIVITY_TAGS);
			}
		});
	}

	private void updateRingtoneContainer() {
		Uri uri = args.getParcelable(Countdown.KEY_TIMER_TONE);

		Uri defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
		if (uri == null || uri.equals(Settings.System.DEFAULT_ALARM_ALERT_URI) ||
				uri.equals(defaultUri)) {
			addTone.setText("Default");
		} else if (uri.equals(Uri.EMPTY)) {
			addTone.setText("Silent");
		} else {
			Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
			String title = ringtone.getTitle(this);
			addTone.setText(title);
		}


	}

	private void updateTagContainer(FlexboxLayout tagContainer, ArrayList<String> tags) {
		Context context = this;

		if (tags == null) tags = new ArrayList<>();

		tagContainer.removeAllViews();

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

			tagContainer.addView(tagView);
		}

		tagContainer.invalidate();

	}

	private void updateTagContainer() {
		ArrayList<String> tags = args.getStringArrayList(Countdown.KEY_TIMER_TAG);

		updateTagContainer(addTimerTag, tags);
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
}
