package com.example.recycletree;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cdev.achievementview.AchievementView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class RecycleFragment extends Fragment {
    int brown_f, orange_f, blue_f;
    private ImageButton scanQR;
    private TextView orange, blue, brown;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String userId;


    public RecycleFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recycle, container, false);

        // Link widgets
        scanQR = view.findViewById(R.id.qr_scan);
        orange = view.findViewById(R.id.tv_recycle_orange);
        blue = view.findViewById(R.id.tv_recycle_blue);
        brown = view.findViewById(R.id.tv_recycle_brown);

        // Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Get current user instance
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();

        // Retrieve user attributes from firestore
        DocumentReference documentReference = firestore.collection("users").document(userId).collection("Records").document(userId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {
                    orange_f = value.getLong("Orange").intValue();
                    brown_f = value.getLong("Brown").intValue();
                    blue_f = value.getLong("Blue").intValue();
                    orange.setText(String.valueOf(orange_f));
                    blue.setText(String.valueOf(blue_f));
                    brown.setText(String.valueOf(brown_f));
                    Log.d("RecycleFrag", "Value get: ");

                } else {
                    Log.d("tag", "Document not exist!");
                }
            }
        });

        scanQR.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    case MotionEvent.ACTION_UP: {
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }
                return false;
            }
        });

        // pass data to QrScanner fragment
        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("Blue", blue_f);
                bundle.putInt("Orange",orange_f);
                bundle.putInt("Brown", brown_f);

                Fragment selectedFragment = new QrScanner();
                selectedFragment.setArguments(bundle);
                getChildFragmentManager().beginTransaction().replace(R.id.recycle_frame,selectedFragment,null).addToBackStack(null).commit();

            }
        });



        return view;
    }


}
