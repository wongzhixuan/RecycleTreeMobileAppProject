package com.example.recycletree;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.cdev.achievementview.AchievementView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    public HomeFragment() {
        // Required empty public constructor
    }
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference storageReference;
    private ImageView treeImage;

    private com.google.android.material.imageview.ShapeableImageView userImage;
    private TextView Username;

    FloatingActionButton drop;
    ProgressBar expBar;
    private TextView levelBar;
    private TextView tv_points;
    private TextView tv_exp;
    int point ;
    int currentLv ;
    int currentxp ;
    int addXp = 10;
    private FirebaseAuth mAuth;
    private String userId;
    int maxLevel = 10;
    private Dialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home,container,false);



        drop = view.findViewById(R.id.drop_button);
        tv_points = view.findViewById(R.id.text_point);
        tv_exp = view.findViewById(R.id.text_exp);
        expBar = view.findViewById(R.id.exp_bar);
        levelBar = view.findViewById(R.id.text_level);
        userImage = view.findViewById(R.id.home_userImage);
        Username = view.findViewById(R.id.home_Uname);
        treeImage = view.findViewById(R.id.ivtree);

        // Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        // Get current user instance
        FirebaseUser currentUser = mAuth.getCurrentUser();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //      Retrieve the user profile image from storage
        StorageReference profileRef = storageReference.child("users/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(userImage);
            }
        });
        userId = currentUser.getUid();
        // Retrieve user attributes from firestore
        DocumentReference documentReference = firestore.collection("users").document(userId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {
                    Username.setText(value.getString("Username"));
                    point = value.getLong("Points").intValue();
                    currentxp = value.getLong("CurrentXP").intValue();
                    currentLv = value.getLong("Lv").intValue();
                    Log.d("HomeFragment", "Value get: "+point+" "+currentLv+" "+currentxp);
                    //Data read from database
                    expBar.setProgress(currentxp);
                    expBar.setMax(100);
                    //Show data
                    tv_points.setText(String.valueOf(point));
                    levelBar.setText(("Level ") + String.valueOf(currentLv));
                    tv_exp.setText(String.valueOf(expBar.getProgress())+" / "+String.valueOf(expBar.getMax()));
                    updateTree();

                    if(currentLv == maxLevel){
                        showAlertDialog();

                    }

                } else {
                    Log.d("tag", "Document not exist!");
                }
            }
        });

        drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLv == maxLevel){
                    Toast.makeText(getActivity(), "You have reached the Maximum level.", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (point > 0) {
                        point--;
                        tv_points.setText(String.valueOf(point));
                        ExperienceManager em = new ExperienceManager();
                        em.addExperience(expBar, levelBar, addXp);
                        //Link to database
                        Map<String, Object> edited = new HashMap<>();
                        edited.put("Points", point);
                        edited.put("CurrentXP", currentxp);
                        edited.put("Lv", currentLv);
                        documentReference.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("HomeFragment", "updated" + point + " " + currentLv + " " + currentxp);
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "Points not enough", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    private void showAlertDialog() {
        Button ok;
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_layout_design);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.getAttributes().windowAnimations = R.style.DialogAimation;

        ok = dialog.findViewById(R.id.alertPositive);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(true);
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        dialog.show();

    }

    private void updateTree() {
        if(currentLv < 1){
            treeImage.setImageResource(R.drawable.tree1);
        }
        else if(currentLv < 3){
            treeImage.setImageResource(R.drawable.tree2);
        }
        else if(currentLv < 5){
            treeImage.setImageResource(R.drawable.tree3);
        }
        else if(currentLv < 7){
            treeImage.setImageResource(R.drawable.tree4);
        }
        else if (currentLv < 9){
            treeImage.setImageResource(R.drawable.tree5);
        }
        else{
            treeImage.setImageResource(R.drawable.tree6);
        }
    }

    public class ExperienceManager{

        public int addExperience(ProgressBar expBar, TextView levelBar, int addXp){

            int addlevel = 0;

            //Check added xp ensure exp
            while(addXp >= expBar.getMax()){
                addXp -= expBar.getMax();
                addlevel += 1;
            }
            //add level progress make sure under max level
            if(currentLv < maxLevel) {
                //make sure the exp is only deducted in correct way and take to next level
                if (addXp >= expBar.getMax() - currentxp) {
                    currentxp = currentxp + addXp - expBar.getMax();
                    expBar.setProgress(currentxp);
                    addlevel += 1;
                } else {
                    //normal situation (current)
                    currentxp = currentxp + addXp;
                    expBar.setProgress(currentxp);
                    tv_exp.setText(String.valueOf(currentxp) + " / " + String.valueOf(expBar.getMax()));
                }
                // if level up
                if (addlevel >= 1) {
                    currentLv += addlevel;
                    tv_exp.setText(String.valueOf(currentxp) + " / " + String.valueOf(expBar.getMax()));
                    Toast.makeText(getActivity(), "Level up!", Toast.LENGTH_SHORT).show();
                    levelBar.setText("Level " + currentLv);
                }
            }else if(currentLv == maxLevel){
                Toast.makeText(getActivity(), "You have reached the Maximum level.", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(getActivity(), "You have reached the full level.", Toast.LENGTH_SHORT).show();
            }
            return currentLv;
        }


    }
}
