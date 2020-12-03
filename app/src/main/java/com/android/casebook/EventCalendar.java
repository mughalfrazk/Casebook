package com.android.casebook;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventCalendar extends LinearLayout {
    private static final String TAG = null;
    private static DBOpenHelperCalendar dbOpenHelper;
    private static Context context;
    public static boolean DateChecker;

    ImageView nextBTN, previousBTN;
    TextView CurrentDate, countCases;
    GridView datesGrid;
    private static final int MAX_CALENDAR_DAYS = 42;
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    SimpleDateFormat dFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    SimpleDateFormat eventDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    SimpleDateFormat checkFormat = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
    SimpleDateFormat intMonthFormat = new SimpleDateFormat("MM", Locale.ENGLISH);

    String checkDate, date, month, year, oDate, intMonth;

    AdapterDateGrid dateGridAdapter;

    List<Date> dates = new ArrayList<>();
    List<ModelEvents> eventsList = new ArrayList<>();

    public EventCalendar(Context context) {
        super(context);
    }

    public EventCalendar(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        InitiatizeLayout();

        previousBTN.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, -1);
                DateChecker = false;
                SetUpCalendar();
            }
        });

        nextBTN.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, 1);
                DateChecker = false;
                SetUpCalendar();
            }
        });

        datesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkDate = checkFormat.format(dates.get(position));
                date = eventDateFormat.format(dates.get(position));
                month = monthFormat.format(dates.get(position));
                year = yearFormat.format(dates.get(position));
                oDate = dFormat.format(dates.get(position));
                intMonth = intMonthFormat.format(dates.get(position));

                SetUpCalendar();
                Intent intent = new Intent(view.getContext(), ViewAddNew.class);
                intent.putExtra("date", date);
                intent.putExtra("month", month);
                intent.putExtra("year", year);
                intent.putExtra("oDate", oDate);
                intent.putExtra("checkDate", checkDate);
                intent.putExtra("intMonth", intMonth);

                context.startActivity(intent);
            }

        });

        datesGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                checkDate = checkFormat.format(dates.get(position));
                date = eventDateFormat.format(dates.get(position));
                month = monthFormat.format(dates.get(position));
                year = yearFormat.format(dates.get(position));
                oDate = dFormat.format(dates.get(position));
                intMonth = intMonthFormat.format(dates.get(position));

                Intent intent = new Intent(getContext(), ViewSelectedDate.class);
                intent.putExtra("SelectedDate", checkDate);
                intent.putExtra("date", date);
                intent.putExtra("month", month);
                intent.putExtra("year", year);
                intent.putExtra("oDate", oDate);
                intent.putExtra("intMonth", intMonth);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(context.getApplicationContext(), R.anim.fadein, R.anim.fadeout);
                context.startActivity(intent, options.toBundle());

                Toast.makeText(getContext(), "Date: "+ date, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private ArrayList<ModelEvents> CollectEventByDate(String date) {
        ArrayList<ModelEvents> arrayList = new ArrayList<>();
        dbOpenHelper = new DBOpenHelperCalendar(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadEvents(date, database);
        while (cursor.moveToNext()) {
            String event = cursor.getString(cursor.getColumnIndex(DBStructureCalendar.EVENT));
            String time = cursor.getString(cursor.getColumnIndex(DBStructureCalendar.TIME));
            String Date = cursor.getString(cursor.getColumnIndex(DBStructureCalendar.DATE));
            String Year = cursor.getString(cursor.getColumnIndex(DBStructureCalendar.YEAR));
            String month = cursor.getString(cursor.getColumnIndex(DBStructureCalendar.MONTH));
            ModelEvents events = new ModelEvents(event, time, Date, month, Year);
            arrayList.add(events);
        }
        cursor.close();
        dbOpenHelper.close();

        return arrayList;
    }

    public EventCalendar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    static void SaveEvent(String event, String time, String date, String month, String year) {
        dbOpenHelper = new DBOpenHelperCalendar(context);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.SaveEvent(event, time, date, month, year, database);
        dbOpenHelper.close();
    }

    void InitiatizeLayout(){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.event_calendar, this);
        nextBTN = view.findViewById(R.id.nextBTN);
        previousBTN = view.findViewById(R.id.previousBTN);
        CurrentDate = view.findViewById(R.id.current_Date);
        datesGrid = view.findViewById(R.id.datesGrid);
        SetUpCalendar();
    }

    void SetUpCalendar(){
        String currentDate = dateFormat.format(calendar.getTime());
        CurrentDate.setText(currentDate);
        dates.clear();
        Calendar monthCalendar= (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH,1);
        int FirstDayofMonth = monthCalendar.get(Calendar.DAY_OF_WEEK)-1;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -FirstDayofMonth);
        CollectEventsPerMonth(monthFormat.format(calendar.getTime()),yearFormat.format(calendar.getTime()));

        for (int i=0; dates.size() < MAX_CALENDAR_DAYS; i++){
            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        dateGridAdapter = new AdapterDateGrid(context,dates,calendar,eventsList);
        datesGrid.setAdapter(dateGridAdapter);

    }

    private void CollectEventsPerMonth(String Month, String year) {
        dbOpenHelper = new DBOpenHelperCalendar(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadEventsperMonth(Month, year, database);
        while ( (cursor.moveToNext())) {
            String event = cursor.getString(cursor.getColumnIndex(DBStructureCalendar.EVENT));
            String time = cursor.getString(cursor.getColumnIndex(DBStructureCalendar.TIME));
            String date = cursor.getString(cursor.getColumnIndex(DBStructureCalendar.DATE));
            String Year = cursor.getString(cursor.getColumnIndex(DBStructureCalendar.YEAR));
            String month = cursor.getString(cursor.getColumnIndex(DBStructureCalendar.MONTH));

            ModelEvents events = new ModelEvents(event, time, date, Year, month);
            eventsList.add(events);
        }
        cursor.close();
        dbOpenHelper.close();
    }

}
