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
                        "%s VARCHAR, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, " +
                        "%s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER);",
                Countdown.TABLE_TIMER,
                Countdown.COLUMN_ID,
                Countdown.COLUMN_NAME,
                Countdown.COLUMN_INIT,
                Countdown.COLUMN_END,
                Countdown.COLUMN_DURATION,
                Countdown.COLUMN_PAUSED_AT,
                Countdown.COLUMN_REMAINING,
                Countdown.COLUMN_ELAPSED,
                Countdown.COLUMN_TIMEOUT,
                Countdown.COLUMN_SINGLE_USE,
                Countdown.COLUMN_PAUSED,
                Countdown.COLUMN_NOTIFY,
                Countdown.COLUMN_SILENT,
                Countdown.COLUMN_TONE
        );
        db.execSQL(query);
        Log.i("Query", query);

        query = String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%s INTEGER, %s VARCHAR);",
                Countdown.TABLE_ALERT,
                Countdown.COLUMN_ID,
                Countdown.COLUMN_TIMER_ID,
                Countdown.COLUMN_URI
        );

        db.execSQL(query);
        Log.i("Query", query);
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
        values.put(Countdown.COLUMN_DURATION, timer.getDuration());
        values.put(Countdown.COLUMN_PAUSED_AT, timer.getPausedAt());
        values.put(Countdown.COLUMN_REMAINING, timer.getRemaining());
        values.put(Countdown.COLUMN_ELAPSED, timer.getElapsed());
        values.put(Countdown.COLUMN_TIMEOUT, timer.getTimeOut());
        values.put(Countdown.COLUMN_SINGLE_USE, timer.isSingleUse());
        values.put(Countdown.COLUMN_PAUSED, timer.isPaused());
        values.put(Countdown.COLUMN_NOTIFY, timer.isNotify());
        values.put(Countdown.COLUMN_SILENT, timer.isSilent());

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
        } catch (Exception e){
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
            Long remaining = c.getLong(c.getColumnIndex(Countdown.COLUMN_REMAINING));
            Long elapsed = c.getLong(c.getColumnIndex(Countdown.COLUMN_ELAPSED));
            Long duration = c.getLong(c.getColumnIndex(Countdown.COLUMN_DURATION));
            Long timeout = c.getLong(c.getColumnIndex(Countdown.COLUMN_TIMEOUT));
            Boolean single_use = c.getInt(c.getColumnIndex(Countdown.COLUMN_SINGLE_USE)) == 1;
            Boolean paused = c.getInt(c.getColumnIndex(Countdown.COLUMN_PAUSED)) == 1;
            Boolean notify = c.getInt(c.getColumnIndex(Countdown.COLUMN_NOTIFY)) == 1;
            Boolean silent = c.getInt(c.getColumnIndex(Countdown.COLUMN_SILENT)) == 1;
            Integer toneId = c.getInt(c.getColumnIndex(Countdown.COLUMN_TONE));

            Log.i("DatabaseHelper", "Retrieved 1 timer");
            timer = new Timer(id, name, init, end, paused_at, duration,
                    remaining, elapsed, timeout, single_use, paused, notify, silent, toneId);
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
                Long remaining = c.getLong(c.getColumnIndex(Countdown.COLUMN_REMAINING));
                Long elapsed = c.getLong(c.getColumnIndex(Countdown.COLUMN_ELAPSED));
                Long duration = c.getLong(c.getColumnIndex(Countdown.COLUMN_DURATION));
                Long timeout = c.getLong(c.getColumnIndex(Countdown.COLUMN_TIMEOUT));
                Boolean single_use = c.getInt(c.getColumnIndex(Countdown.COLUMN_SINGLE_USE)) == 1;
                Boolean paused = c.getInt(c.getColumnIndex(Countdown.COLUMN_PAUSED)) == 1;
                Boolean notify = c.getInt(c.getColumnIndex(Countdown.COLUMN_NOTIFY)) == 1;
                Boolean silent = c.getInt(c.getColumnIndex(Countdown.COLUMN_SILENT)) == 1;
                Integer toneId = c.getInt(c.getColumnIndex(Countdown.COLUMN_TONE));

                timers.add(new Timer(id, name, init, end, paused_at, duration,
                        remaining, elapsed, timeout, single_use, paused, notify, silent, toneId));

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


    private interface Patch {
        void apply(SQLiteDatabase db);

        void revert(SQLiteDatabase db);
    }
}
