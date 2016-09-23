package com.flyingbuff.countdown;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by Aayush on 8/8/2016.
 */
public class TimerHolder extends RecyclerView.ViewHolder {
    protected static final int TIMER_PENDING_DELETE = 1002;
    protected static final int TIMER_PENDING_NONE = 1003;

    protected final TextView timerName;
    protected final TextView timerRemainingTime;
    protected final TextView timerEndDateTime;
    protected final ViewGroup timerTagContainer;
    protected final ImageView timerIndicatorNotify;
    protected final ImageView timerIndicatorSilent;
    protected final ImageView timerIndicatorRepeat;
    protected final ProgressBar timerProgress;
    protected final ToggleButton timerPauseToggle;

    protected final View timerBackground;
    protected final View timerUndoDeleteView;
//    protected final View timerUndoDeleteButton;


    public TimerHolder(View itemView) {
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
        timerBackground = itemView.findViewById(R.id.timer_background);

        timerUndoDeleteView = itemView.findViewById(R.id.timer_undo_delete);
//        timerUndoDeleteButton = itemView.findViewById(R.id.timer_undo_delete);
    }


}
