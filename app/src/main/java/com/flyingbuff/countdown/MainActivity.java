package com.flyingbuff.countdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

	private static final int RUNNABLE_ACTIVE = 1101;
	RecyclerView timerListView;
	TimerAdapter timerAdapter;
	ArrayList<Timer> timerList;
	HashSet<Integer> swipedItems = new HashSet<>();
	ScrollingLinearLayoutManager timerLayoutManager;
	TimerListAnimator timerListAnimator;

	View emptyContainer;
	View highlightContainer;
	View otherTimerLabel;

	View firstTimer;

	AppBarLayout appBarLayout;

	int selectedItem = -1;

	DatabaseHelper databaseHelper = new DatabaseHelper(this);
	private BroadcastReceiver alarmReceiver;
	private TimerListTouchHelperCallback timerTouchHelperCallback;
	private HighlightTimerFragment highlightTimer;
	Handler uiHandler = new Handler();
	Runnable uiRunnable = new Runnable() {
		@Override
		public void run() {
			if (timerAdapter.getItemCount() > 0) {

				int first = timerLayoutManager.findFirstVisibleItemPosition();
				int last = timerLayoutManager.findLastVisibleItemPosition();

				if (first != RecyclerView.NO_POSITION && last != RecyclerView.NO_POSITION) {
//                    int count = last - first + 1;
//                    timerAdapter.notifyItemRangeChanged(first, count);
					for (int i = first; i <= last; i++) {
						if (!swipedItems.contains(i))
							timerAdapter.notifyItemChanged(i);
					}
				}

			}

			setupHighlightTimer();

			uiHandler.postDelayed(this, 1000);
		}
	};

	private void setupHighlightTimer() {
		int count = timerAdapter.getItemCount();
		if (count <= 0) {
			highlightContainer.setVisibility(View.INVISIBLE);
		} else {
			highlightContainer.setVisibility(View.VISIBLE);

			if (count > 1) {
				timerListView.setVisibility(View.VISIBLE);
				otherTimerLabel.setVisibility(View.VISIBLE);
			} else {
				timerListView.setVisibility(View.INVISIBLE);
				otherTimerLabel.setVisibility(View.GONE);
			}


			Timer timer = timerAdapter.getItem(0);

			if (timer == null) return;

			highlightTimer.setTimer(timer);
			highlightTimer.updateData();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		setupWindowTransition();

		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		appBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);

		firstTimer = findViewById(R.id.highlight_timer_container);

		highlightContainer = findViewById(R.id.hightlight_container);
		emptyContainer = findViewById(R.id.empty_placeholder);
		otherTimerLabel = findViewById(R.id.label_other_items);

		highlightTimer = HighlightTimerFragment.newInstance(true);

		highlightTimer.setTimerListener(new HighlightTimerFragment.HighlightTimerListener() {
			@Override
			public void onTimerDeleted(Timer timer) {
				timerAdapter.removeTimerAt(0);
				setupHighlightTimer();
			}

			@Override
			public void onTimerClicked(Timer timer) {
				startDetailActivity(timer);
			}
		});

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.highlight_timer_container, highlightTimer)
				.commit();

		timerListView = (RecyclerView) findViewById(R.id.timer_list);

		timerLayoutManager = new ScrollingLinearLayoutManager(this);
		timerListView.setLayoutManager(timerLayoutManager);

		timerListAnimator = new TimerListAnimator();
		timerListAnimator.setSupportsChangeAnimations(false);
		timerListView.setItemAnimator(timerListAnimator);

		timerList = databaseHelper.loadTimer();

		timerAdapter = new TimerAdapter(this, timerList) {
			@Override
			protected void onItemClicked(View view, int position) {
				startDetailActivity(getItem(position));
			}

			@Override
			protected void onTimerDeleted() {
				setupHighlightTimer();
			}
		};
		timerListView.setAdapter(timerAdapter);

		timerTouchHelperCallback = new TimerListTouchHelperCallback(
				this,
				timerAdapter,
				0,
				ItemTouchHelper.LEFT
		) {
			@Override
			public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
				return super.getSwipeDirs(recyclerView, viewHolder);
			}

			@Override
			public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
				final int position = viewHolder.getAdapterPosition();

				Timer removedTimer = timerAdapter.getItem(position);
				final int id = removedTimer.getId();
				String name = removedTimer.getName();
				if (name.isEmpty()) name = "Timer";

				Snackbar.make(timerListView, name + " deleted", Snackbar.LENGTH_SHORT)
						.setAction("Undo", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								timerAdapter.OnTimerDeleteCancelled(
										timerAdapter.getPositionFromId(id)
								);
							}
						})
						.show();

				timerAdapter.onItemSwiped(position);
			}

			@Override
			public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
//                    swipedItems.add(viewHolder.getAdapterPosition());

				super.onSelectedChanged(viewHolder, actionState);
			}
		};
		ItemTouchHelper timerTouchHelper = new ItemTouchHelper(timerTouchHelperCallback);
		timerTouchHelper.attachToRecyclerView(timerListView);

		timerListView.addItemDecoration(new TimerListBackgroundDecorator(this));
//        timerListView.addItemDecoration(new TimerListDividerDecorator(this));

		final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

		View.OnClickListener addTimerClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AddTimerBottomSheet addTimerFragment = AddTimerBottomSheet.newInstance();
				addTimerFragment.setOnDismissListener(
						new AddTimerBottomSheet.OnAddTimerDialogListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								fab.show();
							}

							@Override
							public void onAddTimer(DialogInterface dialog, Timer timer, Uri tone, ArrayList<String> tags) {
								int id = databaseHelper.saveTimer(timer);

								timer.setId(id);

								Context context = MainActivity.this;
								timer.setTone(context, tone);
								timer.setTags(context, tags);
								timer.startTimer();

								timerAdapter.insertTimer(timer);
								setupHighlightTimer();
							}

						}
				);

				addTimerFragment.show(getSupportFragmentManager(), "ADD_TIMER");
//				addTimerFragment.
				fab.hide();
			}

		};

		assert fab != null;
		fab.setOnClickListener(addTimerClickListener);
		emptyContainer.setOnClickListener(addTimerClickListener);

        /*firstTimer.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupWindowTransition();

                Timer timer = selectedItem == -1 ?
                        timerAdapter.getItem(0):
                        timerAdapter.getTimerFromId(selectedItem);

                Intent editIntent = new Intent(MainActivity.this, TimerDetailActivity.class);
                editIntent.putExtra(Countdown.KEY_TIMER, timer);

                View sharedView = findViewById(R.id.timer_summary_container);
                String transitionName = getString(R.string.name_progress_bar);


                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        MainActivity.this,
                        sharedView,
                        transitionName
                );

//                startActivity(editIntent);
                startActivity(editIntent,  options.toBundle());

            }
        });*/

		alarmReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int timerId = intent.getIntExtra(Countdown.KEY_TIMER_TAG, -1);
				if (timerId == -1) return;

				int timerPosition = timerAdapter.getPositionFromId(timerId);
				if (timerPosition == -1) return;

				Timer updatedTimer = databaseHelper.loadTimer(timerId);

				timerAdapter.changeTimer(updatedTimer, timerPosition);
				Log.i("Timer Adapter", "Item " + timerPosition + " notified");
			}
		};

	}

	private void startDetailActivity(Timer item) {
		Intent timerIntent = new Intent(MainActivity.this, TimerDetailActivity.class);
		timerIntent.putExtra(Countdown.KEY_TIMER, item);
		timerIntent.putExtra(Countdown.KEY_INITIAL_PROGRESS, timerAdapter.getItem(0).getProgress());

		Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
				MainActivity.this,
				new Pair<>(
						firstTimer,
						getString(R.string.transition_timer_bg)
				)
		).toBundle();

		startActivityForResult(timerIntent, Countdown.ACTIVITY_TIMER_DETAIL, bundle);
//                timerLayoutManager.smoothScrollToPosition(
//                        timerListView,
//                        null,
//                        position
//                );

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case Countdown.ACTIVITY_TIMER_DETAIL:
				if (resultCode == RESULT_OK) {
					Bundle extras = data.getExtras();
					if (extras.getBoolean(Countdown.KEY_TIMER_EDITED, false)) {
						Timer timer = extras.getParcelable(Countdown.KEY_TIMER);
						assert timer != null;
						int timerPosition = timerAdapter.getPositionFromId(timer.getId());
						timerAdapter.changeTimer(timer, timerPosition);
						Log.i("Timer Adapter", "Item " + timerPosition + " notified");
					}
				} else if (resultCode == RESULT_CANCELED) {
					Bundle extras = data.getExtras();
					Timer timer = extras.getParcelable(Countdown.KEY_TIMER);
					int timerPosition = timerAdapter.getPositionFromId(timer.getId());
					if (timerPosition != -1) timerAdapter.removeTimerAt(timerPosition);
					Log.i("Timer Adapter", "Item " + timerPosition + " removed");
				}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		uiHandler.removeCallbacks(uiRunnable);
		AlarmHandler.cancelAllSilentAlarms();
	}

	@Override
	protected void onResume() {
		super.onResume();

		uiHandler.post(uiRunnable);
		AlarmHandler.validate(false);
	}

	private void setupWindowTransition() {
		Transition transition = new Fade();

		transition.excludeTarget(android.R.id.statusBarBackground, true);
		transition.excludeTarget(android.R.id.navigationBarBackground, true);

		getWindow().setExitTransition(transition);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		if (BuildConfig.DEBUG) getMenuInflater().inflate(R.menu.menu_test_utilities, menu);
//
//		return super.onCreateOptionsMenu(menu);
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		if (!BuildConfig.DEBUG) return super.onOptionsItemSelected(item);
//
//		if (item.getItemId() == R.id.add_test_timer) {
//			for (int i = 0; i < 5; i++) {
//				int counter = timerAdapter.getItemCount() + i;
//				Timer timer = new Timer(
//						"Timer " + counter,
//						DateTime.now().plusSeconds(5).getMillis(),
//						false,
//						false,
//						true
//				);
//
//				int id = databaseHelper.saveTimer(timer);
//
//				timer.setId(id);
//				timer.startTimer();
//
//				timerAdapter.insertTimer(timer);
//			}
//		}
//		return super.onOptionsItemSelected(item);
//	}
}
