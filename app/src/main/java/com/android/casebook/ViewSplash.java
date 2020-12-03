package com.android.casebook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ViewSplash extends AppCompatActivity {
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        int secondsDelayed = 1;
        if (currentUser == null) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    startActivity(new Intent(ViewSplash.this, ViewRegister.class));
                    finish();
                }
            }, secondsDelayed * 3000);
        }
        else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    startActivity(new Intent(getApplicationContext(), ViewDashboard.class));
                    finish();
                }
            }, secondsDelayed * 3000);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}