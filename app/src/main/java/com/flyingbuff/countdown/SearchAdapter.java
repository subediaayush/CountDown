package com.flyingbuff.countdown;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.StringSearchViewHolder> {
    private SortedList<String> sortedTags;
    private ArrayList<String> tags;

    private Context context;

    private ListItemClickListener itemClickListener;
    private ListItemSelectedListener itemSelectedListener;

    private HashSet<String> selectedItems;

    public SearchAdapter(Context context, HashSet<String> initTags) {
        this.context = context;

        tags = new DatabaseHelper(context).loadTags();
        selectedItems = new HashSet<>();

        selectedItems.addAll(initTags);

        this.sortedTags = new SortedList<>(String.class, new SortedList.Callback<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
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
            public boolean areContentsTheSame(String oldItem, String newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(String item1, String item2) {
                return item1.equals(item2);
            }
        });

        this.sortedTags.addAll(tags);
    }

    public void addTag(String tag) {
        sortedTags.add(tag);
    }

    public void addTag(List<String> tag) {
        sortedTags.addAll(tag);
    }

    public void removeTag(String tag) {
        sortedTags.remove(tag);
    }

    public void removeTag(List<String> tags) {
        this.sortedTags.beginBatchedUpdates();
        for (String tag : tags) {
            this.sortedTags.remove(tag);
            this.tags.remove(tag);
            this.selectedItems.remove(tag);
        }
        this.sortedTags.endBatchedUpdates();
    }

    public void setSelected(String tag, boolean selected) {
        if (selected) {
            selectedItems.add(tag);
            if (itemSelectedListener != null)
                itemSelectedListener.onItemSelected(tag, selectedItems.size());
        } else {
            selectedItems.remove(tag);
            if (itemSelectedListener != null)
                itemSelectedListener.onItemDeselected(tag, selectedItems.size());
        }
    }

    public void updateTags(List<String> tags) {
        this.sortedTags.beginBatchedUpdates();

        for (int i = this.sortedTags.size() - 1; i >= 0; i--) {
            final String tag = this.sortedTags.get(i);
            if (!tags.contains(tag)) {
                this.sortedTags.remove(tag);
                Log.i("Search Adapter", tag + " removed from list");
            }
        }
        this.sortedTags.addAll(tags);
        this.sortedTags.endBatchedUpdates();
    }

    @Override
    public StringSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_search_result, parent, false);
        return new StringSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StringSearchViewHolder holder, int position) {
        final String tag = sortedTags.get(position);
        holder.searchItem.setText(tag);
        holder.searchItem.setChecked(selectedItems.contains(tag));

        holder.searchItem.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSelected(tag, isChecked);
                if (itemSelectedListener != null)
                    itemSelectedListener.onItemSelectionChanged(selectedItems.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return sortedTags.size();
    }

    public void setItemClickListener(ListItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setSearchQuery(String query) {
        query = query.toLowerCase();

        ArrayList<String> tempList = new ArrayList<>();

        for (String tag : tags)
            if (tag.contains(query)) tempList.add(tag);

        updateTags(tempList);
    }

    public ArrayList<String> getSelectedItems() {
        ArrayList<String> output = new ArrayList<>();

        output.addAll(selectedItems);

        output.retainAll(tags);
        return output;
    }

    private ArrayList<String> getDumbList(SortedList<String> tags) {
        ArrayList<String> output = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            output.add(tags.get(i));
        }
        return output;
    }

    public void setItemSelectedListener(ListItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    public boolean isTagPresent(String tag) {
        return tags.contains(tag);
    }

    public static class StringSearchViewHolder extends RecyclerView.ViewHolder {

        private CheckBox searchItem;

        public StringSearchViewHolder(View view) {
            super(view);
            searchItem = (CheckBox) view.findViewById(R.id.search_item);
        }

    }

    interface ListItemSelectedListener {
        void onItemSelected(String tag, int totalSelectedItems);

        void onItemDeselected(String tag, int totalSelectedItems);

        void onItemSelectionChanged(int totalSelectedItems);
    }
}