package com.flyingbuff.countdown;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by Aayush on 8/8/2016.
 */
public class TimerHolder extends RecyclerView.ViewHolder {
    protected final TextView timerName;
    protected final TextView timerRemainingTime;
    protected final TextView timerEndDateTime;
    protected final ViewGroup timerTagContainer;
    protected final ImageView timerIndicatorNotify;
    protected final ImageView timerIndicatorSilent;
    protected final ImageView timerIndicatorRepeat;
    protected final ProgressBar timerProgress;
    protected final ToggleButton timerPauseToggle;

    private final Context context;

    public TimerHolder(Context context, View itemView) {
        super(itemView);

        timerName = (TextView) itemView.findViewById(R.id.timer_name);
        timerRemainingTime = (TextView) itemView.findViewById(R.id.timer_remaining_time);
        timerEndDateTime = (TextView) itemView.findViewById(R.id.timer_end_datetime);
        timerTagContainer = (ViewGroup) itemView.findViewById(R.id.timer_tag_container);
        timerIndicatorNotify = (ImageView) itemView.findViewById(R.id.timer_indicator_nofity);
        timerIndicatorSilent = (ImageView) itemView.findViewById(R.id.timer_indicator_silent);
        timerIndicatorRepeat = (ImageView) itemView.findViewById(R.id.timer_indicator_repeat);
        timerProgress = (ProgressBar) itemView.findViewById(R.id.timer_progress);
        timerPauseToggle = (ToggleButton) itemView.findViewById(R.id.timer_pause_toggle);

        this.context = context;
    }

    public void bindView(final Timer timer, String[] tags) {
        String name = timer.getName();

        final boolean paused = timer.isPaused();
        final boolean stopped = timer.isStopped();

        timerName.setText(timer.getName());
        if (name.isEmpty()) timerName.setVisibility(View.GONE);
        else timerName.setVisibility(View.VISIBLE);

        if (stopped) {
            timerRemainingTime.setText(Timer.humanize(timer.getDuration()));
            timerEndDateTime.setVisibility(View.GONE);
        } else {
            timerRemainingTime.setText(timer.humanize());
            timerEndDateTime.setText(timer.humanizeEndDateTime("ends on"));

            timerEndDateTime.setVisibility(View.VISIBLE);
        }

        if (!timer.isNotify()) {
            timerIndicatorNotify.setAlpha(.25f);
            timerIndicatorSilent.setAlpha(.25f);
        } else if (timer.isSilent()) timerIndicatorSilent.setAlpha(.25f);

        if (!timer.isRepeat()) timerIndicatorRepeat.setAlpha(.25f);

        timerTagContainer.removeAllViews();
        for (String tag : tags) {
            TextView tagView = (TextView) LayoutInflater.from(context)
                    .inflate(R.layout.template_tag, null);
            tagView.setTag(tag);
            tagView.setText(tag);
            timerTagContainer.addView(tagView);
        }

        int progress = timer.getProgress();

        ObjectAnimator animation = ObjectAnimator.ofInt(
                timerProgress,
                "progress",
                progress
        );

        animation.setDuration(500);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();

        timerPauseToggle.setOnCheckedChangeListener(null);
        timerPauseToggle.setChecked(stopped || paused);
        timerPauseToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                if (!checked) {
                    if (stopped)
                        timer.startTimer();
                    else
                        timer.resumeTimer();
                } else timer.pauseTimer();
            }
        });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.wtf("CardView", "Clicked");
            }
        });
    }
}
