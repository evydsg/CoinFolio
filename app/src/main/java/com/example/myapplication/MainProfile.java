package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class MainProfile extends AppCompatActivity {

    TextView textViewWelcome, textViewName, textViewEmail, textViewGender, textViewDOB;
    ProgressBar progressBar;
    String fullName, email, DoB;
    FirebaseAuth authProfile;
    SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_profile);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeToRefresh();

        textViewWelcome = findViewById(R.id.welcome_text);
        textViewName = findViewById(R.id.full_name);
        textViewEmail = findViewById(R.id.email_address);
        textViewDOB = findViewById(R.id.date_of_birth);


        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        progressBar = findViewById(R.id.progressBar);

        //Set OnClickListener on ImageView to Open UploadProfilePicActivity

        if (firebaseUser == null)
        {
            Toast.makeText(MainProfile.this, "Something went wrong! User's details are not available at the moment.", Toast.LENGTH_LONG).show();

        }else {
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }

    }

    private void swipeToRefresh() {
        swipeContainer = findViewById(R.id.swipeContainer);

        //Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> {
            //Code to refresh goes here
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
            swipeContainer.setRefreshing(false);
        });

        //Configure refresh colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }


    private void showUserProfile(FirebaseUser firebaseUser)
    {
        String userID = firebaseUser.getUid();

        //Extracting User Reference from Database for Registered Users

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);

                if(readUserDetails != null)
                {
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    DoB = readUserDetails.doB; // Assigning DOB from database
                    textViewDOB.setText(DoB); // Setting DOB to textViewDOB


                    textViewWelcome.setText(getString(R.string.welcome_head_profile,fullName + "!"));
                    textViewName.setText(fullName);
                    textViewEmail.setText(email);

                    //Set User Profile Picture (After it has been uploaded)
                }else {
                    Toast.makeText(MainProfile.this,"Something went wrong!", Toast.LENGTH_LONG).show();
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(MainProfile.this,"Something went wrong!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });

    }
}