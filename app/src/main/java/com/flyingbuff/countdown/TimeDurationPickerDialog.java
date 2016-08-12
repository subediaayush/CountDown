package com.flyingbuff.countdown;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Aayush on 8/10/2016.
 */
public class TimeDurationPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {
    private final OnDurationSetListener durationSetListener;

    public static final long PICKER_MAX_VALUE = 999999;

    long duration;

    public TimeDurationPickerDialog(Context context, OnDurationSetListener listener, long duration) {
        super(context);
        durationSetListener = listener;

        setDuration(duration);

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_duration_picker, null);

        setView(view);

        initiateView(view);

        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);
    }

    private void initiateView(final View view) {
        displayDuration(view);
        View.OnClickListener numpadClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long tenHour = 100000;
                if (duration >= tenHour) return;

                if (v.getId() == R.id.numPad00) {
                    if (duration >= 10000) return;
                    duration *= 100;
                    duration = setDigit(duration, 1, 0);
                    duration = setDigit(duration, 2, 0);
                } else {
                    TextView digitContainer = (TextView) v;
                    int inputDigit = Integer.parseInt((String) digitContainer.getText());
                    duration *= 10;
                    duration = setDigit(duration, 1, inputDigit);
                }
                displayDuration(view);
            }
        };

        View.OnClickListener backSpaceClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                duration /= 10;
                displayDuration(view);
            }
        };

        view.findViewById(R.id.numPad00).setOnClickListener(numpadClickListener);
        view.findViewById(R.id.numPad0).setOnClickListener(numpadClickListener);
        view.findViewById(R.id.numPad1).setOnClickListener(numpadClickListener);
        view.findViewById(R.id.numPad2).setOnClickListener(numpadClickListener);
        view.findViewById(R.id.numPad3).setOnClickListener(numpadClickListener);
        view.findViewById(R.id.numPad4).setOnClickListener(numpadClickListener);
        view.findViewById(R.id.numPad5).setOnClickListener(numpadClickListener);
        view.findViewById(R.id.numPad6).setOnClickListener(numpadClickListener);
        view.findViewById(R.id.numPad7).setOnClickListener(numpadClickListener);
        view.findViewById(R.id.numPad8).setOnClickListener(numpadClickListener);
        view.findViewById(R.id.numPad9).setOnClickListener(numpadClickListener);

        view.findViewById(R.id.backspace).setOnClickListener(backSpaceClickListener);
        view.findViewById(R.id.backspace).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                duration = 0;
                displayDuration(view);
                return true;
            }
        });

    }

    private void displayDuration(View view) {
        String[] displayNumber = formatMilli(duration);

        setupOutput(view, displayNumber);

    }

    public long setDigit(long source, int position, int digit) {
        long msb = (long) Math.pow(10, position);

        long source_digit = (source % msb) / msb * 10;

        long output = (source - ((source_digit - digit) * (int) (msb / 10)));
        return output;

    }

    private void setupOutput(View view, String[] displayNumber) {
        TextView hour = (TextView) view.findViewById(R.id.hours);
        TextView minute = (TextView) view.findViewById(R.id.minutes);
        TextView second = (TextView) view.findViewById(R.id.seconds);

        hour.setText(displayNumber[Timer.HOUR]);
        minute.setText(displayNumber[Timer.MINUTE]);
        second.setText(displayNumber[Timer.SECOND]);
    }

    private String[] formatMilli(long duration) {
        String[] output = new String[]{"", "", "", ""};

        long seconds = duration % 100;
        duration /= 100;

        long minutes = duration % 100;
        duration /= 100;

        long hours = duration;

        output[Timer.SECOND] = String.format("%02d", seconds);
        output[Timer.MINUTE] = String.format("%02d", minutes);
        output[Timer.HOUR] = String.format("%02d", hours);

        return output;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (durationSetListener != null) {
                    durationSetListener.onDurationSet(getDuration());
                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    private long getDuration() {
        long hour = duration / 10000 * Countdown.SECONDS_IN_HOUR;
        long min = (duration % 10000) / 100 * Countdown.SECONDS_IN_MINUTE;
        long sec = duration % 100;
        return (hour + min + sec) * 1000;
    }

    private void setDuration(long realDuration) {
        realDuration /= 1000;

        long hours = realDuration / Countdown.SECONDS_IN_HOUR;
        realDuration %= Countdown.SECONDS_IN_HOUR;

        long minutes = realDuration / Countdown.SECONDS_IN_MINUTE;
        realDuration %= Countdown.SECONDS_IN_MINUTE;

        long seconds = realDuration;

        if (hours > 99) {
            minutes += (hours - 99) * 60;
            hours = 99;
        }

        if (minutes > 99) {
            seconds += (minutes - 99) * 60;
            minutes = 99;
        }

        seconds = Math.min(seconds, 99);

        duration = Math.min(
                seconds + 100 * (minutes + 100 * hours),
                PICKER_MAX_VALUE
        );
    }

    public interface OnDurationSetListener {
        void onDurationSet(long duration);
    }
}
