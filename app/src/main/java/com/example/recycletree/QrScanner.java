package com.example.recycletree;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.zxing.Result;

import java.util.HashMap;
import java.util.Map;


public class QrScanner extends Fragment {
    private static final int CAMERA_REQUEST_CODE = 101;
    String RESULT_OF_QR = "";
    CodeScanner codeScanner;
    int blue, brown, orange, point;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String userId;
    private ImageButton closeBtn;

    public QrScanner() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.qr_scanner, container, false);

        // get data from parent fragment
        Bundle bundle = this.getArguments();
        brown = bundle.getInt("Brown");
        orange = bundle.getInt("Orange");
        blue = bundle.getInt("Blue");

        // Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Get current user instance
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();

        // Get user current points from firebase
        DocumentReference documentReference2 = firestore.collection("users").document(userId);
        documentReference2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {
                    point = value.getLong("Points").intValue();
                }

            }
        });

        // back to parent fragment
        closeBtn = view.findViewById(R.id.btnClose);
        setUpPermission();
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().popBackStack();
            }
        });

        // QR scanner
        CodeScannerView codeScannerView = view.findViewById(R.id.scanner_view);
        codeScanner = new CodeScanner(getActivity(), codeScannerView);

        codeScanner.setCamera(CodeScanner.CAMERA_BACK);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);
        codeScanner.setScanMode(ScanMode.SINGLE);
        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        codeScanner.setAutoFocusEnabled(true);
        codeScanner.setFlashEnabled(false);


        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RESULT_OF_QR = result.getText();
                        if (RESULT_OF_QR != "") {
                            switch (RESULT_OF_QR) {
                                case "blue":
                                    point = point + 5;
                                    blue++;
                                    Toast.makeText(getActivity(), "You earned 5 points by recycling 1 Paper", Toast.LENGTH_SHORT).show();
                                    break;
                                case "orange":
                                    point = point + 5;
                                    orange++;
                                    Toast.makeText(getActivity(), "You earned 5 points by recycling 1 Aluminum/Plastic", Toast.LENGTH_SHORT).show();
                                    break;
                                case "brown":
                                    point = point + 5;
                                    brown++;
                                    Toast.makeText(getActivity(), "You earned 5 points by recycling 1 Glass", Toast.LENGTH_SHORT).show();
                                    break;
                            }

                            // Update firebase
                            Map<String, Object> map = new HashMap<>();
                            map.put("Blue", blue);
                            map.put("Brown", brown);
                            map.put("Orange", orange);
                            DocumentReference documentReference = firestore.collection("users").document(userId).collection("Records").document(userId);
                            documentReference.update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("RecycleFragment", "Value Updated: " + map);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("RecycleFragment", "Error!" + e.getMessage());

                                }
                            });
                            Map<String, Object> map2 = new HashMap<>();
                            map2.put("Points", point);
                            documentReference2.update(map2).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("qrscan", "Error! cannot update points");
                                }
                            });

                            // back to parent fragment
                            getParentFragmentManager().popBackStackImmediate();
                        }
                        Log.d("qrscan", "Result of scan: " + RESULT_OF_QR);

                    }
                });
            }
        });

        codeScannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeScanner.startPreview();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    public void onPause() {
        QrScanner.super.onPause();
        codeScanner.releaseResources();
    }

    // check permission
    private void setUpPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            MakeRequest();
        }
    }
    // ask for permission
    private void MakeRequest() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //when permission granted

            } else {
                Toast.makeText(getActivity(), "You need camera permission to use this function.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
