package com.flyingbuff.countdown;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Aayush on 8/14/2016.
 */
public class SearchTagActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    HashSet<String> selectedTags = new HashSet<>();

    RecyclerView searchResultList;
    SearchAdapter adapter;

    String searchItem;
    private RadioButton addNewTag;
    private Button applyTags;
    private Button deleteTags;
    private ButtonBarLayout actionsButtonContainer;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_tags);

        ArrayList<String> inputTags = getIntent().getStringArrayListExtra(Countdown.KEY_TAG);
        if (inputTags != null) selectedTags.addAll(inputTags);

        databaseHelper = new DatabaseHelper(this);

        addNewTag = (RadioButton) findViewById(R.id.add_new_tag);
        applyTags = (Button) findViewById(R.id.apply_tag_button);
//        deleteTags = (Button) findViewById(R.id.delete_tag_button);

        actionsButtonContainer = (ButtonBarLayout) findViewById(R.id.tag_action_button_container);

        searchResultList = (RecyclerView) findViewById(R.id.search_list);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));
        searchResultList.addItemDecoration(new SimpleItemDecoration(this));

        adapter = new SearchAdapter(this, selectedTags);

        adapter.setItemSelectedListener(new SearchAdapter.ListItemSelectedListener() {
            @Override
            public void onItemSelected(String tag, int totalSelectedItems) {

            }

            @Override
            public void onItemDeselected(String tag, int totalSelectedItems) {
            }

            @Override
            public void onItemSelectionChanged(int totalSelectedItems) {
                actionsButtonContainer.setVisibility(View.VISIBLE);
            }
        });

        searchResultList.setAdapter(adapter);

//        deleteTags.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ArrayList<String> selectedTags = adapter.getSelectedItems();
//
//                adapter.removeTag(selectedTags);
//                databaseHelper.removeTag(selectedTags);
//            }
//        });

        applyTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTags.clear();
                selectedTags.addAll(adapter.getSelectedItems());

                ArrayList<String> output = new ArrayList<>();
                output.addAll(selectedTags);

                Intent result = new Intent();
                result.putExtra(Countdown.KEY_TAG, output);

                setResult(RESULT_OK, result);
                finish();
            }
        });

        addNewTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.saveTag(searchItem);

                selectedTags.add(searchItem);

                ArrayList<String> output = new ArrayList<String>();
                output.addAll(selectedTags);

                Intent result = new Intent();
                result.putExtra(Countdown.KEY_TAG, output);

                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Tags");
        searchView.requestFocus();
        searchView.setOnQueryTextListener(this);

        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        if (!adapter.isTagPresent(newQuery)) {
            if (newQuery.isEmpty()) {
                addNewTag.setVisibility(View.GONE);
            } else {
                addNewTag.setText("Add \"" + newQuery + "\" tag");
                addNewTag.setVisibility(View.VISIBLE);
            }
        }

        searchItem = newQuery;
        adapter.setSearchQuery(newQuery);

        return true;
    }
}
