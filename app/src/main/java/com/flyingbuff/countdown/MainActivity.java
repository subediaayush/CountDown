package com.flyingbuff.countdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int RUNNABLE_ACTIVE = 1101;
    RecyclerView timerListView;
    TimerAdapter timerAdapter;
    ArrayList<Timer> timerList;
    LinearLayoutManager timerLayoutManager;
    TimerListAnimator timerListAnimator;
    Handler updateUiHandler = new Handler();
    Runnable updateUiRunnable = new Runnable() {
        @Override
        public void run() {
            if (timerAdapter.getItemCount() > 0) {

                int first = timerLayoutManager.findFirstVisibleItemPosition();
                int last = timerLayoutManager.findLastVisibleItemPosition();

                if (first != RecyclerView.NO_POSITION && last != RecyclerView.NO_POSITION) {
                    int count = last - first + 1;
                    timerAdapter.notifyItemRangeChanged(first, count);
                }
            }

            updateUiHandler.postDelayed(this, 1000);
        }
    };

    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    private BroadcastReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle("asdsds");
        setSupportActionBar(toolbar);

        timerListView = (RecyclerView) findViewById(R.id.timer_list);

        timerLayoutManager = new LinearLayoutManager(this);
        timerListView.setLayoutManager(timerLayoutManager);

        timerListAnimator = new TimerListAnimator();
        timerListAnimator.setSupportsChangeAnimations(false);
        timerListView.setItemAnimator(timerListAnimator);

        timerList = databaseHelper.loadTimer();

        timerAdapter = new TimerAdapter(this, timerList);
        timerListView.setAdapter(timerAdapter);

//        timerListView.addItemDecoration(new TimerListBackgroundDecorator());

        updateUiHandler.post(updateUiRunnable);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddTimerFragment addTimerFragment = AddTimerFragment.newInstance();
                addTimerFragment.args.putBoolean(Countdown.KEY_TIMER_ADD, true);
                addTimerFragment.setOnDismissListener(new AddTimerFragment.OnAddTimerDialogListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        fab.show();
                    }

                    @Override
                    public void onAddTimer(DialogInterface dialog, Timer timer, Uri tone, ArrayList<String> tags) {
                        int id = databaseHelper.saveTimer(timer);

                        timer.setId(id);

                        databaseHelper.assignTone(timer, tone);
                        databaseHelper.assignTag(timer, tags);

                        timer.startTimer();

                        timerAdapter.insertTimer(timer);
                    }

                    @Override
                    public void onDialogStateChanged(DialogInterface dialog, boolean settled) {
                    }
                });
//                addTimerFragment.

                addTimerFragment.show(getSupportFragmentManager(), "ADD_TIMER");
                fab.hide();
            }
        });

        alarmReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int timerId = intent.getIntExtra(Countdown.KEY_TIMER_TAG, -1);
                if (timerId == -1) return;

                int timerPosition = timerAdapter.getPositionFromId(timerId);

                timerAdapter.changeTimer(databaseHelper.loadTimer(timerId), timerPosition);
                Log.i("Timer Adapter", "Item " + timerPosition + " notified");
            }
        };
    }


    public void showTagDialog(View view) {
    }

    @Override
    protected void onPause() {
        super.onPause();

        AlarmHandler.cancelAllSilentAlarms();
    }


    @Override
    protected void onResume() {
        super.onResume();

        AlarmHandler.validate(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AlarmReceiver.ACTION_SILENT_ALARM);
        filter.addAction(AlarmReceiver.ACTION_NOISY_ALARM);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(alarmReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(alarmReceiver);
    }

    private void deleteItem(RecyclerView.ViewHolder viewHolder) {
        int removePosition = viewHolder.getAdapterPosition();
        Timer timer = timerAdapter.removeTimerAt(removePosition);
        databaseHelper.removeTimer(timer.getId());
        AlarmHandler.validate(timer);

    }
}
