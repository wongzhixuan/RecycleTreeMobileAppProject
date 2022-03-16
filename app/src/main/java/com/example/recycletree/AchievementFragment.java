package com.example.recycletree;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private List<Task> taskList;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String userId;


    int point ;
    private TextView tv_points;
    public AchievementFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.achievement, container, false);
        tv_points = view.findViewById(R.id.achievement_points);
        // Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        // Get current user instance
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();
        // Retrieve user attributes from firestore
        DocumentReference documentReference = firestore.collection("users").document(userId);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                point = value.getLong("Points").intValue();
                tv_points.setText(String.valueOf(point));

            }
        });

        taskList = new ArrayList<>();
        prepareTask();
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerAdapter = new RecyclerAdapter(taskList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter.setOnItemClickListener(new ClickListener<Task>() {
            @Override
            public void onItemCLick(Task task) {
                Toast.makeText(getActivity(), task.getTask_name(),Toast.LENGTH_SHORT).show();
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        Map<String, Object> edited = new HashMap<>();
                        edited.put("Points", point);
                        documentReference.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("AchivementFragment", "updated" + point );
                            }
                        });

                    }
                });
            }
        });
        recyclerView.setAdapter(recyclerAdapter);

        return view;
    }

    private void prepareTask(){
        Task task = new Task("Recycle 1 Plastic/Aluminium", 5);
        taskList.add(task);
        task = new Task("Recycle 1 Glass", 5);
        taskList.add(task);
        task = new Task("Recycle 1 Paper", 5);
        taskList.add(task);
    }
}
