package com.android.casebook;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBOpenHelperCalendar extends SQLiteOpenHelper {

    private static final String CREATE_EVENTS_TABLE = "create table "+DBStructureCalendar.EVENT_TABLE_NAME+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, "
            +DBStructureCalendar.EVENT+" TEXT, "+DBStructureCalendar.TIME+" TEXT, "+DBStructureCalendar.DATE+" TEXT, "+DBStructureCalendar.MONTH+" TEXT, "
            +DBStructureCalendar.YEAR+" TEXT)";
    private static final String DROP_EVENTS_TABLE= "DROP TABLE IF EXISTS "+DBStructureCalendar.EVENT_TABLE_NAME;

    @SuppressLint("SdCardPath")
    public DBOpenHelperCalendar(@Nullable Context context) {
        super(context, DBStructureCalendar.DB_NAME, null, DBStructureCalendar.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_EVENTS_TABLE);
        onCreate(db);
    }

    public void SaveEvent(String event, String time, String date, String month, String year, SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBStructureCalendar.EVENT, event);
        contentValues.put(DBStructureCalendar.TIME, time);
        contentValues.put(DBStructureCalendar.DATE, date);
        contentValues.put(DBStructureCalendar.MONTH, month);
        contentValues.put(DBStructureCalendar.YEAR, year);
        database.insert(DBStructureCalendar.EVENT_TABLE_NAME, null, contentValues);
    }


    public Cursor ReadEvents(String date, SQLiteDatabase database) {
        String [] Projections = {DBStructureCalendar.EVENT, DBStructureCalendar.TIME, DBStructureCalendar.DATE, DBStructureCalendar.YEAR, DBStructureCalendar.MONTH};
        String Selection = DBStructureCalendar.DATE +"=?";
        String [] SelectionArgs = {date};

        return database.query(DBStructureCalendar.EVENT_TABLE_NAME, Projections, Selection, SelectionArgs, null, null, null);
    }

    public Cursor ReadEventsperMonth(String month,String year,SQLiteDatabase database){
        String [] Projections = {DBStructureCalendar.EVENT,DBStructureCalendar.TIME,DBStructureCalendar.DATE,DBStructureCalendar.MONTH,DBStructureCalendar.YEAR};
        String Selection = DBStructureCalendar.MONTH +"=? and "+DBStructureCalendar.YEAR+"=?";
        String [] SelectionArgs = {month,year};
        return database.query(DBStructureCalendar.EVENT_TABLE_NAME,Projections,Selection,SelectionArgs,null,null,null);
    }
}
