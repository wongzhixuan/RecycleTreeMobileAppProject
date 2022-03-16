package com.example.recycletree;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executor;


public class Profile extends Fragment {
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference storageReference;
    //Initialization
    private Button btnChangePwd, btnUpdateProfile;
    private com.google.android.material.imageview.ShapeableImageView userImage;
    private EditText etUname, etDescrip, etEmail;
    private FirebaseAuth mAuth;
    private String userId;
    private TextView tv_point;
    public Profile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.activity_profile,container,false);


        // Link widgets
        etUname = view.findViewById(R.id.etUname_profile);
        etEmail = view.findViewById(R.id.etEmail_profile);
        etDescrip = view.findViewById(R.id.etDescription);
        userImage = view.findViewById(R.id.userImage);
        tv_point = view.findViewById(R.id.tv_points);


        // Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Get current user instance
        FirebaseUser currentUser = mAuth.getCurrentUser();

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

        // Return to sign up page if user haven't login
        if (currentUser == null) {
            Toast.makeText(getActivity(), "You haven't sign in!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), SignUpActivity.class);
            startActivity(intent);
        } else {
            userId = currentUser.getUid();
            // Retrieve user attributes from firestore
            DocumentReference documentReference = firestore.collection("users").document(userId);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value.exists()) {
                        etUname.setText(value.getString("Username"));
                        etEmail.setText(value.getString("Email"));
                        etDescrip.setText(value.getString("Description"));
                        int point = value.getLong("Points").intValue();
                        tv_point.setText(String.valueOf(point));
                    } else {
                        Log.d("tag", "Document not exist!");
                    }
                }
            });


            // Edit user profile
            btnUpdateProfile = view.findViewById(R.id.btnEditProfile);
            btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // pass data to edit profile activity
                    Intent intent = new Intent(view.getContext(), EditProfile.class);
                    intent.putExtra("Username", etUname.getText().toString() );
                    intent.putExtra("Email", etEmail.getText().toString());
                    intent.putExtra("Description", etDescrip.getText().toString());
                    intent.putExtra("Points", tv_point.getText().toString());
                    startActivity(intent);
                }
            });

            // Change password
            btnChangePwd = view.findViewById(R.id.btnChgPwd);
            btnChangePwd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText resetPwd = new EditText(view.getContext());

                    final AlertDialog.Builder pwdDialog = new AlertDialog.Builder(view.getContext());
                    pwdDialog.setTitle("Reset Password ?");
                    pwdDialog.setMessage("Enter new password with >6 characters.");
                    pwdDialog.setView(resetPwd);

                    pwdDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String newPwd = resetPwd.getText().toString().trim();

                            currentUser.updatePassword(newPwd).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getActivity(), "Password Reset Successfully", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                        }
                    });
                    pwdDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    pwdDialog.create().show();
                }
            });

        }

        return view;
    }


}