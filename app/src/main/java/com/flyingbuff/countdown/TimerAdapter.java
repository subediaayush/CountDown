package com.flyingbuff.countdown;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Aayush on 8/8/2016.
 */
public class TimerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final DatabaseHelper dbHelper;
    SortedList<Timer> timerList;
    HashMap<Integer, ArrayList<String>> tagSet;

    public TimerAdapter(Context context, ArrayList<Timer> timerList) {
        this.dbHelper = new DatabaseHelper(context);
        tagSet = new HashMap<>();

        this.timerList = new SortedList<>(Timer.class, new SortedList.Callback<Timer>() {
            @Override
            public int compare(Timer o1, Timer o2) {
                return Timer.REMAINING_TIME_COMPARATOR.compare(o1, o2);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Timer oldItem, Timer newItem) {
                boolean elapsed = oldItem.getElapsedTime() == newItem.getElapsedTime();

                return elapsed;
            }

            @Override
            public boolean areItemsTheSame(Timer item1, Timer item2) {
                return item1.getId() == item2.getId();
            }
        });

        this.timerList.addAll(timerList);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LayoutInflater li = LayoutInflater.from(context);
        View view = li.inflate(R.layout.layout_timer_list_item, parent, false);

        return new TimerHolder(context, view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder h, final int position) {
        final Timer timer = getItem(position);
        int id = timer.getId();

        TimerHolder holder = (TimerHolder) h;

        if (tagSet.get(id) == null) tagSet.put(id, dbHelper.loadTags(timer));


        ArrayList<String> strings = tagSet.get(id);
        String[] tags = strings.toArray(new String[strings.size()]);
        if (tags.length == 0) tags = new String[]{"No Tags"};

        holder.bindView(timer, tags);
    }

    public Timer getItem(int position) {
        return timerList.get(position);
    }

    @Override
    public int getItemCount() {
        return timerList.size();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public int getPositionFromId(int id) {
        for (int i = 0; i < timerList.size(); i++) {
            Timer timer = timerList.get(i);
            if (timer.getId() == id) return timerList.indexOf(timer);
        }
        return -1;
    }

    public void insertTimer(Timer timer) {
        timerList.add(timer);
    }

    public void changeTimer(Timer timer, int position) {
        timerList.updateItemAt(position, timer);
    }

    public void removeTimer(Timer timer) {
        timerList.remove(timer);
    }

    public Timer removeTimerAt(int position) {
        return timerList.removeItemAt(position);
    }

    public SortedList<Timer> getList() {
        return timerList;
    }
}
