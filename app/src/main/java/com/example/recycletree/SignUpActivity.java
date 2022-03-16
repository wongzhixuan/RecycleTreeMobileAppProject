package com.example.recycletree;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


public class SignUpActivity extends AppCompatActivity {
    private static final String KEY_EMPTY = "";

    // Firebase
    private FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    // Initialize widgets
    private EditText etUname, etEmail, etPwd, etRepwd;
    private String email, username, password, repeatPwd, userID;
    private Button signup, login;
    private ProgressDialog pDialog;

    private static final String TAG = "SignUp";

    @Override
    public void onStart() {
        //Check if the user is logged in
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload(currentUser);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Link widgets
        etUname = findViewById(R.id.etUname);
        etEmail = findViewById(R.id.etEmail);
        etPwd = findViewById(R.id.etPwd);
        etRepwd = findViewById(R.id.etRptPwd);
        signup = findViewById(R.id.btnSignUp);
        login = findViewById(R.id.btnLogIn);

        //Sign Up
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get text
                username = etUname.getText().toString().trim();
                email = etEmail.getText().toString().trim();
                password = etPwd.getText().toString().trim();
                repeatPwd = etRepwd.getText().toString().trim();

                if (validateInputs()) {
                    registerUser();
                }
            }
        });

        //redirect to login page when login button is pressed
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // If user already login before, Go to Home
    private void reload(FirebaseUser user)
    {
        if(user != null) {
            loadDashboard();
        }
    }

    /**
     * Validates inputs and shows error if any
     */
    private boolean validateInputs(){

        // If email is empty
        if(KEY_EMPTY.equals(email)){
            etEmail.setError("Email cannot be empty");
            etEmail.requestFocus();
            return false;
        }

        // If username is empty
        if(KEY_EMPTY.equals(username)){
            etUname.setError("Username cannot be empty");
            etUname.requestFocus();
            return false;
        }

        // If password is empty
        if(KEY_EMPTY.equals(password)){
            etPwd.setError("Password cannot be empty");
            etPwd.requestFocus();
            return false;
        }

        //If password is too short
        if(password.length() < 6){
            etPwd.setError("Password must be >= 6 characters");
            etPwd.requestFocus();
            return false;
        }

        // If repeat password is empty
        if(KEY_EMPTY.equals(repeatPwd)){
            etRepwd.setError("Confirm password cannot be empty");
            etPwd.requestFocus();
            return false;
        }

        // If password and repeat password is not match
        if(!password.equals(repeatPwd)){
            etRepwd.setError("Password and Confirm password does not match");
            etRepwd.requestFocus();
            return false;
        }
        return true;
    }

    //Register New user
    private void registerUser(){
        displayLoader();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pDialog.dismiss();

                // If registration is successful
                if(task.isSuccessful()){
                    /**
                    //sent email verification
                    FirebaseUser myuser = mAuth.getCurrentUser();
                    myuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(SignUpActivity.this, "Verification Email sent!", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Email not sent" + e.getMessage());
                        }
                    });
                    **/
                    Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    userID = user.getUid();
                    // Create new document for that user
                    DocumentReference documentReference = firestore.collection("users").document(userID);
                    Map<String, Object> Thisuser = new HashMap<>();
                    Thisuser.put("Username", username);
                    Thisuser.put("Email", email);
                    Thisuser.put("Description", "");
                    Thisuser.put("Points", 0);
                    Thisuser.put("Lv", 0);
                    Thisuser.put("CurrentXP", 0);
                    documentReference.set(Thisuser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "on Success: user Profile is created for" + userID );
                        }
                    });

                    DocumentReference documentReference2 = firestore.collection("users").document(userID).collection("Records").document(userID);
                    Map<String, Object> thisuser = new HashMap<>();
                    thisuser.put("Blue", 0);
                    thisuser.put("Orange", 0);
                    thisuser.put("Brown",0);
                    documentReference2.set(thisuser).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error! "+e.getMessage());
                        }
                    });
                    // redirect to dashboard
                    reload(user);
                }
                else{
                    //Print error
                    Toast.makeText(SignUpActivity.this, "Registeration Failed!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    clearForm();
                }
            }
        });
    }

    //Display Progress bar while registering
    private void displayLoader(){
        pDialog = new ProgressDialog(SignUpActivity.this);
        pDialog.setMessage("Signing up... Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    //Launch Dashboard Activity on Successful Sign up
    private void loadDashboard(){
        Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
        startActivity(i);
    }

    //Clear form
    private void clearForm(){
        etEmail.getText().clear();
        etUname.getText().clear();
        etRepwd.getText().clear();
        etPwd.getText().clear();
    }
}