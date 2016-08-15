package com.flyingbuff.countdown;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Aayush on 8/7/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = Countdown.PACKAGE_NAME;
    private static final Integer DATABASE_VERSION = 1;

    private Patch PATCHES[] = new Patch[]{
            new Patch() {
                @Override
                public void apply(SQLiteDatabase db) {
                    onCreate(db);
                }

                @Override
                public void revert(SQLiteDatabase db) {

                }
            }
    };

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query;

        query = String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%s VARCHAR, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, " +
                        "%s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER);",
                Countdown.TABLE_TIMER,
                Countdown.COLUMN_ID,
                Countdown.COLUMN_NAME,
                Countdown.COLUMN_INIT,
                Countdown.COLUMN_END,
                Countdown.COLUMN_PAUSED_AT,
                Countdown.COLUMN_RESUMED_AT,
                Countdown.COLUMN_DURATION,
                Countdown.COLUMN_ELAPSED,
                Countdown.COLUMN_SINGLE_USE,
                Countdown.COLUMN_NOTIFY,
                Countdown.COLUMN_SILENT,
                Countdown.COLUMN_PAUSED
        );
        Log.i("Query", query);
        db.execSQL(query);

        query = String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%s INTEGER, %s VARCHAR);",
                Countdown.TABLE_ALERT,
                Countdown.COLUMN_ID,
                Countdown.COLUMN_TIMER_ID,
                Countdown.COLUMN_URI
        );

        Log.i("Query", query);
        db.execSQL(query);

        query = String.format(
                "CREATE TABLE %s (%s VARCHAR PRIMARY KEY);",
                Countdown.TABLE_TAGS,
                Countdown.COLUMN_TAG
        );

        Log.i("Query", query);
        db.execSQL(query);

        query = String.format(
                "CREATE TABLE %s (%s VARCHAR, %s INTEGER);",
                Countdown.TABLE_TAG_REFERENCE,
                Countdown.COLUMN_TAG,
                Countdown.COLUMN_TIMER_ID
        );

        Log.i("Query", query);
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; i++)
            PATCHES[i].apply(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; i--)
            PATCHES[i].revert(db);
    }

    // Save a timer object to database
    public int saveTimer(Timer timer) {
        ContentValues values = new ContentValues();

        // If new alert then insert default id
        if (timer.getId() != -1) values.put(Countdown.COLUMN_ID, timer.getId());

        // Insert other parameters
        values.put(Countdown.COLUMN_NAME, timer.getName());
        values.put(Countdown.COLUMN_INIT, timer.getStart());
        values.put(Countdown.COLUMN_END, timer.getEnd());
        values.put(Countdown.COLUMN_PAUSED_AT, timer.getPausedAt());
        values.put(Countdown.COLUMN_RESUMED_AT, timer.getResumedAt());
        values.put(Countdown.COLUMN_DURATION, timer.getDuration());
        values.put(Countdown.COLUMN_ELAPSED, timer.getElapsed());
        values.put(Countdown.COLUMN_SINGLE_USE, timer.isRepeat());
        values.put(Countdown.COLUMN_NOTIFY, timer.isNotify());
        values.put(Countdown.COLUMN_SILENT, timer.isSilent());
        values.put(Countdown.COLUMN_PAUSED, timer.isPaused());

        int row_id = -1;
        try (SQLiteDatabase db = getWritableDatabase()) {
            // Update an alert if already exists
            row_id = (int) db.insertWithOnConflict(
                    Countdown.TABLE_TIMER,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
        } catch (SQLiteException e) {
            Log.e("Error on table " + Countdown.TABLE_TIMER, e.toString());
        }
        return row_id;
    }

    // Edit a single field of a timer
    public int editTimer(int timerId, ContentValues args) {
        int rowCount = -1;

        try (SQLiteDatabase db = getWritableDatabase()) {

            rowCount = db.update(
                    Countdown.TABLE_TIMER,
                    args,
                    Countdown.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(timerId)}
            );
        } catch (Exception e) {
            Log.e("Database Helper", "Could not update timer " + timerId, e);
        }
        return rowCount;
    }

    // Load a timer with given id
    public Timer loadTimer(Integer timerId) {
        String Query = String.format(Locale.getDefault(),
                "SELECT * FROM %s WHERE %s = \"%d\";",
                Countdown.TABLE_TIMER,
                Countdown.COLUMN_ID,
                timerId
        );

        Log.i("Query", Query);


        Timer timer = null;

        try (SQLiteDatabase db = getReadableDatabase(); Cursor c = db.rawQuery(Query, null)) {
            c.moveToFirst();

            Integer id = c.getInt(c.getColumnIndex(Countdown.COLUMN_ID));
            String name = c.getString(c.getColumnIndex(Countdown.COLUMN_NAME));
            Long init = c.getLong(c.getColumnIndex(Countdown.COLUMN_INIT));
            Long end = c.getLong(c.getColumnIndex(Countdown.COLUMN_END));
            Long paused_at = c.getLong(c.getColumnIndex(Countdown.COLUMN_PAUSED_AT));
            Long resumed_at = c.getLong(c.getColumnIndex(Countdown.COLUMN_RESUMED_AT));
            Long duration = c.getLong(c.getColumnIndex(Countdown.COLUMN_DURATION));
            Long elapsed = c.getLong(c.getColumnIndex(Countdown.COLUMN_ELAPSED));
            Boolean single_use = c.getInt(c.getColumnIndex(Countdown.COLUMN_SINGLE_USE)) == 1;
            Boolean notify = c.getInt(c.getColumnIndex(Countdown.COLUMN_NOTIFY)) == 1;
            Boolean silent = c.getInt(c.getColumnIndex(Countdown.COLUMN_SILENT)) == 1;
            Boolean paused = c.getInt(c.getColumnIndex(Countdown.COLUMN_PAUSED)) == 1;

            Log.i("DatabaseHelper", "Retrieved 1 timer");
            timer = new Timer(id, name, init, end, paused_at, resumed_at, duration,
                    elapsed, single_use, notify, silent, paused);
        } catch (SQLiteException e) {
            Log.e("DatabaseHelper", "Error while retrieving timer", e);
        }
        return timer;
    }

    // Load all timers
    public ArrayList<Timer> loadTimer() {
        String Query = String.format(Locale.getDefault(),
                "SELECT * FROM %s;",
                Countdown.TABLE_TIMER
        );

        Log.i("Query", Query);

        Timer timer = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(Query, null);

        c.moveToFirst();
        ArrayList<Timer> timers = new ArrayList<>();
        while (!c.isAfterLast()) {
            try {
                Integer id = c.getInt(c.getColumnIndex(Countdown.COLUMN_ID));
                String name = c.getString(c.getColumnIndex(Countdown.COLUMN_NAME));
                Long init = c.getLong(c.getColumnIndex(Countdown.COLUMN_INIT));
                Long end = c.getLong(c.getColumnIndex(Countdown.COLUMN_END));
                Long paused_at = c.getLong(c.getColumnIndex(Countdown.COLUMN_PAUSED_AT));
                Long resumed_at = c.getLong(c.getColumnIndex(Countdown.COLUMN_RESUMED_AT));
                Long duration = c.getLong(c.getColumnIndex(Countdown.COLUMN_DURATION));
                Long elapsed = c.getLong(c.getColumnIndex(Countdown.COLUMN_ELAPSED));
                Boolean single_use = c.getInt(c.getColumnIndex(Countdown.COLUMN_SINGLE_USE)) == 1;
                Boolean notify = c.getInt(c.getColumnIndex(Countdown.COLUMN_NOTIFY)) == 1;
                Boolean silent = c.getInt(c.getColumnIndex(Countdown.COLUMN_SILENT)) == 1;
                Boolean paused = c.getInt(c.getColumnIndex(Countdown.COLUMN_PAUSED)) == 1;

                Log.i("DatabaseHelper", "Retrieved 1 timer");

                timers.add(new Timer(id, name, init, end, paused_at, resumed_at, duration,
                        elapsed, single_use, notify, silent, paused));

                c.moveToNext();
            } catch (SQLiteException e) {
                Log.e("DatabaseHelper", "Error while loading timer", e);
            }
        }
        c.close();
        db.close();
        return timers;
    }

    // Load all timers
    public ArrayList<Timer> loadTimer(String tag) {
        String Query = String.format(Locale.getDefault(),
                "SELECT * FROM %s JOIN %s WHERE %s = %s;",
                Countdown.TABLE_TIMER,
                Countdown.TABLE_TAG_REFERENCE,
                Countdown.COLUMN_ID,
                Countdown.COLUMN_TIMER_ID
        );

        Log.i("Query", Query);

        Timer timer = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(Query, null);

        c.moveToFirst();
        ArrayList<Timer> timers = new ArrayList<>();
        while (!c.isAfterLast()) {
            try {
                Integer id = c.getInt(c.getColumnIndex(Countdown.COLUMN_ID));
                String name = c.getString(c.getColumnIndex(Countdown.COLUMN_NAME));
                Long init = c.getLong(c.getColumnIndex(Countdown.COLUMN_INIT));
                Long end = c.getLong(c.getColumnIndex(Countdown.COLUMN_END));
                Long paused_at = c.getLong(c.getColumnIndex(Countdown.COLUMN_PAUSED_AT));
                Long resumed_at = c.getLong(c.getColumnIndex(Countdown.COLUMN_RESUMED_AT));
                Long duration = c.getLong(c.getColumnIndex(Countdown.COLUMN_DURATION));
                Long elapsed = c.getLong(c.getColumnIndex(Countdown.COLUMN_ELAPSED));
                Boolean single_use = c.getInt(c.getColumnIndex(Countdown.COLUMN_SINGLE_USE)) == 1;
                Boolean notify = c.getInt(c.getColumnIndex(Countdown.COLUMN_NOTIFY)) == 1;
                Boolean silent = c.getInt(c.getColumnIndex(Countdown.COLUMN_SILENT)) == 1;
                Boolean paused = c.getInt(c.getColumnIndex(Countdown.COLUMN_PAUSED)) == 1;

                Log.i("DatabaseHelper", "Retrieved 1 timer");

                timers.add(new Timer(id, name, init, end, paused_at, resumed_at, duration,
                        elapsed, single_use, notify, silent, paused));

                c.moveToNext();
            } catch (SQLiteException e) {
                Log.e("DatabaseHelper", "Error while loading timer", e);
            }
        }
        c.close();
        db.close();
        return timers;
    }

    public boolean removeTimer(Integer timerId) {
        String Query = String.format(Locale.getDefault(),
                "DELETE FROM %s WHERE %s=\"%d\";",
                Countdown.TABLE_TIMER,
                Countdown.COLUMN_ID,
                timerId
        );
        Log.i("Query", Query);

        try (SQLiteDatabase db = getWritableDatabase()) {
            db.execSQL(Query);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int saveTag(String tag) {
        ContentValues arg = new ContentValues();
        arg.put(Countdown.COLUMN_TAG, tag.trim().toLowerCase());

        int row;
        try (SQLiteDatabase db = getWritableDatabase()) {
            row = (int) db.insertWithOnConflict(Countdown.TABLE_TAGS, null, arg, SQLiteDatabase.CONFLICT_IGNORE);
        }
        return row;
    }

    public ArrayList<String> loadTags() {
        String query = String.format(
                "SELECT %s FROM %s;",
                Countdown.COLUMN_TAG,
                Countdown.TABLE_TAGS
        );
        Log.i("Query", query);

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        c.moveToFirst();
        ArrayList<String> tags = new ArrayList<>();

        while (!c.isAfterLast()) {
            try {
                tags.add(c.getString(c.getColumnIndex(Countdown.COLUMN_TAG)));
                c.moveToNext();
            } catch (SQLiteException e) {
                Log.e("DatabaseHelper", "Error while loading tags", e);
            }
        }
        c.close();
        db.close();

        return tags;
    }

    public ArrayList<String> loadTags(Timer timer) {
        String query = String.format(Locale.getDefault(),
                "SELECT %s FROM %s WHERE %s = %d;",
                Countdown.COLUMN_TAG,
                Countdown.TABLE_TAG_REFERENCE,
                Countdown.COLUMN_TIMER_ID,
                timer.getId()
        );
        Log.i("Query", query);

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        c.moveToFirst();
        ArrayList<String> tags = new ArrayList<>();

        while (!c.isAfterLast()) {
            try {
                tags.add(c.getString(c.getColumnIndex(Countdown.COLUMN_TAG)));
                c.moveToNext();
            } catch (SQLiteException e) {
                Log.e("DatabaseHelper", "Error while loading tags", e);
            }
        }
        c.close();
        db.close();

        return tags;
    }

    public void assignTone(Timer timer, Uri tone) {
        int id = timer.getId();
        if (id < 0) return;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_TIMER_ID, id);
        args.put(Countdown.COLUMN_URI, tone.toString());

        try (SQLiteDatabase db = getWritableDatabase()) {
            db.insertWithOnConflict(Countdown.TABLE_ALERT, null, args, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public void removeTag(ArrayList<String> tags) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            for (String tag : tags) {
//                String query = String.format(
//                        "DELETE FROM %s WHERE %s = %s",
//                        Countdown.TABLE_TAGS,
//                        Countdown.COLUMN_TAG,
//                        tag
//                );
                db.delete(
                        Countdown.TABLE_TAGS,
                        Countdown.COLUMN_TAG + " = ?",
                        new String[]{tag}
                );

                db.delete(
                        Countdown.TABLE_TAG_REFERENCE,
                        Countdown.COLUMN_TAG + " = ?",
                        new String[]{tag}
                );
            }
        }
    }

    public void assignTag(Timer timer, ArrayList<String> tags) {
        SQLiteDatabase db = getWritableDatabase();
        int timerId = timer.getId();

        for (String tag : tags) {
            try {
                ContentValues args = new ContentValues();
                args.put(Countdown.COLUMN_TAG, tag);
                args.put(Countdown.COLUMN_TIMER_ID, timerId);
                db.insert(Countdown.TABLE_TAG_REFERENCE, null, args);
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error while assigning " + tag + " to timer", e);
            }
        }

    }

    private interface Patch {
        void apply(SQLiteDatabase db);

        void revert(SQLiteDatabase db);
    }
}
