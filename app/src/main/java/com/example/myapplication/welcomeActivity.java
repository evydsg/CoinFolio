package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class welcomeActivity extends AppCompatActivity {

    Button registerButton, loginButton, guestButton;
    private FirebaseAuth authProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(welcomeActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click here
                Intent intent = new Intent(welcomeActivity.this, LoginActivity.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        guestButton = findViewById(R.id.guestButton);
        guestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click here
                Intent intent = new Intent(welcomeActivity.this, MainActivity.class);

                // Start the new activity
                startActivity(intent);
            }
        });

    }
}