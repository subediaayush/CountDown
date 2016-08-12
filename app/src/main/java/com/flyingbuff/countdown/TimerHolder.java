package com.flyingbuff.countdown;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Aayush on 8/8/2016.
 */
public class TimerHolder extends RecyclerView.ViewHolder {
    public AdjustableTextView maxOrderTime;
    public AdjustableTextView midOrderTime;
    public AdjustableTextView minOrderTime;

    public AdjustableTextView maxOrderUnit;
    public AdjustableTextView midOrderUnit;
    public AdjustableTextView minOrderUnit;

    public TimerHolder(View itemView) {
        super(itemView);

        maxOrderTime = (AdjustableTextView) itemView.findViewById(R.id.time_order_max);
        midOrderTime = (AdjustableTextView) itemView.findViewById(R.id.time_order_mid);
        minOrderTime = (AdjustableTextView) itemView.findViewById(R.id.time_order_min);

        maxOrderUnit = (AdjustableTextView) itemView.findViewById(R.id.unit_order_max);
        midOrderUnit = (AdjustableTextView) itemView.findViewById(R.id.unit_order_mid);
        minOrderUnit = (AdjustableTextView) itemView.findViewById(R.id.unit_order_min);
    }
}
