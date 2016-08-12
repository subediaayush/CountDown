package com.flyingbuff.countdown;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Aayush on 8/8/2016.
 */
public class TimerTextView extends AdjustableTextView {
    Timer timer;

    public TimerTextView(Context context) {
        this(context, null);
    }

    public TimerTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}
