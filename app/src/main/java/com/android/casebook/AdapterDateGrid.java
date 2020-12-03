package com.android.casebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.android.casebook.ViewRegister.TAG;

public class AdapterDateGrid extends ArrayAdapter {
    List<Date> dates;
    Calendar currentDate;
    List<ModelEvents> events;
    LayoutInflater inflater;

    Calendar calendar = Calendar.getInstance();
    Date todayDate = calendar.getTime();
    SimpleDateFormat checkFormat = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
    String todayFormattedDate = checkFormat.format(todayDate);

    public AdapterDateGrid(@NonNull Context context, List<Date> dates, Calendar currentDate, List<ModelEvents> events) {
        super(context, R.layout.single_cell);

        this.dates=dates;
        this.currentDate=currentDate;
        this.events=events;
        inflater = LayoutInflater.from(context);
    }


    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Date monthDate = dates.get(position);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(monthDate);
        int DayNo = dateCalendar.get(Calendar.DAY_OF_MONTH);
        int displayMonth = dateCalendar.get(Calendar.MONTH)+1;
        int displayYear = dateCalendar.get(Calendar.YEAR);

        int currentMonth = currentDate.get(Calendar.MONTH)+1;
        int currentYear = currentDate.get(Calendar.YEAR);
        int todayDate = currentDate.get(Calendar.DAY_OF_MONTH);

        View view = convertView;
        if (view ==null){
            view = inflater.inflate(R.layout.single_cell,parent,false);
        }

        if(displayMonth == currentMonth && displayYear == currentYear){
            view.setBackgroundColor(getContext().getResources().getColor(R.color.calendarCell));
        }
        else
        {
            view.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        final String userID = fAuth.getCurrentUser().getUid();

        final CollectionReference collectionReference = fStore.collection("users").document(userID)
                .collection("casesID");

        final TextView Day_Number = view.findViewById(R.id.calendar_day);
        final TextView EventNumber = view.findViewById(R.id.events_id);
        Day_Number.setText(String.valueOf(DayNo));
        Calendar eventCalendar = Calendar.getInstance();
        ArrayList<String> arrayList = new ArrayList<>();

        final String DateCheck = displayYear + "-" + displayMonth + "-" + DayNo;
        for (int i = 0; i < events.size(); i++){

            eventCalendar.setTime(ConvertStringToDate(events.get(i).getDATE()));
            if(DayNo == eventCalendar.get(Calendar.DAY_OF_MONTH) && displayMonth == eventCalendar.get(Calendar.MONTH)+1
                    && displayYear == eventCalendar.get(Calendar.YEAR)){

                    arrayList.add(events.get(i).getEVENT());

                    Query query = collectionReference.whereEqualTo("case_date", DateCheck)
                            .whereEqualTo("dispose", false);

                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                final int count = task.getResult().size();
                                if (count != 0) EventNumber.setText(String.valueOf(count));

                                if (DateCheck.equals(todayFormattedDate)) {
                                    collectionReference.whereEqualTo("prev_date", DateCheck)
                                            .whereEqualTo("dispose", false).get().
                                            addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                                                    if (Objects.requireNonNull(task2.getResult()).size() != 0) {
                                                        int total = task2.getResult().size() + count;
                                                        EventNumber.setText(String.valueOf(total));
                                                    } else {
                                                        if (count != 0) EventNumber.setText(String.valueOf(count));
                                                    }
                                                }
                                            });
                                } else {
                                    if (count != 0) EventNumber.setText(String.valueOf(count));
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: "+ e.getMessage());
                        }
                    });
            }
        }

        DateFormat todayMonthFormat = new SimpleDateFormat("M");
        DateFormat todayYearFormat = new SimpleDateFormat("yyyy");

        Date date = new Date();
        int todayMonth = Integer.parseInt(todayMonthFormat.format(date));
        int todayYear = Integer.parseInt(todayYearFormat.format(date));

        if(displayMonth == todayMonth && displayYear == todayYear && DayNo == todayDate){
            view.setBackgroundColor(getContext().getResources().getColor(R.color.todayDate));
        }
        return view;
    }


    private Date ConvertStringToDate(String eventDate){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }


    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public int getPosition(@Nullable Object item) {
        return dates.indexOf(item);
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return dates.get(position);
    }
}

