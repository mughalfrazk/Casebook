package com.android.casebook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ViewEventCalendar extends AppCompatActivity {

    EventCalendar eventCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        eventCalendar = findViewById(R.id.event_calendar_view);
    }
}