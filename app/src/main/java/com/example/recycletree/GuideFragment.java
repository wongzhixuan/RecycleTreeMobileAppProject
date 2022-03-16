package com.example.recycletree;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class GuideFragment extends Fragment {
    private Button btn1, btn2, btn3;
    public GuideFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_guide,container,false);
        btn1 = (Button) view.findViewById(R.id.expand1);
        btn2 = (Button) view.findViewById(R.id.expand2);
        btn3 = (Button) view.findViewById(R.id.expand3);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment selectedFragment = new GuideFragment1();
                getChildFragmentManager().beginTransaction().replace(R.id.fragment_guide_container,selectedFragment,null).addToBackStack(null).commit();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment selectedFragment = new GuideFragment2();
                getChildFragmentManager().beginTransaction().replace(R.id.fragment_guide_container,selectedFragment).addToBackStack(null).commit();

            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment selectedFragment = new GuideFragment3();
                getChildFragmentManager().beginTransaction().replace(R.id.fragment_guide_container,selectedFragment).addToBackStack(null).commit();

            }
        });

        return view;


    }
}
