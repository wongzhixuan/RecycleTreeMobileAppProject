package com.example.recycletree;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class DashboardActivity extends AppCompatActivity {

    private Button btnlogout, btnHomepage;
    private TextView tvWelcome;
    private FirebaseAuth mAuth;

    String userId, username, email;

    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        // link widgets
        btnlogout = findViewById(R.id.btnLogout);
        btnHomepage = findViewById(R.id.btnHome);
        tvWelcome = findViewById(R.id.welcomeText);

        // Check if the user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(DashboardActivity.this, SignUpActivity.class));
            finish();
        }

        userId = currentUser.getUid();
        //Retrieve user attributes from firestore
        DocumentReference documentReference = firestore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()){
                    username = value.getString("Username");
                    email = value.getString("Email");
                    // Print username and email
                    tvWelcome.setText("Welcome " + username + " (" + email + ") ");
                }
                else{
                    Log.d("tag", "Document not exist!");
                }
            }
        });

        //Log out
        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(DashboardActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Home page
        btnHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, Navigation.class);
                startActivity(intent);
            }
        });
    }
}