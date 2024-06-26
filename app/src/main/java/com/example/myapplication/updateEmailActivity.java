package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

class UpdateEmailActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuthenticated;
    private String userOldEmail, userNewEmail, userPwd;
    private Button buttonUpdateEmail;
    private EditText editTextNewEmail, editTextPwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        progressBar = findViewById(R.id.progressBar);
        editTextPwd = findViewById(R.id.editText_verify_password);
        textViewAuthenticated = findViewById(R.id.textView_update_email);
        editTextNewEmail= findViewById(R.id.editText_new_email);
        buttonUpdateEmail = findViewById(R.id.update_email);

        buttonUpdateEmail.setEnabled(false);
        editTextNewEmail.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        //Set old email ID on TextView
        userOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmail = findViewById(R.id.showCurrentEmail);
        textViewOldEmail.setText(userOldEmail);

        if (firebaseUser.equals(""))
        {
            Toast.makeText(UpdateEmailActivity.this, "Something went wrong! User's details not available.", Toast.LENGTH_LONG).show();
        } else {
            reAuthenticate(firebaseUser);
        }

    }

    //ReAuthenticate/Verify User before updating email
    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button buttonVerifyUser = findViewById(R.id.authenticate);
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Obtain password for authentication
                userPwd = editTextPwd.getText().toString();

                if(TextUtils.isEmpty(userPwd))
                {
                    Toast.makeText(UpdateEmailActivity.this, "Password is needed to continue.", Toast.LENGTH_LONG).show();
                    editTextPwd.setError("Please enter your password for authentication.");
                    editTextPwd.requestFocus();
                }else {
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPwd);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                Toast.makeText(UpdateEmailActivity.this, "Password has been verified." + "You can update your email.", Toast.LENGTH_LONG).show();
                                textViewAuthenticated.setText("You have been verified. You can update your email now.");
                                textViewAuthenticated.setGravity(Gravity.CENTER);

                                editTextNewEmail.setEnabled(true);
                                editTextPwd.setEnabled(false);
                                buttonVerifyUser.setEnabled(false);
                                buttonUpdateEmail.setEnabled(true);

                                buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this,
                                        R.color.black_shade_3));

                                buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        userNewEmail = editTextNewEmail.getText().toString();

                                        if(TextUtils.isEmpty(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "New email is required.",Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please enter your new email.");
                                            editTextNewEmail.requestFocus();
                                        }else if(!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches())
                                        {
                                            Toast.makeText(UpdateEmailActivity.this, "Please enter a valid email.",Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please provide valid email.");
                                            editTextNewEmail.requestFocus();
                                        }else if (userOldEmail.matches(userNewEmail))
                                        {
                                            Toast.makeText(UpdateEmailActivity.this, "New email cannot be the same as old email.",Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please enter a new email.");
                                            editTextNewEmail.requestFocus();
                                        }else {
                                            progressBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);

                                        }

                                    }
                                });
                            }else {
                                try {
                                    throw task.getException();
                                }catch (Exception e)
                                {
                                    Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.verifyBeforeUpdateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete()){
                    firebaseUser.sendEmailVerification();
                    Toast.makeText(UpdateEmailActivity.this, "Email has been updated. Please verify your email.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateEmailActivity. this, welcomeActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    try {
                        throw task.getException();
                    }catch (Exception e ){
                        Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}
