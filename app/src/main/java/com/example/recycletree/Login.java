package com.example.recycletree;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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


public class Login extends AppCompatActivity {
    private static final String KEY_EMPTY = "";
    private EditText etEmail, etPwd;
    private String email, password;
    private ProgressDialog pDialog;

    private Button btnlogin;
    private Button btnFgtPwd;

    private FirebaseAuth mAuth;
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
        setContentView(R.layout.activity_login);
        // initialise firebase
        mAuth = FirebaseAuth.getInstance();

        //link widgets
        etEmail = findViewById(R.id.etEmailLogin);
        etPwd = findViewById(R.id.etPwdLogin);

        btnlogin = findViewById(R.id.btnSignIn);

        // Login
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = etEmail.getText().toString().trim();
                password = etPwd.getText().toString().trim();
                if(validateInputs()){
                    login();
                }

            }
        });

        // Reset password
        btnFgtPwd = findViewById(R.id.btnForgetPwd);
        btnFgtPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetMail = new EditText(view.getContext());
                AlertDialog.Builder pwdresetDialog = new AlertDialog.Builder(view.getContext());
                pwdresetDialog.setTitle("Reset Password ?");
                pwdresetDialog.setMessage("Enter Your Email to get reset link:");
                pwdresetDialog.setView(resetMail);
                pwdresetDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //sent link to email
                        String mail = resetMail.getText().toString().trim();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Login.this, "Reset Link Sent", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                pwdresetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //close dialog
                    }
                });
                pwdresetDialog.create().show();
            }
        });


    }

    // Display Progress bar while logging in
    private void displayLoader(){
        pDialog = new ProgressDialog(Login.this);
        pDialog.setMessage("Logging In... Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }
    private void login(){
        displayLoader();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pDialog.dismiss();

                // If registration is successful
                if(task.isSuccessful()){
                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    reload(user);
                }
                else{
                    //Print error
                    Toast.makeText(Login.this, "Login Failed!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    clearForm();
                }
            }
        });
    }
    /**
     * Validates inputs and shows error if any
     */
    private boolean validateInputs() {
        if(KEY_EMPTY.equals(email)){
            etEmail.setError("Email cannot be empty");
            etEmail.requestFocus();
            return false;
        }
        if(KEY_EMPTY.equals(password)){
            etPwd.setError("Password cannot be empty");
            etPwd.requestFocus();
            return false;
        }
        return true;
    }
    private void reload(FirebaseUser user)
    {
        if(user != null) {
            loadDashboard();
        }
    }
    //Launch Dashboard Activity on Successful Sign up
    private void loadDashboard(){
        Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
        startActivity(i);
        finish();
    }

    //Clear form
    private void clearForm(){
        etEmail.getText().clear();
        etPwd.getText().clear();
    }
}