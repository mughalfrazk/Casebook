package com.android.casebook;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class ViewInnerCase extends ViewCase {
    private static final String TAG = "TAG";
    private static final int REQUEST_PHONE_CALL = 1;
    private Context context;
    private TextView case_title, party_name, court_name, case_type,
            case_no, on_behalf, client_contact, filed_U_Sec, start_date;
    private Button adjournBTN, disposeOff, reOpen;
    ImageButton doc_delete, callClient, setInnerReminder, msgClient;
    LinearLayout adjournSection, reminderIndicator;
    ImageView disposeIndicator;
    Calendar myCalendar;
    Switch reminderSwitch;

    ViewDashboard viewDashboard;
    EditText updateDate;
    EditText updateReason;

    RecyclerView adjournRecycler;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter adapter;
    Query dateQuery;
    DocumentReference documentReference, SelectedDate;
    CollectionReference eventReference;
    ViewAddNew viewAddNew;

    String caseStartDate, caseCurrentDate, currentProceedings, caseID, currentDate,
            clientNumber, caseName, courtName, dateExtra, monthExtra, yearExtra,
            singleDate, singleMonth, singleYear, caseType;

    SimpleDateFormat checkFormat = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    SimpleDateFormat eventDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inner_case_view);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        final String userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        myCalendar = Calendar.getInstance();

        case_title = findViewById(R.id.case_title);
        party_name = findViewById(R.id.party_name);
        court_name = findViewById(R.id.court_name);
        case_type = findViewById(R.id.case_type);
        case_no = findViewById(R.id.case_no);
        on_behalf = findViewById(R.id.on_behalf);
        client_contact = findViewById(R.id.contact_no);
        filed_U_Sec = findViewById(R.id.filed_section);
        doc_delete = findViewById(R.id.docdelete);
        start_date = findViewById(R.id.txtdate);
        disposeOff = findViewById(R.id.disposeOff);
        reOpen = findViewById(R.id.reOpen);
        callClient = findViewById(R.id.callClient);
        msgClient = findViewById(R.id.msgClient);
/*        setInnerReminder = findViewById(R.id.setInnerReminder);*/

        reminderSwitch = findViewById(R.id.reminderSwitch);
/*        reminderIndicator = findViewById(R.id.reminderIndicator);*/
        adjournSection = findViewById(R.id.adjournSection);
        adjournBTN = findViewById(R.id.adjournBTN);
        adjournRecycler = findViewById(R.id.adjourn_recycler);
        disposeIndicator = findViewById(R.id.disposeIndicator);

        viewDashboard = new ViewDashboard();
        viewAddNew = new ViewAddNew();

        Intent incomingintent = getIntent();
        caseID = incomingintent.getStringExtra("case_ID");

        eventReference = fStore.collection("users").document(userID).collection("eventDates");

        documentReference = fStore.collection("users").document(userID)
                .collection("casesID").document(caseID);

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                assert snapshot != null;
                case_title.setText(snapshot.getString("case_title"));
                party_name.setText(snapshot.getString("party_name"));
                court_name.setText(snapshot.getString("court_name"));
                case_type.setText(snapshot.getString("case_type"));
                case_no.setText(snapshot.getString("case_no"));
                on_behalf.setText(snapshot.getString("on_behalf"));
                client_contact.setText(snapshot.getString("client_contact"));
                filed_U_Sec.setText(snapshot.getString("filed_U_Sec"));
                start_date.setText(snapshot.getString("start_date"));

                currentDate = snapshot.getString("case_date");
                singleDate = snapshot.getString("single_date");
                singleMonth = snapshot.getString("single_month");
                singleYear = snapshot.getString("single_year");

                caseName = case_title.getText().toString();
                courtName = court_name.getText().toString();
                clientNumber = client_contact.getText().toString();
                caseType = case_type.getText().toString();
                caseStartDate = start_date.getText().toString();
                caseCurrentDate = snapshot.getString("case_date");
                currentProceedings = snapshot.getString("proceedings");

                boolean checkDispose = snapshot.getBoolean("dispose");
                boolean checkReminder = snapshot.getBoolean("reminder");

                if (checkDispose) {
                    disposeOff.setVisibility(View.GONE);
                    reOpen.setVisibility(View.VISIBLE);
                    disposeIndicator.setVisibility(View.VISIBLE);
                    adjournBTN.setVisibility(View.GONE);
                    adjournSection.setVisibility(View.VISIBLE);
                }

                if (caseStartDate.equals(caseCurrentDate)) {
                    if (!checkReminder && !checkDispose) {
/*                        reminderIndicator.setVisibility(View.VISIBLE);*/
                        reminderSwitch.setChecked(false);
                    }
                }
            }
        });


//// SET CASE REMINDER

        reminderSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Switch btn = (Switch) v;
                final boolean switchChecked = btn.isChecked();

                if (btn.isChecked()) {
                    btn.setChecked(false);
                } else {
                    btn.setChecked(true);
                }

                String message = "Delete Case Reminder?";
                if (!btn.isChecked()) {
                    message = "Set Case Reminder?";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewInnerCase.this);
                builder.setMessage(message)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                // "Yes" button was clicked
                                if (switchChecked) {
                                    documentReference.update("reminder", true);

                                    int rDate = Integer.parseInt(singleDate);
                                    int rMonth = Integer.parseInt(singleMonth)-1;
                                    int rYear = Integer.parseInt(singleYear);
                                    String description = case_type.getText().toString();

                                    setCaseReminder(rDate, rMonth, rYear, description);
                                    btn.setChecked(true);
                                } else {
                                    deleteCalendarEvent();
                                    btn.setChecked(false);
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


//// CALL CLIENT PHONE NUMBER

        callClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ViewInnerCase.this);
                builder.setMessage("Call Client?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                        CallClient();

                            }
                        }).setNegativeButton("No", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });


//// MESSEGE CLIENT PHONE NUMBER

        msgClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewInnerCase.this);
                builder.setMessage("Send Case Details to Client?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                MsgClient();

                            }
                        }).setNegativeButton("No", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


//// CASE DELETE

        doc_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewInnerCase.this);

                builder.setMessage("Are you sure you want delete the Case?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ViewDashboard.edited = true;

                                Intent intent = new Intent(getApplicationContext(), ViewCase.class);

                                deleteCalendarEvent();
                                intent.putExtra("deleteID", caseID);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton("No", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


////  ADJOURN CASE

        adjournBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LayoutInflater inflater= ViewInnerCase.this.getLayoutInflater();
                //this is what I did to added the layout to the alert dialog
                View layout=inflater.inflate(R.layout.adjourn_date,null);

                final EditText adjournDate = layout.findViewById(R.id.adjournDate);
                final EditText adjournReason = layout.findViewById(R.id.adjournReason);
                final ImageButton adjournCal = layout.findViewById(R.id.adjournCalendar);
                final Button adjournCase = layout.findViewById(R.id.adjournBTN);
                final Button adjournReminder = layout.findViewById(R.id.adjournReminder);

                final AlertDialog alert = new AlertDialog.Builder(ViewInnerCase.this).create();
                alert.setView(layout);
                alert.setTitle("Adjourn Date");
                alert.setMessage("Write Date and Reason for Adjourn Case");

                adjournCal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                  int dayOfMonth) {
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, monthOfYear);
                                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                adjournDate.setText(checkFormat.format(myCalendar.getTime()));
                            }
                        };

                        new DatePickerDialog(ViewInnerCase.this, date, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });

                adjournCase.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveAdjournDate(adjournDate, adjournReason);
                        alert.dismiss();
                    }
                });

                adjournReminder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveAdjournDate(adjournDate, adjournReason);

                        documentReference.update("reminder", true);
                        reminderSwitch.setChecked(true);

                        int rDate = myCalendar.get(Calendar.DATE);
                        int rMonth = myCalendar.get(Calendar.MONTH);
                        int rYear = myCalendar.get(Calendar.YEAR);
                        String description = "Proceedings: "+ adjournReason.getText().toString();

                        setCaseReminder(rDate, rMonth, rYear, description);

                        alert.dismiss();
                    }
                });

                alert.show();
            }
        });


//// DISPOSE CASES

        disposeOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                LayoutInflater inflater= ViewInnerCase.this.getLayoutInflater();
                View layout=inflater.inflate(R.layout.dispose_case,null);

                final EditText disposeDate = (EditText)layout.findViewById(R.id.disposeDate);
                final EditText disposeReason = (EditText)layout.findViewById(R.id.disposeReason);
                final ImageButton disposeCal = layout.findViewById(R.id.disposeCalendar);
                final Button disposeBTN = layout.findViewById(R.id.disposeBTN);

                final AlertDialog alert = new AlertDialog.Builder(ViewInnerCase.this).create();
                alert.setView(layout);
                alert.setTitle("Dispose Case");
                alert.setMessage("Write Date and Reason of Case Disposal");

                disposeCal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                  int dayOfMonth) {
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, monthOfYear);
                                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                disposeDate.setText(checkFormat.format(myCalendar.getTime()));
                            }
                        };

                        new DatePickerDialog(ViewInnerCase.this, date, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

                    }
                });

                disposeBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String dDate = disposeDate.getText().toString();
                        final String dReason = disposeReason.getText().toString();

                        if (TextUtils.isEmpty(dDate)) {
                            disposeDate.setError("Date is Required.");
                            return;
                        }

                        ////// DISPOSED TRUE IN CASES

                        documentReference.update("dispose", true).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: " + e.toString());
                            }
                        });

                        ////// DISPOSED DATE ADDED

                        DocumentReference DisposedDate = documentReference.collection("caseDates").document();

                        Map<String, Object> dates = new HashMap<>();
                        dates.put("adjourn_date", dDate);
                        dates.put("adjourn_reason", dReason);
                        dates.put("timestamp", FieldValue.serverTimestamp());
                        dates.put("dispose_date", true);

                        DisposedDate.set(dates).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Case Disposed", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: " + e.toString());
                            }
                        });

                        adjournSection.setVisibility(View.VISIBLE);
                        adjournBTN.setVisibility(View.GONE);
                        disposeOff.setVisibility(View.GONE);
                        reOpen.setVisibility(View.VISIBLE);
                        disposeIndicator.setVisibility(View.VISIBLE);

                        ViewDashboard.edited = true;
                        alert.dismiss();
                    }
                });

                alert.show();
            }
        });


//// RE-OPEN CASE

        reOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewInnerCase.this);

                builder.setMessage("Are you sure you want to Re-Open tha Case?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        documentReference.update("dispose", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Case Re-Opened", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: " + e.toString());
                                            }
                                        });


                                        final CollectionReference disposeDate = documentReference.collection("caseDates");
                                        Query deleteDoc = disposeDate.whereEqualTo("dispose_date", true);
                                        deleteDoc.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                                    disposeDate.document(documentSnapshot.getId()).delete();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: " + e.toString());
                                            }
                                        });

                                        reOpen.setVisibility(View.GONE);
                                        disposeIndicator.setVisibility(View.GONE);
                                        disposeOff.setVisibility(View.VISIBLE);

                                        ViewDashboard.edited = true;
                                    }
                                }
                        ).setNegativeButton("No", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });


        //// ADJOURN DATE RECYCLER VIEW ////

        adjournRecycler.setLayoutManager(new LinearLayoutManager(this));

        dateQuery = fStore.collection("users").document(userID)
                .collection("casesID").document(caseID).collection("caseDates")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        dateQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (Objects.requireNonNull(task.getResult()).size() == 0) {
                    adjournSection.setVisibility(View.GONE);
                }
            }
        });

        FirestoreRecyclerOptions<ModelAdjournDate> options = new FirestoreRecyclerOptions.Builder<ModelAdjournDate>()
                .setLifecycleOwner(this)
                .setQuery(dateQuery, ModelAdjournDate.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ModelAdjournDate, DatesViewHolder>(options) {

            @NonNull
            @Override
            public DatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_adjourn_date, parent, false);
                return new DatesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final DatesViewHolder datesViewHolder, int i, @NonNull ModelAdjournDate adjournDateModel) {
                datesViewHolder.adjourn_date.setText(adjournDateModel.getAdjourn_date());
                datesViewHolder.adjourn_reason.setText(adjournDateModel.getAdjourn_reason());
                datesViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DocumentSnapshot snapshot = getSnapshots().getSnapshot(datesViewHolder.getLayoutPosition());
                        String dateID = snapshot.getId();
                        String firstIDafterDelete = null, prevID = null;

                        if (adapter.getItemCount() == 1) {
                            firstIDafterDelete = "no_date";
                            prevID = null;

                        } else {
                            DocumentSnapshot firstDate = getSnapshots().getSnapshot(1);
                            firstIDafterDelete = firstDate.getId();

                            if (adapter.getItemCount() == 2) {
                                prevID = null;
                            } else {
                                DocumentSnapshot prevDate = getSnapshots().getSnapshot(2);
                                prevID = prevDate.getId();
                            }
                        }

                        if (datesViewHolder.getLayoutPosition() == 0) {
                            int count = adapter.getItemCount();
                            dateClicked(dateID, firstIDafterDelete, prevID, count);
                        } else {
                            Toast.makeText(getApplicationContext(), "Only latest date can be edited", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
            }
        };

        adjournRecycler.setAdapter(adapter);
        adjournRecycler.setHasFixedSize(false);
        adjournRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    public static class DatesViewHolder extends RecyclerView.ViewHolder {

        private TextView adjourn_date;
        private TextView adjourn_reason;

        public DatesViewHolder(@NonNull View itemView) {
            super(itemView);

            adjourn_date = itemView.findViewById(R.id.txtAdate);
            adjourn_reason = itemView.findViewById(R.id.txtReason);

        }
    }

    ////   MESSAGE CLIENT BUTTON FUNCTION

    private void MsgClient() {
        Intent msgIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", clientNumber, null));
        msgIntent.putExtra("sms_body", "Title: "+ case_title.getText().toString() +
                "\nCourt: " + court_name.getText().toString() + "\nDate: " + caseCurrentDate + "\nProceeding: " + currentProceedings);
        startActivity(msgIntent);

    }

    ////   CALL CLIENT BUTTON FUNCTION

    private void CallClient() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+clientNumber));

        if (ContextCompat.checkSelfPermission(ViewInnerCase.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ViewInnerCase.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
            if (ContextCompat.checkSelfPermission(ViewInnerCase.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent);
            }
        }
        else
        {
            startActivity(callIntent);
        }
    }

    private void deleteCalendarEvent() {
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                boolean checkReminder = documentSnapshot.getBoolean("reminder");
                if (checkReminder) {
                    getContentResolver().delete(Uri.parse("content://com.android.calendar/events"),
                            "title=? and description=?", new String[]{caseName, caseType});

                    Toast.makeText(getApplicationContext(), "Calendar Event Deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    //// REMINDER FUNCTION

    private void setCaseReminder(int rDate, int rMonth, int rYear, String description) {
        Calendar cal = Calendar.getInstance();
        cal.set(rYear, rMonth, rDate, 15, 30);

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis()+60*60*1000*5);
        intent.putExtra(CalendarContract.Events.TITLE, caseName);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, courtName);
        startActivity(intent);
    }



    //// SAVE ADJOURN DATE FUNCTION

    private void saveAdjournDate(@NotNull EditText adjournDate, @NotNull EditText adjournReason) {
        final String aDate = adjournDate.getText().toString();
        final String aReason = adjournReason.getText().toString();

        dateExtra = eventDateFormat.format(myCalendar.getTime());
        monthExtra = monthFormat.format(myCalendar.getTime());
        yearExtra = yearFormat.format(myCalendar.getTime());

        ViewAddNew.eventsDocuments(dateExtra, monthExtra, yearExtra);

        dateQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                if (queryDocumentSnapshots.size() > 1) {
                    String predate = queryDocumentSnapshots.getDocuments().get(1).getString("adjourn_date");
                    documentReference.update("prev_date", predate);
                } else if (queryDocumentSnapshots.size() < 1) {
                    documentReference.update("prev_date", caseStartDate);
                }
            }
        });

        if (TextUtils.isEmpty(aDate)) {
            adjournDate.setError("Date is Required.");
            return;
        }

/*        if (TextUtils.isEmpty(aReason)) {
            adjournReason.setError("Reason is Required.");
            return;
        }*/

        documentReference.update("case_date", aDate);
        documentReference.update("proceedings", aReason);

        DocumentReference AdjournDate = documentReference.collection("caseDates").document();

        Map<String, Object> dates = new HashMap<>();
        dates.put("adjourn_date", aDate);
        dates.put("adjourn_reason", aReason);
        dates.put("timestamp", FieldValue.serverTimestamp());

        AdjournDate.set(dates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Adjourn Date Added", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.toString());
            }
        });

        adjournSection.setVisibility(View.VISIBLE);
        EventCalendar.SaveEvent(null, null, dateExtra, monthExtra, yearExtra);



        ViewDashboard.edited = true;
    }




    ////  ADJOURN DATE CLICKED

    private void dateClicked(String dateID, final String firstID, final String prevID, final int count) {
        LayoutInflater inflater= ViewInnerCase.this.getLayoutInflater();
        View layout=inflater.inflate(R.layout.date_edit,null);

        final CardView editAdjourn = layout.findViewById(R.id.editAdjourn);
        final CardView deleteAdjourn = layout.findViewById(R.id.deleteAdjourn);

        SelectedDate = documentReference.collection("caseDates").document(dateID);

        final AlertDialog adjournClick = new AlertDialog.Builder(ViewInnerCase.this).create();
        adjournClick.setView(layout);
        adjournClick.setTitle("Edit Adjourn Date");
        adjournClick.setMessage("Select an Option");

        ////  DELETE DATE

        deleteAdjourn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewInnerCase.this);

                builder.setMessage("Are you sure you want to Delete this Date?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SelectedDate.delete();

                                DocumentReference FirstDate = documentReference.collection("caseDates").document(firstID);
                                FirstDate.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            String LastDate;

                                            if (firstID.equals("no_date")) {
                                                LastDate = caseStartDate;
                                                documentReference.update("prev_date", null);

                                            } else if (count == 2) {
                                                assert documentSnapshot != null;
                                                LastDate = documentSnapshot.getString("adjourn_date");
                                                documentReference.update("prev_date", caseStartDate);

                                            } else {
                                                assert documentSnapshot != null;
                                                LastDate = documentSnapshot.getString("adjourn_date");

                                            }

                                            deleteCalendarEvent();

                                            ViewDashboard.edited = true;
                                            Toast.makeText(getApplicationContext(), "ADJOURN DATE DELETED", Toast.LENGTH_SHORT).show();
                                            documentReference.update("case_date", LastDate);
                                        }
                                    }
                                });
                                adjournClick.dismiss();
                            }
                        }).setNegativeButton("No", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });





        ////  UPDATE DATE

        editAdjourn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater= ViewInnerCase.this.getLayoutInflater();
                //this is what I did to added the layout to the alert dialog
                View layout=inflater.inflate(R.layout.adjourn_date,null);

                SelectedDate.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            assert documentSnapshot != null;
                            String DatePH = documentSnapshot.getString("adjourn_date");
                            String ReasonPH = documentSnapshot.getString("adjourn_reason");

                            updateDate.setText(DatePH);
                            updateReason.setText(ReasonPH);
                        }
                    }
                });

                updateDate = layout.findViewById(R.id.adjournDate);
                updateReason = layout.findViewById(R.id.adjournReason);
                final ImageButton adjournCal = layout.findViewById(R.id.adjournCalendar);
                final Button updateAdjourn = layout.findViewById(R.id.adjournBTN);
                final Button updateReminder = layout.findViewById(R.id.adjournReminder);


                final AlertDialog editAlert = new AlertDialog.Builder(ViewInnerCase.this).create();
                editAlert.setView(layout);
                editAlert.setTitle("Adjourn Date");
                editAlert.setMessage("Write Date and Reason for Adjourn Case");

                adjournCal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, monthOfYear);
                                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                updateDate.setText(checkFormat.format(myCalendar.getTime()));
                            }
                        };

                        new DatePickerDialog(ViewInnerCase.this, date, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });

                updateAdjourn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateAdjournDate(updateDate, updateReason, myCalendar);

                        ViewDashboard.edited = true;
                        adjournClick.dismiss();
                        editAlert.dismiss();
                    }
                });

                updateReminder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateAdjournDate(updateDate, updateReason, myCalendar);
                        deleteCalendarEvent();

                        documentReference.update("reminder", true);
/*                        reminderIndicator.setVisibility(View.GONE);*/
                        reminderSwitch.setChecked(true);

                        int rDate = myCalendar.get(Calendar.DATE);
                        int rMonth = myCalendar.get(Calendar.MONTH);
                        int rYear = myCalendar.get(Calendar.YEAR);
                        String description = "Proceedings: "+ updateReason.getText().toString();

                        setCaseReminder(rDate, rMonth, rYear, description);

                        {Toast.makeText(context, "Adjourn Date Updated", Toast.LENGTH_SHORT).show();}

                        ViewDashboard.edited = true;
                        adjournClick.dismiss();
                        editAlert.dismiss();
                    }
                });
                editAlert.show();
            }
        });
        adjournClick.show();
    }

    private void updateAdjournDate(EditText updateDate, EditText updateReason, Calendar newCalendar) {
        final String uDate = updateDate.getText().toString();
        final String uReason = updateReason.getText().toString();

        final String saveDate = eventDateFormat.format(newCalendar.getTime());
        final String saveMonth = monthFormat.format(newCalendar.getTime());
        final String saveYear = yearFormat.format(newCalendar.getTime());

        ViewAddNew.eventsDocuments(saveDate, saveMonth, saveYear);

        if (TextUtils.isEmpty(uDate)) {
            updateDate.setError("Date is Required.");
            return;
        }

        if (TextUtils.isEmpty(uReason)) {
            updateReason.setError("Reason is Required.");
            return;
        }
        documentReference.update("case_date", uDate);
        documentReference.update("proceedings", uReason);

        SelectedDate.update("adjourn_date", uDate);
        SelectedDate.update("adjourn_reason", uReason);
        SelectedDate.update("timestamp", FieldValue.serverTimestamp());

        EventCalendar.SaveEvent(null, null, saveDate, saveMonth, saveYear);
        {Toast.makeText(context, "Adjourn Date Updated", Toast.LENGTH_SHORT).show();}
    }


    ////  TOOLBAR MENU


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        final String Title, Court, Date, Reason;

        Title = case_title.getText().toString();
        Court = court_name.getText().toString();
        Date = caseCurrentDate;
        Reason = currentProceedings;

        switch (item.getItemId()) {

            case R.id.shareCase:

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody="Title: "+ Title + "\nCourt: " + Court + "\nDate: " + Date + "\nProceeding: " + Reason;
                String shareSubject="Case is Registered";

                sharingIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT,shareSubject);

                startActivity(Intent.createChooser(sharingIntent, "Share Using"));
                break;

            case R.id.editCase:
                Intent intent = new Intent(this, ViewAddNew.class);
                intent.putExtra("case_ID", caseID);
                startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
