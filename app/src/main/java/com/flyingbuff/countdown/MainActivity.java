package com.flyingbuff.countdown;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
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
    Handler updateUiHandler = new Handler();
    Runnable updateUiRunnable = new Runnable() {
        @Override
        public void run() {
            if (timerList.size() > 0) {

                int firstItem = timerLayoutManager.findFirstVisibleItemPosition();
                int lastItem = timerLayoutManager.findLastVisibleItemPosition();

                for (int i = firstItem; i <= lastItem; i++)
                    timerAdapter.notifyItemChanged(i);
            }
            updateUiHandler.postDelayed(this, 1000);
        }
    };

    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    private BottomSheetBehavior addTimerSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("asdsds");

        timerListView = (RecyclerView) findViewById(R.id.timer_list);
        timerLayoutManager = new LinearLayoutManager(this);
        timerListView.setLayoutManager(timerLayoutManager);
        ((DefaultItemAnimator) timerListView.getItemAnimator()).setSupportsChangeAnimations(false);

        timerList = databaseHelper.loadTimer();

        timerAdapter = new TimerAdapter(this, timerList);
        timerListView.setAdapter(timerAdapter);

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
                        timerList.add(timer);

                        timerAdapter.notifyItemInserted(timerList.indexOf(timer));
                    }

                    @Override
                    public void onDialogStateChanged(DialogInterface dialog, boolean settled) {
                        if (settled) resumeUiRefresh();
                        else pauseUiRefresh();
                    }
                });
//                addTimerFragment.

                addTimerFragment.show(getSupportFragmentManager(), "ADD_TIMER");
                fab.hide();
//                addTimerSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                DateTime now = DateTime.now();
//                timerList.add(new Timer(now.plusHours(2).plusMinutes(5).getMillis()));
////                Collections.sort(timerList);
//                timerAdapter.notifyItemInserted(timerList.size() - 1);
            }
        });

//        addTimerSheetBehavior = BottomSheetBehavior.from(addTimerSheet);
    }

    private void pauseUiRefresh() {
        if (updateUiHandler.hasMessages(RUNNABLE_ACTIVE)) {
            updateUiHandler.removeCallbacks(updateUiRunnable);
            updateUiHandler.removeMessages(RUNNABLE_ACTIVE);
        }
        Log.i("MainActivity", "UI refresh paused");
    }

    private void resumeUiRefresh() {
        if (!updateUiHandler.hasMessages(RUNNABLE_ACTIVE)) {
            updateUiHandler.post(updateUiRunnable);
            updateUiHandler.sendEmptyMessage(RUNNABLE_ACTIVE);
        }
        Log.i("MainActivity", "UI refresh resumed");
    }

    public void showTagDialog(View view) {
    }
}
