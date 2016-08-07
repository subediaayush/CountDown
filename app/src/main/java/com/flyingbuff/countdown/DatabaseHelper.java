package com.flyingbuff.countdown;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Aayush on 8/7/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = Countdown.PACKAGE_NAME;
    private static final Integer DATABASE_VERSION = 1;

    private Context context;

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
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query;

        query = String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%s STRING, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER);",
                Countdown.TABLE_TIMER,
                Countdown.COLUMN_ID,
                Countdown.COLUMN_NAME,
                Countdown.COLUMN_INIT,
                Countdown.COLUMN_END,
                Countdown.COLUMN_RESUMED_AT,
                Countdown.COLUMN_STOPPED_AT,
                Countdown.COLUMN_GOAL,
                Countdown.COLUMN_SINGLE_USE
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
        values.put(Countdown.COLUMN_INIT, timer.getInit());
        values.put(Countdown.COLUMN_END, timer.getEnd());
        values.put(Countdown.COLUMN_RESUMED_AT, timer.getResumedAt());
        values.put(Countdown.COLUMN_STOPPED_AT, timer.getStoppedAt());
        values.put(Countdown.COLUMN_GOAL, timer.getGoal());
        values.put(Countdown.COLUMN_SINGLE_USE, timer.isSingleUse());

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
    public int editTimer(int timerId, String column, Object value) {
        int rowCount = -1;

        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues arg = new ContentValues();
            if (value instanceof Boolean) arg.put(column, (Boolean) value);
            else if (value instanceof Long) arg.put(column, (Long) value);
            else if (value instanceof String) arg.put(column, (String) value);
            else arg.put(column, (Integer) value);

            rowCount = db.update(
                    Countdown.TABLE_TIMER,
                    arg,
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
                "SELECT * FROM %s WHERE %s = \"%d\" AND %s = \"%d\";",
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
            Long resumedAt = c.getLong(c.getColumnIndex(Countdown.COLUMN_RESUMED_AT));
            Long stoppedAt = c.getLong(c.getColumnIndex(Countdown.COLUMN_STOPPED_AT));
            Long goal = c.getLong(c.getColumnIndex(Countdown.COLUMN_GOAL));
            Boolean single_use = c.getInt(c.getColumnIndex(Countdown.COLUMN_SINGLE_USE)) == 1;

            Log.i("DatabaseHelper", "Retrieved 1 timer");
            timer = new Timer(id, name, init, end, resumedAt, stoppedAt, goal, single_use);
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
                Long resumedAt = c.getLong(c.getColumnIndex(Countdown.COLUMN_RESUMED_AT));
                Long stoppedAt = c.getLong(c.getColumnIndex(Countdown.COLUMN_STOPPED_AT));
                Long goal = c.getLong(c.getColumnIndex(Countdown.COLUMN_GOAL));
                Boolean single_use = c.getInt(c.getColumnIndex(Countdown.COLUMN_SINGLE_USE)) == 1;

                timers.add(new Timer(id, name, init, end, resumedAt, stoppedAt, goal, single_use));

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


    private interface Patch {
        void apply(SQLiteDatabase db);

        void revert(SQLiteDatabase db);
    }
}
