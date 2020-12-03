package com.android.casebook;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.android.casebook.R.id.myCases;
import static com.android.casebook.R.id.profileIcon;
import static com.android.casebook.R.id.todayCases;
import static com.android.casebook.R.id.tomorrowCases;

public class ViewDashboard extends AppCompatActivity {
    private static final String TAG = null;
    private static final int STORAGE_PERMISSION_CODE = 1;

    CardView caseView;
    CardView clientList;
    CardView courtList;
    CardView courtLinks;
    LinearLayout todayView, tomorrowView, disposeView, totalCases;
    TextView countCases, countToday, countTomorrow, countDispose;
    ImageView dashboardprofile;
    int totalcount, tomorrowcount, todaycount, disposecount;
    View updateView;
    LinearLayout updateProgress, updateOffline;
    File imgFile;

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    FirebaseUser currentUser;
    StorageReference storageReference;
    String tomorrowDate, userID;
    public static boolean edited;

    Calendar calendar = Calendar.getInstance();
    Date currentDate = calendar.getTime();
    SimpleDateFormat checkFormat = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
    String todayformattedDate = checkFormat.format(currentDate);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        requestStoragePermission();

        calendar.add(Calendar.DATE, 1);
        tomorrowDate = checkFormat.format(calendar.getTime());

        countCases = findViewById(R.id.count_cases);
        countToday = findViewById(R.id.count_today);
        countTomorrow = findViewById(R.id.count_tomorrow);
        countDispose = findViewById(R.id.count_dispose);

        dashboardprofile = findViewById(profileIcon);
        caseView = findViewById(myCases);
        clientList = findViewById(R.id.clientList);
        courtLinks = findViewById(R.id.courtLinks);
        courtList = findViewById(R.id.courtList);
        todayView = findViewById(todayCases);
        tomorrowView = findViewById(tomorrowCases);
        disposeView = findViewById(R.id.disposeView);
        totalCases = findViewById(R.id.totalCases);
        updateView = findViewById(R.id.updateView);
        updateProgress = findViewById(R.id.updateProgress);
        updateOffline = findViewById(R.id.updateOffline);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        if (isNetworkAvailable()) {
            updateView.setVisibility(View.VISIBLE);
            updateProgress.setVisibility(View.VISIBLE);
        } else {
            updateView.setVisibility(View.VISIBLE);
            updateOffline.setVisibility(View.VISIBLE);
        }

//// SETTING PROFILE PICTURE

        if (fAuth.getCurrentUser() != null) {

            userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
            imgFile = new File("/storage/self/primary/Android/data/com.android.casebook/files/data/user/0/com.android.casebook/cache/" + userID + "profileImg.jpg");
        } else {
            startActivity(new Intent(getApplicationContext(), ViewLogin.class));
        }


        if(imgFile.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            dashboardprofile.setImageBitmap(myBitmap);


        } else if (!imgFile.exists() && isNetworkAvailable()) {

            final StorageReference fileRef = storageReference.child("users/"+ userID +"/"+userID+"profileImg.jpg");
            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String url = uri.toString();
                    Picasso.get().load(url).into(dashboardprofile);
                    downloadFile(ViewDashboard.this,  userID + "profileImg", ".jpg", getCacheDir().getPath(), url);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                }
            });

        } else {
            Log.d(TAG, "onCreate: No Picture" );
        }

//// SETTING PROFILE PICTURE

        todayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewSelectedDate.class);
                intent.putExtra("SelectedDate", todayformattedDate);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fadein, R.anim.fadeout);
                startActivity(intent, options.toBundle());
            }
        });

        tomorrowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewSelectedDate.class);
                intent.putExtra("SelectedDate", tomorrowDate);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fadein, R.anim.fadeout);
                startActivity(intent, options.toBundle());
            }
        });

        disposeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(getApplicationContext(), ViewDisposed.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fadein, R.anim.fadeout);
                startActivity(backIntent, options.toBundle());
            }
        });

        dashboardprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(getApplicationContext(), MainActivity.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fadein, R.anim.fadeout);
                startActivity(backIntent, options.toBundle());
            }
        });

        caseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(getApplicationContext(), ViewCase.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fadein, R.anim.fadeout);
                startActivity(backIntent, options.toBundle());
            }
        });

        clientList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(getApplicationContext(), ViewClients.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fadein, R.anim.fadeout);
                startActivity(backIntent, options.toBundle());
            }
        });

        courtList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(getApplicationContext(), ViewCourts.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fadein, R.anim.fadeout);
                startActivity(backIntent, options.toBundle());
            }
        });


        courtLinks.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            @Override
            public void onClick(View v) {
            LinksDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (edited) {
            finish();
            startActivity(getIntent());
            edited = false;
        }
    }


////  COUNT CASES QUERIES

    @Override
    protected void onStart() {
        super.onStart();
        final CollectionReference collectionReference = fStore.collection("users")
                .document(userID).collection("casesID");

        Query currentCases = collectionReference.whereEqualTo("dispose", false);
        currentCases.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    updateView.setVisibility(View.GONE);
                    updateProgress.setVisibility(View.GONE);
                    updateOffline.setVisibility(View.GONE);

                    totalcount = Objects.requireNonNull(task.getResult()).size();
                    countCases.setText(String.valueOf(totalcount));
                }
                else {
                    Log.d(TAG, "Error getting documents", task.getException());
                }
            }
        });

        //// TODAY CASES

        Query todayQuery = collectionReference.whereEqualTo("case_date", todayformattedDate)
                .whereEqualTo("dispose", false);
        todayQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    todaycount = Objects.requireNonNull(task.getResult()).size();
                    collectionReference.whereEqualTo("prev_date", todayformattedDate)
                            .whereEqualTo("dispose", false).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                                    if (Objects.requireNonNull(task2.getResult()).size() != 0) {
                                        int preDate = task2.getResult().size() + todaycount;
                                        countToday.setText(String.valueOf(preDate));
                                    } else {
                                        countToday.setText(String.valueOf(todaycount));
                                    }
                                }
                            });
                }
                else {
                    Log.d(TAG, "Error getting documents", task.getException());
                }
            }
        });

        //// TOMORROW CASES

        Query tomorrowQuery = collectionReference.whereEqualTo("case_date", tomorrowDate)
                .whereEqualTo("dispose", false);
        tomorrowQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    tomorrowcount = Objects.requireNonNull(task.getResult()).size();
                    countTomorrow.setText(String.valueOf(tomorrowcount));
                }
                else {
                    Log.d(TAG, "Error getting documents", task.getException());
                }
            }
        });

        //// DISPOSED CASES

        Query disposedQuery = collectionReference.whereEqualTo("dispose", true);
        disposedQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    disposecount = Objects.requireNonNull(task.getResult()).size();
                    countDispose.setText(String.valueOf(disposecount));
                }
                else {
                    Log.d(TAG, "Error getting documents", task.getException());
                }
            }
        });
    }


////  CHECK NETWORK

    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


////  REQUEST PERMISSIONS

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
            return;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
            return;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            return;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            return;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
            return;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;


        ActivityCompat.requestPermissions(this, new String[]
                {
                        android.Manifest.permission.READ_CALENDAR,
                        android.Manifest.permission.WRITE_CALENDAR,
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.WRITE_CONTACTS,
                        android.Manifest.permission.CALL_PHONE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Permission is needed", Toast.LENGTH_LONG).show();
            }
        }
    }


////  COURT LINKS

    private void LinksDialog() {
        LayoutInflater inflater= ViewDashboard.this.getLayoutInflater();
        View layout=inflater.inflate(R.layout.court_links,null);

        ImageButton supCourt, highCourt, disCourt, nAssembly, senate, punjabLaws;
        final ProgressBar progressBar3;

        progressBar3 = layout.findViewById(R.id.progressBar3);
        supCourt = layout.findViewById(R.id.supCourt);
        highCourt = layout.findViewById(R.id.highCourt);
        disCourt = layout.findViewById(R.id.disCourt);
        nAssembly = layout.findViewById(R.id.nAssembly);
        senate = layout.findViewById(R.id.senate);
        punjabLaws = layout.findViewById(R.id.punjabLaws);

        final AlertDialog alert = new AlertDialog.Builder(ViewDashboard.this).create();
        Objects.requireNonNull(alert.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alert.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;
        alert.setView(layout);

        supCourt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar3.setVisibility(View.VISIBLE);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.supremecourt.gov.pk")));
                alert.dismiss();
            }
        });

        highCourt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar3.setVisibility(View.VISIBLE);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://lhc.gov.pk")));
                alert.dismiss();
            }
        });

        disCourt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar3.setVisibility(View.VISIBLE);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://lahore.dc.lhc.gov.pk")));
                alert.dismiss();
            }
        });

        nAssembly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar3.setVisibility(View.VISIBLE);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://na.gov.pk/en/index.php")));
                alert.dismiss();
            }
        });

        senate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar3.setVisibility(View.VISIBLE);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://senate.gov.pk/en/index.php")));
                alert.dismiss();
            }
        });

        punjabLaws.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar3.setVisibility(View.VISIBLE);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.punjablaws.gov.pk")));
                alert.dismiss();
            }
        });

        alert.show();
    }


////  DOWNLOAD IMAGE FILES

    void downloadFile(Context mainActivity, String profile_pic, String extension, String path, String url) {
        DownloadManager downloadmanager = (DownloadManager) mainActivity.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setDestinationInExternalFilesDir(mainActivity, path, profile_pic + extension);

        downloadmanager.enqueue(request);
    }


////  BACK BUTTON PRESSED

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewDashboard.this);

        builder.setMessage("Are you sure you want to Exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                        System.exit(0);
                    }
                }).setNegativeButton("No", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}