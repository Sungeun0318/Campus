package com.example.campus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    Button btnTour, btnAiCare, btnMarket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        btnTour = findViewById(R.id.btnTour);
        btnAiCare = findViewById(R.id.btnAiCare);
        btnMarket = findViewById(R.id.btnMarket);

        btnAiCare.setOnClickListener(v -> {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
