package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteAccount extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private EditText editTextUserPwd;
    private TextView textViewAuthenticated;
    private ProgressBar progressBar;
    private String userPwd;
    private Button buttonReAuthenticate, buttonDeleteUser;
    private static final String TAG = "DeleteAccount";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        progressBar = findViewById(R.id.progressBar);
        editTextUserPwd = findViewById(R.id.editText_verify_password);
        buttonDeleteUser = findViewById(R.id.delete_account);
        buttonReAuthenticate = findViewById(R.id.authenticate);
        textViewAuthenticated = findViewById(R.id.textView_delete_account);

        //Disable Delete User button until user is authenticated
        buttonDeleteUser.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();

        firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser.equals(""))
        {
            Toast.makeText(DeleteAccount.this, "Something went wrong!" + "User detail's are not available at the moment.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DeleteAccount.this, MainProfile.class);
            startActivity(intent);
        }else {
            reAuthenticateUser(firebaseUser);
        }
    }
    //Reauthenticate user before allowing him to change password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwd = editTextUserPwd.getText().toString();

                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(DeleteAccount.this, "Password is needed to continue.", Toast.LENGTH_SHORT).show();
                    editTextUserPwd.setError("Please enter your current password to authenticate.");
                    editTextUserPwd.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);

                    //ReAuthenticate User Now
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                progressBar.setVisibility(View.GONE);

                                //Disable textView for Password
                                editTextUserPwd.setEnabled(false);
                                //Enable Delete Account
                                buttonDeleteUser.setEnabled(true);

                                //Disable Authenticate Button
                                buttonReAuthenticate.setEnabled(false);

                                //Set TextView to show user is verified
                                textViewAuthenticated.setText("You have been verified. You can delete your account now. Be careful, this action is irreversible.");
                                Toast.makeText(DeleteAccount.this, "Password has been verified. You can delete your account now.", Toast.LENGTH_SHORT).show();

                                //Update color of Change password button
                                buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(DeleteAccount.this, R.color.blood_red));

                                buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showAlertDialog();
                                    }
                                });
                            }else {
                                try{
                                    throw task.getException();
                                }catch (Exception e ){
                                    Toast.makeText(DeleteAccount.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }

            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteAccount.this);
        builder.setTitle("Delete user and related data?");
        builder.setMessage("Do you really want to delete your profile and related data? This action is irreversible.");

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserData(firebaseUser);
            }
        });
        //Return to User Profile Activity if user presses cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DeleteAccount.this, MainProfile.class);
                startActivity(intent);
                finish();
            }
        });

        AlertDialog alertDialog = builder.create();

        //Change the button color of Continue
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.blood_red));
            }
        });
        alertDialog.show();
    }

    private void deleteUser() {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    authProfile.signOut();
                    Toast.makeText(DeleteAccount.this, "User has been deleted!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DeleteAccount.this, welcomeActivity.class);
                    startActivity(intent);
                    finish();
                }else
                {
                    try{
                        throw task.getException();
                    }catch (Exception e)
                    {
                        Toast.makeText(DeleteAccount.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    //Delete all the data of User
    private void deleteUserData(FirebaseUser firebaseUser) {
        //Delete Display Pic. Also check if user has uploaded any pic before deleting
        if(firebaseUser.getPhotoUrl()!= null){
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());

            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "onSuccess: Photo Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(DeleteAccount.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Delete Data from Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: User data deleted");

                //Delete user from firebase
                deleteUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteAccount.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


}