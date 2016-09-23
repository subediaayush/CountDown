package com.flyingbuff.countdown;

import android.content.Context;
import android.util.AttributeSet;

import com.john.waveview.WaveView;

/**
 * Created by Aayush on 9/21/2016.
 */

public class WaveViewPlus extends WaveView {
	private int mProgress;

	public WaveViewPlus(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setProgress(int progress) {
		super.setProgress(progress);
		mProgress = progress;
	}

	public int getProgress() {
		return mProgress;
	}
}
