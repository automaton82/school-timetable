package com.systems.automaton.classtimetableplanner.activities;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.systems.automaton.classtimetableplanner.R;
import com.systems.automaton.classtimetableplanner.adapters.HomeworkAdapter;
import com.systems.automaton.classtimetableplanner.ads.AdManager;
import com.systems.automaton.classtimetableplanner.model.Homework;
import com.systems.automaton.classtimetableplanner.profiles.ProfileManagement;
import com.systems.automaton.classtimetableplanner.utils.AlertDialogsHelper;
import com.systems.automaton.classtimetableplanner.utils.DbHelper;
import com.systems.automaton.classtimetableplanner.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.Objects;


public class HomeworkActivity extends AppCompatActivity {
    public static final String ACTION_ADD_HOMEWORK = "addHomework";

    @NonNull
    private final AppCompatActivity context = this;
    private ListView listView;
    private HomeworkAdapter adapter;
    private DbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtil.getGeneralTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homeworks);
        if (ACTION_ADD_HOMEWORK.equalsIgnoreCase(getIntent().getAction())) {
            db = new DbHelper(this, ProfileManagement.loadPreferredProfilePosition());
            initAll();
            findViewById(R.id.fab).performClick();
        } else {
            db = new DbHelper(context);
            initAll();
        }
    }

    private void initAll() {
        setupAdapter();
        setupListViewMultiSelect();
        setupCustomDialog();
        AdManager.instance.createAdView(getApplicationContext(), findViewById(R.id.ad_container));
    }

    private void setupAdapter() {
        listView = findViewById(R.id.homeworklist);
        adapter = new HomeworkAdapter(db, HomeworkActivity.this, listView, R.layout.listview_homeworks_adapter, db.getHomework());
        listView.setAdapter(adapter);
    }

    private void setupListViewMultiSelect() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(@NonNull ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(checkedCount + " " + getResources().getString(R.string.selected));
                if (checkedCount == 0) mode.finish();
            }

            @Override
            public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.toolbar_action_mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(@NonNull final ActionMode mode, @NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    ArrayList<Homework> removelist = new ArrayList<>();
                    SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                    for (int i = 0; i < checkedItems.size(); i++) {
                        int key = checkedItems.keyAt(i);
                        if (checkedItems.get(key)) {
                            db.deleteHomeworkById(Objects.requireNonNull(adapter.getItem(key)));
                            removelist.add(adapter.getHomeworkList().get(key));
                        }
                    }
                    adapter.getHomeworkList().removeAll(removelist);
                    db.updateHomework(adapter.getHomework());
                    adapter.notifyDataSetChanged();
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }

    private void setupCustomDialog() {
        final View alertLayout = getLayoutInflater().inflate(R.layout.dialog_add_homework, null);
        AlertDialogsHelper.getAddHomeworkDialog(db, HomeworkActivity.this, alertLayout, adapter);
    }
}
