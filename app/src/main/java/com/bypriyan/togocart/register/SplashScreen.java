package com.bypriyan.togocart.register;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.bypriyan.togocart.activity.MainActivity;
import com.google.firebase.messaging.FirebaseMessaging;

public class SplashScreen extends AppCompatActivity {
    private static int splashScreen = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        new Handler( ).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        },splashScreen);
    }

}
