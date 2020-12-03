package com.android.casebook;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ViewAddNew extends FragmentActivity {
    public static final String TAG = "TAG";
    private static final int RESULT_PICK_CONTACT = 1;
    private static final int RESULT_PICK_CONTACT_2 = 2;
    EditText cTitle, cName, cType, cNo, cBehalf, cParty, cContact, rName, fSection, aAdvocate, aContact;
    EditText uTitle, uCourt, uType, uNo, uBehalf, uParty, uContact, ufiled;
    TextView cDate;
    Button saveCase, client_Contact, advocate_Contact;
    FirebaseFirestore fStore;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    FirebaseUser currentUser;
    EventCalendar eventCalendar;
    static CollectionReference collectionReference;
    DocumentReference updateCase;
    ViewDashboard viewDashboard;
    static String odateExtra, checkExtra, intMonthExtra, yearExtra;
    String incomingIntent;

    AutoCompleteTextView auto_court, auto_party;
    String[] courtArray, partyArray;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);

        cTitle = uTitle = findViewById(R.id.caseTitle);
        cName = uCourt = findViewById(R.id.courtName);
        cType = uType = findViewById(R.id.caseType);
        cNo = uNo = findViewById(R.id.caseNo);
        cBehalf = uBehalf = findViewById(R.id.onBehalf);
        cParty = uParty = findViewById(R.id.partyName);
        cContact = uContact = findViewById(R.id.contactNo);
        rName = findViewById(R.id.respodentName);
        fSection = ufiled = findViewById(R.id.filedU);
        aAdvocate = findViewById(R.id.adverseAdvocate);
        aContact = findViewById(R.id.advocateContact);
        cDate = findViewById(R.id.txtdate);

        client_Contact = findViewById(R.id.addContact);
        advocate_Contact = findViewById(R.id.addContact2);
        saveCase = findViewById(R.id.saveBtn);
        progressBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        saveCase.setEnabled(true);

        incomingIntent = getIntent().getStringExtra("case_ID");
        if (incomingIntent != null) {
            updateCase(incomingIntent);
        }

        String userID = currentUser.getUid();

        collectionReference = fStore.collection("users").document(userID).collection("casesID");

        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().size() > 0) {
                    courtArray = new String[task.getResult().size()];
                    partyArray = new String[task.getResult().size()];
                    int increment = 0;
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        courtArray[increment] = documentSnapshot.getString("court_name");
                        partyArray[increment] = documentSnapshot.getString("party_name");

                        increment++;
                    }
                    auto_court = findViewById(R.id.courtName);
                    auto_court.setAdapter(new ArrayAdapter<>(ViewAddNew.this, android.R.layout.simple_list_item_1, courtArray));

                    auto_party = findViewById(R.id.partyName);
                    auto_party.setAdapter(new ArrayAdapter<>(ViewAddNew.this, android.R.layout.simple_list_item_1, partyArray));

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });


        if(currentUser != null) {

                final String dateExtra = getIntent().getStringExtra("date");
                final String monthExtra = getIntent().getStringExtra("month");
                yearExtra = getIntent().getStringExtra("year");
                odateExtra = getIntent().getStringExtra("oDate");
                checkExtra = getIntent().getStringExtra("checkDate");
                intMonthExtra = getIntent().getStringExtra("intMonth");

                cDate.setText(dateExtra);

                client_Contact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
                    }
                });

                advocate_Contact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT_2);
                    }
                });


                saveCase.setOnClickListener(new View.OnClickListener() {

                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View v) {
                        try {
                            final String caseTitle = cTitle.getText().toString();
                            final String courtName = cName.getText().toString();
                            final String caseType = cType.getText().toString();
                            final String caseNo = cNo.getText().toString();
                            final String onBehalf = cBehalf.getText().toString();
                            final String partyName = cParty.getText().toString();
                            final String clientContact = cContact.getText().toString();
                            final String filedSection = fSection.getText().toString();

                            if (TextUtils.isEmpty(caseTitle)) {
                                cTitle.setError("Case Title is Required.");
                                return;
                            }

                            if (TextUtils.isEmpty(partyName)) {
                                cParty.setError("Party Name is Required.");
                                return;
                            }

                            if (TextUtils.isEmpty(courtName)) {
                                cName.setError("Court Name is Required.");
                                return;
                            }

                            if (TextUtils.isEmpty(caseType)) {
                                cType.setError("Case Type is Required.");
                                return;
                            }

                            if (TextUtils.isEmpty(clientContact)) {
                                cContact.setError("Case Type is Required.");
                                return;
                            }

                            final Map<String, Object> cases = new HashMap<>();
                            cases.put("case_title", caseTitle);
                            cases.put("court_name", courtName);
                            cases.put("case_type", caseType);
                            cases.put("case_no", caseNo);
                            cases.put("on_behalf", onBehalf);
                            cases.put("party_name", partyName);
                            cases.put("client_contact", clientContact);
                            cases.put("filed_U_Sec", filedSection);
                            cases.put("case_date", checkExtra);
                            cases.put("start_date", checkExtra);
                            cases.put("dispose", false);
                            cases.put("proceedings", null);
                            cases.put("single_date", odateExtra);
                            cases.put("single_month", intMonthExtra);
                            cases.put("single_year", yearExtra);
                            /*cases.put("prev_date", checkExtra);*/

                            LayoutInflater inflater= ViewAddNew.this.getLayoutInflater();
                            View layout=inflater.inflate(R.layout.case_reminder,null);

                            Button reminderBTN = layout.findViewById(R.id.reminderBTN);
                            Button reminderCancel = layout.findViewById(R.id.reminderCancel);

                            final AlertDialog.Builder alert = new AlertDialog.Builder(ViewAddNew.this);
                            alert.setView(layout);

                            final AlertDialog alertDialog = alert.create();
                            eventsDocuments(dateExtra, monthExtra, yearExtra);

                            reminderBTN.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cases.put("reminder", true);
                                    collectionReference.document().set(cases).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: " + e.toString());
                                        }
                                    });

                                    viewDashboard.edited = true;
                                    eventCalendar.SaveEvent(caseTitle, partyName, dateExtra, monthExtra, yearExtra);

                                    Calendar cal = Calendar.getInstance();
                                    cal.set(Integer.parseInt(yearExtra), Integer.parseInt(intMonthExtra)-1, Integer.parseInt(odateExtra), 8, 00);


                                    addCaseReminder(caseTitle, caseType, courtName, cal);

                                    finish();
                                }
                            });

                            reminderCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cases.put("reminder", false);
                                    collectionReference.document().set(cases).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: " + e.toString());
                                        }
                                    });

                                    eventCalendar.SaveEvent(caseTitle, partyName, dateExtra, monthExtra, yearExtra);
                                    startActivity(new Intent(ViewAddNew.this, ViewDashboard.class));

                                    alertDialog.dismiss();
                                    viewDashboard.edited = true;
                                    finish();
                                }
                            });

                            alert.setCancelable(false);
                            alertDialog.show();

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Memory exceptions", "exceptions" + e);
                        }
                    }
                });

        }
        else {
            Toast.makeText(ViewAddNew.this, "Please Log in First", Toast.LENGTH_SHORT).show();
        }
    }

    // PICK CONTACTS FROM CONTACTS LIST

    @SuppressLint({"LongLogTag", "WrongViewCast"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    Cursor cursor = null;
                    try {
                        String phoneNo = null;
                        String name = null;

                        Uri uri = data.getData();
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        phoneNo = cursor.getString(phoneIndex);
                        name = cursor.getString(nameIndex);

                        cParty.setText(name);
                        cContact.setText(phoneNo);

                        Log.e("Name and Contact number is",name+","+phoneNo);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case RESULT_PICK_CONTACT_2:
                    try {
                        String phoneNo = null;
                        String name = null;

                        Uri uri = data.getData();
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        phoneNo = cursor.getString(phoneIndex);
                        name = cursor.getString(nameIndex);

                        aAdvocate.setText(name);
                        aContact.setText(phoneNo);

                        Log.e("Name and Contact number is",name+","+phoneNo);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } else {
            Log.e("Failed", "Not able to pick contact");
        }
    }

    static void eventsDocuments(final String dateExtra, final String monthExtra, final String yearExtra) {

        final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final CollectionReference eventReference = FirebaseFirestore.getInstance().collection("users")
                .document(userID).collection("eventDates");

        Query totalEvent = eventReference;

        totalEvent.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    final int eventCount = task.getResult().size();
                    Query eventQuery = eventReference.whereEqualTo("eventDate", dateExtra);
                    eventQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().size() == 0) {

                                    Map<String, Object> events = new HashMap<>();
                                    events.put("eventDate", dateExtra);
                                    events.put("eventMonth", monthExtra);
                                    events.put("eventYear", yearExtra);

                                    eventReference.document(String.valueOf(eventCount))
                                            .set(events).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: Event Saved");
                                        }
                                    });
                                } else {
                                    Log.d(TAG, "onComplete: Date Already Exist");
                                }
                            }
                        }
                    });
                }
            }
        });
    }


////  UPDATE CASE

    private void updateCase(final String caseID) {

        Log.d(TAG, "updateCase: "+ caseID);
        String userID = currentUser.getUid();
        updateCase = fStore.collection("users").document(userID)
                .collection("casesID").document(caseID);

        updateCase.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    final String titlePH = documentSnapshot.getString("case_title");
                    String courtPH = documentSnapshot.getString("court_name");
                    final String typePH = documentSnapshot.getString("case_type");
                    String casenoPH = documentSnapshot.getString("case_no");
                    String behalf = documentSnapshot.getString("on_behalf");
                    String partyPH = documentSnapshot.getString("party_name");
                    String contactPH = documentSnapshot.getString("client_contact");
                    String filedPH = documentSnapshot.getString("filed_U_Sec");
                    String startDate = documentSnapshot.getString("start_date");

                    uTitle.setText(titlePH);
                    uCourt.setText(courtPH);
                    uType.setText(typePH);
                    uNo.setText(casenoPH);
                    uBehalf.setText(behalf);
                    uParty.setText(partyPH);
                    uContact.setText(contactPH);
                    ufiled.setText(filedPH);
                    cDate.setText(startDate);

                    saveCase.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        final String caseTitle = uTitle.getText().toString();
                        final String courtName = uCourt.getText().toString();
                        final String caseType = uType.getText().toString();
                        final String caseNo = uNo.getText().toString();
                        final String onBehalf = uBehalf.getText().toString();
                        final String partyName = uParty.getText().toString();
                        final String clientContact = uContact.getText().toString();
                        final String filedSection = ufiled.getText().toString();

                        updateCase.update("case_title", caseTitle);
                        updateCase.update("court_name", courtName);
                        updateCase.update("case_type", caseType);
                        updateCase.update("case_no", caseNo);
                        updateCase.update("on_behalf", onBehalf);
                        updateCase.update("party_name", partyName);
                        updateCase.update("client_contact", clientContact);
                        updateCase.update("filed_U_Sec", filedSection);

                        LayoutInflater inflater= ViewAddNew.this.getLayoutInflater();
                        View layout=inflater.inflate(R.layout.case_reminder,null);

                        Button reminderBTN = layout.findViewById(R.id.reminderBTN);
                        Button reminderCancel = layout.findViewById(R.id.reminderCancel);

                        final AlertDialog.Builder alert = new AlertDialog.Builder(ViewAddNew.this);
                        alert.setView(layout);

                        final AlertDialog alertDialog = alert.create();

                        reminderBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String newName = caseTitle;
                                String newType = caseType;
                                String newCourt = courtName;

                                updateCase.update("reminder", true);
                                updateCalendarEvent(titlePH, typePH, newName, newType, newCourt);

                                finish();
                            }
                        });

                        reminderCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(ViewAddNew.this, ViewDashboard.class));


                                alertDialog.dismiss();
                                finish();
                            }
                        });

                        alert.setCancelable(false);
                        alertDialog.show();
                        }
                    });
                }
            }
        });
    }


////  UPDATE CALENDAR EVENT

    private void updateCalendarEvent(final String caseName, final String caseType, final String newName, final String newType, final String newCourt) {

        updateCase.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                boolean checkReminder = documentSnapshot.getBoolean("reminder");
                if (checkReminder) {
                    ContentValues event = new ContentValues();
                    event.put(CalendarContract.Events.TITLE, newName);
                    event.put(CalendarContract.Events.DESCRIPTION, newType);

                    getContentResolver().update(Uri.parse("content://com.android.calendar/events"), event,
                            "title=? and description=?", new String[]{caseName, caseType});

                    Toast.makeText(getApplicationContext(), "Previous Event Updated", Toast.LENGTH_SHORT).show();
                } else {
                    updateCase.update("reminder", true);
                    updateCase.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            String calDate = value.getString("single_date");
                            String calMonth = value.getString("single_month");
                            String calYear = value.getString("single_year");


                            Calendar updateCal = Calendar.getInstance();
                            updateCal.set(Integer.parseInt(calYear), Integer.parseInt(calMonth)-1, Integer.parseInt(calDate), 8, 00);

                            addCaseReminder(newName, newType, newCourt, updateCal);
                        }
                    });
                }
            }
        });
    }


////  ADD CALENDAR EVENT

    private void addCaseReminder(String caseTitle, String caseType, String courtName, Calendar cal) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis()+60*60*1000*5);
        intent.putExtra(CalendarContract.Events.TITLE, caseTitle);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, caseType);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, courtName);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}