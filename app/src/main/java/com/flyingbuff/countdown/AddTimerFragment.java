package com.flyingbuff.countdown;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
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
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
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
public class AddTimerFragment extends BottomSheetDialogFragment {

    Bundle args;

    private OnAddTimerDialogListener onDismissListener;

    TextView addEndTime;
    TextView addEndDate;
    TextView addEndTimer;
    TextView metaInfo;
    EditText addName;
    Switch addNotify;
    TextView addTone;
    Button addTimer;
    Switch addAutoDelete;
    View timerTagPlaceholder;
    FlexboxLayout timerTagContaier;

    BottomSheetBehavior behavior;

    DatePickerDialog datePicker;
    private boolean isShowingTimer = true;
    private int timerLocation = -1;

    public static AddTimerFragment newInstance() {
        AddTimerFragment fragment = new AddTimerFragment();
        fragment.args = new Bundle();

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AddTimerTheme);
        // clone the inflater using the ContextThemeWrapper
//        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        View contentView = View.inflate(contextThemeWrapper, R.layout.content_add_timer, null);

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
        addAutoDelete = (Switch) contentView.findViewById(R.id.add_auto_repeat);

        timerTagPlaceholder = contentView.findViewById(R.id.add_tags_placeholder);
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
                        getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
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
                args.putBoolean(Countdown.KEY_TIMER_AUTO_DEL, isChecked);
            }
        });

        contentView.findViewById(R.id.tags_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getContext(), SearchTagActivity.class);
                searchIntent.putExtra(Countdown.KEY_TAG, args.getStringArrayList(Countdown.KEY_TIMER_TAG));
                startActivityForResult(searchIntent, Countdown.ACTIVITY_ADD_TAGS);
            }
        });

        Toolbar toolbar = (Toolbar) contentView.findViewById(R.id.toolbar);
        boolean addMode = args.getBoolean(Countdown.KEY_TIMER_ADD);
        toolbar.setTitle(addMode ? "Add Timer" : "Edit Timer");
        toolbar.inflateMenu(R.menu.menu_add_timer);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.add_timer) {
                    addNewTimer();
                    dismiss();
                }
                return true;
            }
        });

        dialog.setContentView(contentView);
        behavior = BottomSheetBehavior.from((View) contentView.getParent());
        behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback);
        return dialog;
    }

    private void addNewTimer() {
        String name = args.getString(Countdown.KEY_TIMER_NAME);
        long end = args.getLong(Countdown.KEY_TIMER_END);
        Boolean notify = args.getBoolean(Countdown.KEY_TIMER_NOTIFY);
        Uri tone = args.getParcelable(Countdown.KEY_TIMER_TONE);
        Boolean autoDelete = args.getBoolean(Countdown.KEY_TIMER_AUTO_DEL);
        ArrayList<String> tags = args.getStringArrayList(Countdown.KEY_TIMER_TAG);

        if (tone == null) tone = Uri.EMPTY;
        if (tags == null) tags = new ArrayList<>();

        long endDate = DateTime.now().plus(end).getMillis();

        Timer timer = new Timer(name, endDate, autoDelete, notify, notify && tone.equals(Uri.EMPTY));

        if (onDismissListener != null) onDismissListener.onAddTimer(getDialog(), timer, tone, tags);
    }

    private void animateSaveButton() {

        int translationY = getTimerTranslation(addTimer);
        addTimer.animate().setDuration(100).setInterpolator(new DecelerateInterpolator())
                .translationY(translationY);
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            Log.i("AddTimerFragment", "State " + newState);
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            } else if (newState == BottomSheetBehavior.STATE_COLLAPSED ||
                    newState == BottomSheetBehavior.STATE_EXPANDED) {
                if (onDismissListener != null)
                    onDismissListener.onDialogStateChanged(getDialog(), true);
            } else if (newState == BottomSheetBehavior.STATE_DRAGGING ||
                    newState == BottomSheetBehavior.STATE_SETTLING) {
                if (onDismissListener != null)
                    onDismissListener.onDialogStateChanged(getDialog(), false);
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        if (onDismissListener != null) onDismissListener.onDialogStateChanged(getDialog(), false);
    }

    private void hideSaveButton() {
        addTimer.animate().setDuration(100).setInterpolator(new AccelerateInterpolator())
                .scaleY(.1f)
                .scaleX(.1f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        addTimer.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void showSaveButton() {
        addTimer.animate().setDuration(100).setInterpolator(new DecelerateInterpolator())
                .scaleY(1)
                .scaleX(1)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {

                        addTimer.setScaleY(.1f);
                        addTimer.setScaleX(.1f);

                        addTimer.setVisibility(View.VISIBLE);

                        int translation = getTimerTranslation(addTimer);
                        addTimer.setTranslationY(translation);
                    }
                });
    }

    private int getTimerTranslation(View view) {
        Context context = getContext();

        final int[] location = new int[2];
        view.getLocationOnScreen(location);

        Point displaySize = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getSize(displaySize);

        int initialY = location[1];
        int finalY = (int) (displaySize.y - view.getHeight()
                - Countdown.dpToPixel(context, 10));

        return finalY - initialY;
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

            String[] summaryComp = Timer.formatDuration(duration);

            for (int i = 1; i < summaryComp.length; i += 2) {
                if (!"00".equals(summaryComp[i])) {
                    summary.append(summaryComp[i]).append(" ");
                    summary.append(summaryComp[i + 1]).append(" ");
                }
            }

            metaInfo.setText(summary.toString());
        }
    }


    private void initViews() {
        updateEndTimeContainers();

        addName.setText(args.getString(Countdown.KEY_TIMER_NAME));
        addNotify.setChecked(args.getBoolean(Countdown.KEY_TIMER_NOTIFY));
        addAutoDelete.setChecked(args.getBoolean(Countdown.KEY_TIMER_AUTO_DEL));

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
        if (!args.containsKey(Countdown.KEY_TIMER_ADD))
            args.putBoolean(Countdown.KEY_TIMER_ADD, true);

        if (!args.containsKey(Countdown.KEY_TIMER_END))
            args.putLong(Countdown.KEY_TIMER_END, Countdown.MILLIS_IN_HOUR);

        if (!args.containsKey(Countdown.KEY_TIMER_NAME))
            args.putString(Countdown.KEY_TIMER_NAME, "");

        if (!args.containsKey(Countdown.KEY_TIMER_NOTIFY))
            args.putBoolean(Countdown.KEY_TIMER_NOTIFY, true);

        if (!args.containsKey(Countdown.KEY_TIMER_TONE)) {
            boolean notify = args.getBoolean(Countdown.KEY_TIMER_NOTIFY);
            if (notify)
                args.putParcelable(Countdown.KEY_TIMER_TONE, Settings.System.DEFAULT_ALARM_ALERT_URI);
        }

        if (!args.containsKey(Countdown.KEY_TIMER_TAG))
            args.putStringArrayList(Countdown.KEY_TIMER_TAG, new ArrayList<String>());

        if (!args.containsKey(Countdown.KEY_TIMER_AUTO_DEL))
            args.putBoolean(Countdown.KEY_TIMER_AUTO_DEL, false);


    }

    private void updateEndTimeContainers() {
        long timerDuration = args.getLong(Countdown.KEY_TIMER_END, Countdown.MILLIS_IN_HOUR);

        DateTime endDateTime = DateTime.now().plus(timerDuration);

        String endTime = endDateTime.toString("hh : mm a");
        String endDate = endDateTime.toString("MMM dd, yyyy");

        String[] durationString = Timer.formatDuration(timerDuration);

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < durationString.length; i += 2) {
            if (!"00".equals(durationString[i])) {
                builder.append(durationString[i]).append(" ");
                builder.append(durationString[i + 1]).append(" ");
            }
        }
        String endTimer = builder.toString();

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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (onDismissListener != null) onDismissListener.onDismiss(dialog);
    }

    public void setOnDismissListener(OnAddTimerDialogListener onAddTimerDialogListener) {
        this.onDismissListener = onAddTimerDialogListener;
    }

    public interface OnAddTimerDialogListener {
        void onDismiss(DialogInterface dialog);

        void onAddTimer(DialogInterface dialog, Timer timer, Uri toneUri, ArrayList<String> tags);

        void onDialogStateChanged(DialogInterface dialog, boolean settled);
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

                Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
                String title = ringtone.getTitle(getContext());
                Log.i("AddTimerFragment", title);
            } else {
                args.putParcelable(Countdown.KEY_TIMER_TONE, Uri.EMPTY);
            }
            updateRingtoneContainer();
        }
        if (requestCode == Countdown.ACTIVITY_ADD_TAGS && resultCode == Activity.RESULT_OK) {
            Bundle data = intent.getExtras();
            ArrayList<String> tags = data.getStringArrayList(Countdown.KEY_TAG);

            if (tags != null) args.putStringArrayList(Countdown.KEY_TIMER_TAG, tags);
            updateTagContainer();
        }
    }

    private void updateTagContainer() {
        ArrayList<String> tags = args.getStringArrayList(Countdown.KEY_TIMER_TAG);
        Context context = getContext();

        if (tags == null) tags = new ArrayList<>();

        if (tags.isEmpty()) timerTagPlaceholder.setVisibility(View.VISIBLE);
        else timerTagPlaceholder.setVisibility(View.INVISIBLE);

        int totalDisplayedTags = timerTagContaier.getChildCount();
        for (int i = totalDisplayedTags - 1; i >= 0; i--) {
            View child = timerTagContaier.getChildAt(i);
            String childTag = child.getTag().toString();
            if (!tags.contains(childTag)) timerTagContaier.removeView(child);
        }

        for (String tag : tags) {
            if (timerTagContaier.findViewWithTag(tag) != null) continue;
            TextView tagView = new TextView(context, null, R.style.Timer_Tag);
            tagView.setTag(tag);
            tagView.setText(tag);
            timerTagContaier.addView(tagView);
        }
    }

    private void updateRingtoneContainer() {
        Uri uri = args.getParcelable(Countdown.KEY_TIMER_TONE);

        Uri defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
        if (uri == null || uri.equals(Settings.System.DEFAULT_ALARM_ALERT_URI) ||
                uri.equals(Settings.System.DEFAULT_RINGTONE_URI)) {
            addTone.setText("Default");
        } else if (uri.equals(Uri.EMPTY)) {
            addTone.setText("Silent");
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
            String title = ringtone.getTitle(getContext());
            addTone.setText(title);
        }


    }
}
