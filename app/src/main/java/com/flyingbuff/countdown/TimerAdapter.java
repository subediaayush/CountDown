package com.flyingbuff.countdown;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Aayush on 8/8/2016.
 */
public class TimerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Timer> mDataset;

    public TimerAdapter(ArrayList<Timer> mDataset) {
        this.mDataset = mDataset;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(parent.getContext());
        View view = li.inflate(R.layout.layout_timer_list_item, parent, false);

        return new TimerHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Timer timer = getItem(position);

        String[] displayString = timer.formatDuration();

        TimerHolder timerHolder = (TimerHolder) holder;

        int order = Integer.parseInt(displayString[0]);

        if (order == -1) return;

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
            } else {
                timerHolder.midOrderTime.setVisibility(View.GONE);
                timerHolder.midOrderUnit.setVisibility(View.GONE);

                timerHolder.minOrderTime.setText(displayString[1]);
                timerHolder.minOrderUnit.setText(displayString[2]);
            }
        }
    }

    public Timer getItem(int position) {
        return mDataset.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
