package com.flyingbuff.countdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Aayush on 9/22/2016.
 */
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmHandler.validateNoisyTimers();
	}
}
