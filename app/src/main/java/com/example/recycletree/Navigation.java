package com.example.recycletree;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.ui.AppBarConfiguration;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class Navigation extends AppCompatActivity {

    NavController navController;
    DrawerLayout drawer;
    NavigationView navigationView;
    AppBarConfiguration appBarConfiguration;
    BottomNavigationView bottomNavigationView;
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference storageReference;
    private ShapeableImageView drawerImage;
    private TextView drawerUname, drawerEmail;
    private FirebaseAuth mAuth;
    private String userId;

    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        // Get current user instance
        FirebaseUser currentUser = mAuth.getCurrentUser();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Return to sign up page if user haven't login
        if (currentUser == null) {
            Toast.makeText(Navigation.this, "You haven't sign in!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Navigation.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        }
        userId = currentUser.getUid();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setItemIconTintList(null);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();

        drawer = findViewById(R.id.drawer);
        fab = findViewById(R.id.menu_button);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(drawerListener);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        //Link widgets
        View header = navigationView.getHeaderView(0);
        drawerImage = (ShapeableImageView) header.findViewById(R.id.drawer_ProfileImage);
        drawerUname = (TextView) header.findViewById(R.id.drawer_UserName);
        drawerEmail = (TextView) header.findViewById(R.id.drawer_UserEmail);

        // Retrieve the user profile image from storage
        StorageReference profileRef = storageReference.child("users/" + userId + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(drawerImage);
            }
        });
        // Retrieve user attributes from firestore
        DocumentReference documentReference = firestore.collection("users").document(userId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {
                    drawerUname.setText(value.getString("Username"));
                    drawerEmail.setText(value.getString("Email"));
                } else {
                    Log.d("tag", "Document not exist!");
                }
            }
        });

    }
    private NavigationView.OnNavigationItemSelectedListener drawerListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment selectedFragment = null;

            switch (item.getItemId()){
                case R.id.nav_profile:
                    selectedFragment = new Profile();
                    drawer.closeDrawer(GravityCompat.START);
                    break;
                case R.id.nav_acheievement:
                    selectedFragment = new AchievementFragment();
                    drawer.closeDrawer(GravityCompat.START);
                    break;
                case R.id.nav_logout:
                    drawer.closeDrawer(GravityCompat.START);
                    mAuth.signOut();
                    Intent intent = new Intent(Navigation.this, SignUpActivity.class);
                    startActivity(intent);
                     return false;

            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();

            return true;

        }
    };


    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment selectedFragment = null;

            switch (item.getItemId()){
                case R.id.home:
                    selectedFragment = new HomeFragment();

                    break;

                case R.id.location:
                    selectedFragment = new LocationFragment();

                    break;

                case R.id.recycle:
                    selectedFragment = new RecycleFragment();

                    break;

                case R.id.guide:
                    selectedFragment = new GuideFragment();

                    break;

                case R.id.statistic:
                    selectedFragment = new StatisticFragment();

                    break;

            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();

            return true;
        }
    };
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}