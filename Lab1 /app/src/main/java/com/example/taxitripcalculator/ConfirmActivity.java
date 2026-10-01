package com.example.taxitripcalculator;


import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;


public class ConfirmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        int total = getIntent().getIntExtra("total", 0);
        String car = getIntent().getStringExtra("carClass");
        String zone = getIntent().getStringExtra("zone");
        double km = getIntent().getDoubleExtra("km", 0);
        int minutes = getIntent().getIntExtra("minutes", 0);
        int passengers = getIntent().getIntExtra("passengers", 1);

        TextView details = findViewById(R.id.details);
        details.setText("Класс: " + car + "\nЗона: " + zone + "\nРасстояние: " + km + " км"
                + "\nВремя: " + minutes + " мин." + "\nПассажиров: " + passengers
                + "\n\nИтоговая стоимость: " + total + " ₽");

        MaterialButton confirm = findViewById(R.id.confirmButton);
        confirm.setOnClickListener(v -> {
                    Intent data = new Intent();
                    data.putExtra("orderId", "TAXI-" + System.currentTimeMillis());
                    setResult(RESULT_OK, data);
                    finish();
                }
        );

        findViewById(R.id.cancelButton).setOnClickListener(v -> finish());
    }
}