package com.example.recycletree;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class StatisticFragment extends Fragment {
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String userId;
    float brown_double , orange_double , blue_double ;
    float count_brown;
    float count_blue;
    float count_orange;
    float total;
    public StatisticFragment() {
        // Required empty public constructor
    }
    // Lib
    PieChart pieChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistic,container,false);
        pieChart = view.findViewById(R.id.pie_chart);
        setupPieChart();
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
                    count_blue = value.getLong("Blue").floatValue();
                    count_brown = value.getLong("Brown").floatValue();
                    count_orange = value.getLong("Orange").floatValue();
                    total = count_blue+count_orange+count_brown;
                    Log.d("Statistic", "Value Retrieved: "+total+' '+count_brown+" "+count_blue+" "+count_orange);
                    blue_double = (count_blue/total);
                    brown_double = (count_brown/total);
                    orange_double = (count_orange/total);
                    Log.d("Statistic", "Value Count: "+blue_double+" "+brown_double+" "+orange_double);
                    loadPieChartData(brown_double, blue_double, orange_double);

                } else {
                    Log.d("tag", "Document not exist!");
                }
            }
        });






        return view;
    }

    private void setupPieChart(){
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        pieChart.setCenterText("Recycle");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setEnabled(false);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);

    }


    private void loadPieChartData(float brown, float blue, float orange){
        //divide
        //data read from database
        final DecimalFormat df = new DecimalFormat("0.000f");

        float orange_area = Float.parseFloat(df.format(orange));
        float brown_area = Float.parseFloat(df.format(brown));
        float blue_area = Float.parseFloat(df.format(blue));
        Log.d("Statistic", "pie Data: "+blue_area+" "+orange_area+ " "+brown_area);
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(orange_area,"Aluminum"));
        entries.add(new PieEntry( blue_area,"Paper"));
        entries.add(new PieEntry(brown_area,"Glass"));

        ArrayList<Integer> colors = new ArrayList<>();
        for(int color: ColorTemplate.PASTEL_COLORS){
            colors.add(color);
        }

        for(int color: ColorTemplate.VORDIPLOM_COLORS){
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Recycle");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EaseInQuad);
    }
}
