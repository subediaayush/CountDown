package com.flyingbuff.countdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Aayush on 8/17/2016.
 */
public class AlarmNotificationHandler extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle args = intent.getExtras();

        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(mainActivityIntent);
    }
}
