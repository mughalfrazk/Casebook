package com.android.casebook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = null;
    TextView fullName,email,phone;
    ImageView profilePic, editProfile;
    Button restoreData;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    StorageReference storageReference;
    CollectionReference eventReference;
    ProgressBar profileProgress, restoreProgress;
    File imgFile;

    @SuppressLint("SdCardPath")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phone = findViewById(R.id.profilePhone);
        fullName = findViewById(R.id.profileName);
        email = findViewById(R.id.profileEmail);
        profilePic = findViewById(R.id.profilePic);
        editProfile = findViewById(R.id.editDP);
        profileProgress = findViewById(R.id.profileProgress);
        restoreData = findViewById(R.id.restoreData);
        restoreProgress = findViewById(R.id.restoreProgress);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

////  PROFILE PICTURE CHECK AND UPLOAD   ////

        if (fAuth.getCurrentUser() != null) {
            userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
            imgFile = new File("/storage/self/primary/Android/data/com.android.casebook/files/data/user/0/com.android.casebook/cache/" + userID + "profileImg.jpg");

            eventReference = fStore.collection("users").document(userID).collection("eventDates");
        } else {
            startActivity(new Intent(getApplicationContext(), ViewLogin.class));
        }

        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            profilePic.setImageBitmap(myBitmap);
        }

////  ----------------------------------  ////


////  PERSONAL INFORMATION   ////

        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                phone.setText(documentSnapshot.getString("phone"));
                fullName.setText(documentSnapshot.getString("fName"));
                email.setText(documentSnapshot.getString("email"));
            }
        });

////  ----------------------------------  ////x


////  POPULATE SQLITE DB THROUGH FIREBASE "eventDates" COLLECTION  ////

        restoreData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (isNetworkAvailable()) {
                restoreProgress.setVisibility(View.VISIBLE);
                Query totalEvent = eventReference;
                totalEvent.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int eventCount = Objects.requireNonNull(task.getResult()).size();
                            if (eventCount > 0) {
                                LayoutInflater inflater= MainActivity.this.getLayoutInflater();
                                //this is what I did to added the layout to the alert dialog
                                View layout = inflater.inflate(R.layout.data_restore,null);

                                final LinearLayout dataRestored = layout.findViewById(R.id.dataRestored);

                                final AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
                                Objects.requireNonNull(alert.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                alert.setView(layout);
                                alert.show();

                                for (int i = 0; i < eventCount; i++) {
                                    eventReference.document(String.valueOf(i)).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                            assert documentSnapshot != null;
                                            String date = documentSnapshot.getString("eventDate");
                                            String month = documentSnapshot.getString("eventMonth");
                                            String year = documentSnapshot.getString("eventYear");

                                            EventCalendar.SaveEvent(null, null, date, month, year);
                                        }
                                    });
                                }

                                dataRestored.setVisibility(View.VISIBLE);
                                restoreProgress.setVisibility(View.GONE);

                            } else if (eventCount == 0) {
                                Toast.makeText(getApplicationContext(), "Nothing to Restore", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "No Network Connection", Toast.LENGTH_SHORT).show();
            }
            }
        });

////  ----------------------------------  ////

////  UPLOAD PROFILE PICTURE   ////

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(openGallery, 1000);
                } else {
                    Toast.makeText(getApplicationContext(), "No Network Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

////  ----------------------------------  ////

    }

////  UPLOAD PROFILE PICTURE  ////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                assert data != null;
                Uri imageUri;
                imageUri = data.getData();
                uploadProfile(imageUri);
                profileProgress.setVisibility(View.VISIBLE);
            }
        }
    }

    private void uploadProfile(final Uri imageUri) {

        final StorageReference fileRef = storageReference.child("users/"+ userID +"/"+userID+"profileImg.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        profileProgress.setVisibility(View.GONE);
                        String url = uri.toString();
                        Picasso.get().load(url).into(profilePic);

                        if (!imgFile.exists()) {
                            downloadFile(MainActivity.this,  userID + "profileImg", ".jpg", getCacheDir().getPath(), url);
                        } else {
                            imgFile.delete();
                            if (!imgFile.exists()) {
                                downloadFile(MainActivity.this, userID + "profileImg", ".jpg", getCacheDir().getPath(), url);
                            }
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                profileProgress.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

////  ----------------------------------  ////


////  DOWNLOAD PROFILE PICTURE TO PHONE'S CACHE  ////

    static void downloadFile(Context context, String profile_pic, String extension, String path, String url) {
        ViewDashboard.edited = true;

        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setDestinationInExternalFilesDir(context, path, profile_pic + extension);

        downloadmanager.enqueue(request);
    }

////  ----------------------------------  ////


////  CHECK NETWORK  ////

    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

////  ----------------------------------  ////


////  LOGOUT  ////

    public void logout (View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage("Are you sure you want to Logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        finish();
                        startActivity(new Intent(getApplicationContext(), ViewLogin.class));
                    }
                }).setNegativeButton("No", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

////  ----------------------------------  ////

}