package com.example.recycletree;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 5000;
    private ImageView showLogo;
    private TextView recycleText;
    private TextView treeText;
    Animation animLogo, animText1, animText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // make this activity as splash screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Link anim file with java
        animLogo = AnimationUtils.loadAnimation(this, R.anim.animation_logo);
        animText1 = AnimationUtils.loadAnimation(this, R.anim.animation_text1);
        animText2 = AnimationUtils.loadAnimation(this, R.anim.animation_text2);

        //Hook Animation Widget
        showLogo = findViewById(R.id.recycletree_logo);
        recycleText = findViewById(R.id.tv_recycle);
        treeText = findViewById(R.id.tv_tree);

        //Start animation
        showLogo.setAnimation(animLogo);
        recycleText.setAnimation(animText1);
        treeText.setAnimation(animText2);

        // Handler to run splash screen and redirect to next activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent home = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(home);
                finish();
            }
        },SPLASH_TIME_OUT);
    }
}