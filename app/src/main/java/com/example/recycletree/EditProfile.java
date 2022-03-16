package com.example.recycletree;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference storageReference;

    private Button btnSave;
    private com.google.android.material.imageview.ShapeableImageView userImage;
    private EditText etUname, etDescrip, etEmail;
    private FirebaseAuth mAuth;
    private String userId;
    private TextView tv_point;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent data = getIntent();
        String Username = data.getStringExtra("Username");
        String Email = data.getStringExtra("Email");
        String Description = data.getStringExtra("Description");
        String Points = data.getStringExtra("Points");

        // Link widgets
        etUname = findViewById(R.id.etEditUname);
        etEmail = findViewById(R.id.etEditEmail);
        etDescrip = findViewById(R.id.etEditDescription);
        userImage = findViewById(R.id.userEditImage);
        btnSave = findViewById(R.id.btnSaveProfile);
        tv_point = findViewById(R.id.tv_points);

        // Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        // Get current user instance
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Retrieve the user profile image from storage
        StorageReference profileRef = storageReference.child("users/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(userImage);
            }
        });

        // show user data
        etUname.setText(Username);
        etEmail.setText(Email);
        etDescrip.setText(Description);
        tv_point.setText(Points);

        // update database
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etUname.getText().toString().isEmpty() || etEmail.getText().toString().isEmpty()){
                    Toast.makeText(EditProfile.this, "Username and Email cannot be empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                String email = etEmail.getText().toString().trim();
                currentUser.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DocumentReference documentReference = firestore.collection("users").document(userId);
                        Map<String, Object> edited = new HashMap<>();
                        edited.put("Email", email);
                        edited.put("Username", etUname.getText().toString());
                        edited.put("Description", etDescrip.getText().toString());
                        documentReference.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(EditProfile.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProfile.this, "Error! "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        getSupportFragmentManager().popBackStackImmediate();
                        Intent intent = new Intent(EditProfile.this, Navigation.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, "Error! "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Change profile image if clicked on the image
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateImage();
            }
        });


    }

    // Change profile Image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();
                userImage.setImageURI(imageUri);
                uploadToFirebase(imageUri);
            }
        }
    }

    private void uploadToFirebase(Uri imageUri) {
        // upload image to Firebase storage
        StorageReference fileRef = storageReference.child("users/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(userImage);
                    }
                });
                Toast.makeText(EditProfile.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void UpdateImage() {
        //open gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1000);
    }


}