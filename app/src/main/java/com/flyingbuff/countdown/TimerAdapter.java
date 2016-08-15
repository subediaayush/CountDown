package com.flyingbuff.countdown;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Aayush on 8/8/2016.
 */
public class TimerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final DatabaseHelper dbHelper;
    ArrayList<Timer> mDataset;
    HashMap<Integer, ArrayList<String>> tagList;
    Context context;

    public TimerAdapter(Context context, ArrayList<Timer> mDataset) {
        this.mDataset = mDataset;
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        tagList = new HashMap<>();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(parent.getContext());
        View view = li.inflate(R.layout.layout_timer_list_item, parent, false);

        return new TimerHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder h, final int position) {
        final Timer timer = getItem(position);

        String[] displayString = timer.formatDuration();

        TimerHolder holder = (TimerHolder) h;

        TextView timerName = holder.timerName;
        TextView timerRemainingTime = holder.timerRemainingTime;
        TextView timerEndDateTime = holder.timerEndDateTime;

        ViewGroup timerTagContainer = holder.timerTagContainer;

        ImageView timerIndicatorNotify = holder.timerIndicatorNotify;
        ImageView timerIndicatorSilent = holder.timerIndicatorSilent;
        ImageView timerIndicatorRepeat = holder.timerIndicatorRepeat;

        ProgressBar timerProgress = holder.timerProgress;
        ToggleButton timerPauseToggle = holder.timerPauseToggle;

        String name = timer.getName();
        if (name.isEmpty()) timerName.setVisibility(View.GONE);

        timerName.setText(timer.getName());
        timerRemainingTime.setText(timer.humanize());
        timerEndDateTime.setText(timer.humanizeEndDateTime("ends on"));

        if (!timer.isNotify()) {
            timerIndicatorNotify.setAlpha(.25f);
            timerIndicatorSilent.setAlpha(.25f);
        } else if (timer.isSilent()) timerIndicatorSilent.setAlpha(.25f);

        if (!timer.isRepeat()) timerIndicatorRepeat.setAlpha(.25f);

        timerTagContainer.removeAllViews();

        if (tagList.get(position) == null) tagList.put(position, dbHelper.loadTags(timer));

        ArrayList<String> tags = tagList.get(position);
        if (tags.isEmpty()) tags.add("No Tag");

        for (String tag : tags) {
            if (timerTagContainer.findViewWithTag(tag) != null) continue;
            TextView tagView = (TextView) LayoutInflater.from(context)
                    .inflate(R.layout.template_tag, null);
            tagView.setTag(tag);
            tagView.setText(tag);
            timerTagContainer.addView(tagView);
        }

        boolean paused = timer.isPaused();

        int progress = (int) (timer.getProgress() * 100);
        timerProgress.setProgress(progress);

        timerPauseToggle.setChecked(paused);
        timerPauseToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) timer.pauseTimer();
                else timer.resumeTimer();
            }
        });

        /*int order = Integer.parseInt(displayString[0]);

        if (order == -1)
        return;

        if (order > Timer.MINUTE) {
            timerHolder.maxOrderTime.setText(displayString[1]);
            timerHolder.maxOrderUnit.setText(displayString[2]);

            timerHolder.midOrderTime.setText(displayString[3]);
            timerHolder.midOrderUnit.setText(displayString[4]);

            timerHolder.minOrderTime.setText(displayString[5]);
            timerHolder.minOrderUnit.setText(displayString[6]);
        } else {
            timerHolder.maxOrderTime.setVisibility(View.GONE);
            timerHolder.maxOrderUnit.setVisibility(View.GONE);

            if (order == Timer.MINUTE) {
                timerHolder.midOrderTime.setText(displayString[1]);
                timerHolder.midOrderUnit.setText(displayString[2]);

                timerHolder.minOrderTime.setText(displayString[3]);
                timerHolder.minOrderUnit.setText(displayString[4]);
            } else if (order == Timer.SECOND){
                timerHolder.midOrderTime.setVisibility(View.GONE);
                timerHolder.midOrderUnit.setVisibility(View.GONE);

                timerHolder.minOrderTime.setText(displayString[1]);
                timerHolder.minOrderUnit.setText(displayString[2]);
            }
        }
        */
    }

    public void invalidateTimerTags() {
        tagList = new HashMap<>();
    }

    public void invalidateTimerTags(int position) {
        tagList.remove(position);
    }

    public Timer getItem(int position) {
        return mDataset.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
