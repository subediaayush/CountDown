package com.flyingbuff.countdown;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

import com.google.android.flexbox.FlexboxLayout;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;

/**
 * Created by Aayush on 8/9/2016.
 */
public class AddTimerBottomSheet extends BottomSheetDialogFragment {

	TextView addEndTime;
	TextView addEndDate;
	TextView addEndTimer;
	TextView metaInfo;
	EditText addName;
	Switch addNotify;
	TextView addTone;
	Switch addAutoRepeat;
	View timerTagPlaceholder;
	FlexboxLayout timerTagContaier;
	DatePickerDialog datePicker;
	private boolean isShowingTimer = true;

	private OnAddTimerDialogListener onDismissListener;

	private BottomSheetBehavior behavior;
	private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

		@Override
		public void onStateChanged(@NonNull View bottomSheet, int newState) {
			Log.i("AddTimerFragment", "State " + newState);
			if (newState == BottomSheetBehavior.STATE_HIDDEN) {
				dismiss();
			}
		}

		@Override
		public void onSlide(@NonNull View bottomSheet, float slideOffset) {
		}
	};

	public static AddTimerBottomSheet newInstance() {
		AddTimerBottomSheet fragment = new AddTimerBottomSheet();
		fragment.setArguments(new Bundle());

		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

		View contentView = View.inflate(getContext(), R.layout.layout_add_timer, null);

		setupView(contentView);

		View doneButton = contentView.findViewById(R.id.done_button);
		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addNewTimer();
				dismiss();
			}
		});

		dialog.setContentView(contentView);

		behavior = BottomSheetBehavior.from((View) contentView.getParent());
		behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback);

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			}
		});

		return dialog;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);

		if (onDismissListener != null) onDismissListener.onDismiss(dialog);
	}

	public void setOnDismissListener(OnAddTimerDialogListener onAddTimerDialogListener) {
		this.onDismissListener = onAddTimerDialogListener;
	}

	private void addNewTimer() {
		Bundle args = getArguments();

		int id = args.getInt(Countdown.KEY_TIMER_ID, -1);
		String name = args.getString(Countdown.KEY_TIMER_NAME);
		long end = args.getLong(Countdown.KEY_TIMER_END);
		Boolean notify = args.getBoolean(Countdown.KEY_TIMER_NOTIFY);
		Uri tone = args.getParcelable(Countdown.KEY_TIMER_TONE);
		Boolean autoDelete = args.getBoolean(Countdown.KEY_TIMER_REPEAT);
		ArrayList<String> tags = args.getStringArrayList(Countdown.KEY_TIMER_TAG);

		if (tone == null) tone = Uri.EMPTY;
		if (tags == null) tags = new ArrayList<>();

		long endDate = DateTime.now().plus(end).getMillis();

		if (name == null || name.isEmpty()) name = "Timer";

		Timer timer = new Timer(name, endDate, autoDelete, notify, notify && tone.equals(Uri.EMPTY));

		if (onDismissListener != null) onDismissListener.onAddTimer(
				getDialog(),
				timer,
				tone,
				tags
		);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		Bundle args = getArguments();

		if (requestCode == Countdown.ACTIVITY_ACTION_RINGTONE &&
				resultCode == Activity.RESULT_OK) {
			Log.i("Add Timer", "Ringtone obtained");
			Bundle data = intent.getExtras();
			Uri uri = data.getParcelable(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

			if (uri != null) {
				args.putParcelable(Countdown.KEY_TIMER_TONE, uri);

				Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
				String title = ringtone.getTitle(getContext());
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
			updateTagContainer();
		}
	}

	private void updateSummary() {
		long duration = getArguments().getLong(Countdown.KEY_TIMER_END, Countdown.MILLIS_IN_HOUR);
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
		Bundle args = getArguments();

		updateEndTimeContainers();

		addName.setText(args.getString(Countdown.KEY_TIMER_NAME));
		addNotify.setChecked(args.getBoolean(Countdown.KEY_TIMER_NOTIFY));
		addAutoRepeat.setChecked(args.getBoolean(Countdown.KEY_TIMER_REPEAT));

		addTone.setEnabled(
				args.getBoolean(Countdown.KEY_TIMER_NOTIFY)
		);

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
		Bundle args = getArguments();

		if (!args.containsKey(Countdown.KEY_TIMER_ADD))
			args.putBoolean(Countdown.KEY_TIMER_ADD, true);

		if (!args.containsKey(Countdown.KEY_TIMER_END)) {
			long duration;
			if (BuildConfig.DEBUG)
				duration = Countdown.MILLIS_IN_SECOND * 5;
			else
				duration = Countdown.MILLIS_IN_HOUR;

			args.putLong(Countdown.KEY_TIMER_END, duration);
		}

		if (!args.containsKey(Countdown.KEY_TIMER_NAME))
			args.putString(Countdown.KEY_TIMER_NAME, "");

		if (!args.containsKey(Countdown.KEY_TIMER_NOTIFY))
			args.putBoolean(Countdown.KEY_TIMER_NOTIFY, true);

		if (!args.containsKey(Countdown.KEY_TIMER_TONE)) {
			boolean notify = args.getBoolean(Countdown.KEY_TIMER_NOTIFY);
			args.putParcelable(Countdown.KEY_TIMER_TONE, Settings.System.DEFAULT_ALARM_ALERT_URI);
		}

		if (!args.containsKey(Countdown.KEY_TIMER_TAG))
			args.putStringArrayList(Countdown.KEY_TIMER_TAG, new ArrayList<String>());

		if (!args.containsKey(Countdown.KEY_TIMER_REPEAT))
			args.putBoolean(Countdown.KEY_TIMER_REPEAT, false);


	}

	private void updateEndTimeContainers() {
		long timerDuration = getArguments().getLong(Countdown.KEY_TIMER_END, Countdown.MILLIS_IN_HOUR);

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

	private void setupView(View contentView) {
		final View toggleInputGroup = contentView.findViewById(R.id.group_input_toggle);

		final View groupDateTime = contentView.findViewById(R.id.group_time_date);
		addEndTime = (TextView) contentView.findViewById(R.id.add_end_time);
		addEndDate = (TextView) contentView.findViewById(R.id.add_end_date);

		final View groupTimer = contentView.findViewById(R.id.group_timer);
		addEndTimer = (TextView) contentView.findViewById(R.id.add_end_timer);

		metaInfo = (TextView) contentView.findViewById(R.id.input_detail);

		addName = (EditText) contentView.findViewById(R.id.add_name);
		addNotify = (Switch) contentView.findViewById(R.id.add_notify);
		addTone = (TextView) contentView.findViewById(R.id.add_tone);
		addAutoRepeat = (Switch) contentView.findViewById(R.id.add_auto_repeat);

		timerTagContaier = (FlexboxLayout) contentView.findViewById(R.id.add_tags_container);

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
				final Bundle args = getArguments();

				long duration = args.getLong(
						Countdown.KEY_TIMER_END,
						Countdown.MILLIS_IN_HOUR
				);

				final DateTime currentTime = DateTime.now();
				final DateTime endTime = currentTime.plus(duration);

				int hour = endTime.getHourOfDay(),
						min = endTime.getMinuteOfHour();

				new TimePickerDialog(
						getActivity(),
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
				long duration = getArguments().getLong(
						Countdown.KEY_TIMER_END,
						Countdown.MILLIS_IN_HOUR
				);

				final DateTime currentTime = DateTime.now();
				final DateTime endTime = currentTime.plus(duration);

				int year = endTime.getYear(),
						month = endTime.getMonthOfYear() - 1,
						day = endTime.getDayOfMonth();

				datePicker = new DatePickerDialog(
						getActivity(),
						new DatePickerDialog.OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
								final Bundle args = getArguments();
								LocalDate obtainedDate = new LocalDate(year, monthOfYear + 1, dayOfMonth);

								long obtained = endTime.withDate(obtainedDate).getMillis();
								long now = currentTime.getMillis();

								long duration = obtained - now;

								if (duration < 0)
									new AlertDialog.Builder(getContext())
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
													updateEndTimeContainers();
												}
											}).show();

								else if (duration > 0)
									args.putLong(Countdown.KEY_TIMER_END, duration);

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
				final Bundle args = getArguments();
				long duration = args.getLong(
						Countdown.KEY_TIMER_END,
						Countdown.MILLIS_IN_HOUR
				);

				new TimeDurationPickerDialog(
						getActivity(),
						new TimeDurationPickerDialog.OnDurationSetListener() {
							@Override
							public void onDurationSet(long duration) {
								if (duration == 0) return;

								args.putLong(Countdown.KEY_TIMER_END, duration);
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
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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
				getArguments().putString(Countdown.KEY_TIMER_NAME, obtainedText);
			}
		});

		addNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				addTone.setEnabled(isChecked);
				getArguments().putBoolean(Countdown.KEY_TIMER_NOTIFY, isChecked);
			}
		});

		addTone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Uri tone = getArguments().getParcelable(Countdown.KEY_TIMER_TONE);

				Intent toneChooser = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
				toneChooser.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
				toneChooser.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, tone);

				startActivityForResult(toneChooser, Countdown.ACTIVITY_ACTION_RINGTONE);
				// show chooser
			}
		});

		addAutoRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				getArguments().putBoolean(Countdown.KEY_TIMER_REPEAT, isChecked);
			}
		});

		contentView.findViewById(R.id.add_tags).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent searchIntent = new Intent(getContext(), SearchTagActivity.class);
				searchIntent.putExtra(Countdown.KEY_TAG, getArguments().getStringArrayList(Countdown.KEY_TIMER_TAG));
				startActivityForResult(searchIntent, Countdown.ACTIVITY_TAGS);
			}
		});
	}

	private void updateRingtoneContainer() {
		Uri uri = getArguments().getParcelable(Countdown.KEY_TIMER_TONE);

		Uri defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
		if (uri == null || uri.equals(Settings.System.DEFAULT_ALARM_ALERT_URI) ||
				uri.equals(defaultUri)) {
			addTone.setText("Default");
		} else if (uri.equals(Uri.EMPTY)) {
			addTone.setText("Silent");
		} else {
			Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
			String title = ringtone.getTitle(getContext());
			addTone.setText(title);
		}


	}

	private void updateTagContainer() {
		ArrayList<String> tags = getArguments().getStringArrayList(Countdown.KEY_TIMER_TAG);
		Context context = getContext();

		if (tags == null) tags = new ArrayList<>();

		timerTagContaier.removeAllViews();

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

			timerTagContaier.addView(tagView);
		}

		timerTagContaier.invalidate();
	}

	public interface OnAddTimerDialogListener {
		void onDismiss(DialogInterface dialog);

		void onAddTimer(DialogInterface dialog, Timer timer, Uri toneUri, ArrayList<String> tags);
	}


}
