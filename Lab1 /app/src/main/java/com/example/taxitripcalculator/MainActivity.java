package com.example.taxitripcalculator;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity {
    private TaxiViewModel viewModel;
    private TextView passengerCount, totalText, demandLabel;
    private android.widget.EditText kmInput, minInput;
    private SeekBar demandSeekBar;
    private PriceStructureView priceStructure;
    private MaterialButtonToggleGroup carGroup, zoneGroup;
    private Chip seatChip, luggageChip, waitChip;

    private final ActivityResultLauncher<Intent>
            confirmLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String orderId = result.getData().getStringExtra("orderId");
                    Snackbar.make(findViewById(android.R.id.content),
                            "Поездка подтверждена: " + orderId, Snackbar.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(TaxiViewModel.class);

        bindViews();
        setupListeners();

        viewModel.getState().observe(this, this::render);
    }

    private void bindViews() {
        passengerCount = findViewById(R.id.passengerCount);
        totalText = findViewById(R.id.totalText);
        demandLabel = findViewById(R.id.demandLabel);
        kmInput = findViewById(R.id.kmInput);
        minInput = findViewById(R.id.minInput);
        demandSeekBar = findViewById(R.id.demandSeekBar);
        priceStructure = findViewById(R.id.priceStructure);
        carGroup = findViewById(R.id.carGroup);
        zoneGroup = findViewById(R.id.zoneGroup);
        seatChip = findViewById(R.id.seatChip);
        luggageChip = findViewById(R.id.luggageChip);
        waitChip = findViewById(R.id.waitChip);
    }

    private void setupListeners() {
        carGroup.addOnButtonCheckedListener((group,
                                             checkedId, isChecked) -> {
                    if (!isChecked) {
                        return;
                    }
                    if (checkedId == R.id.carEconomy) {
                        viewModel.setCarClass("Эконом");
                    } else if (checkedId == R.id.carComfort) {
                        viewModel.setCarClass("Комфорт");
                    } else {
                        viewModel.setCarClass("Бизнес");
                    }
                }
        );
        zoneGroup.addOnButtonCheckedListener((group,
                                              checkedId, isChecked) -> {

                    if (!isChecked) {
                        return;
                    }
                    if (checkedId == R.id.zoneOut) {
                        viewModel.setZone("За городом");
                    } else {
                        viewModel.setZone("Город");
                    }
                }
        );
        findViewById(R.id.minusButton).setOnClickListener(
                v -> {
                    TaxiState state = viewModel.getState().getValue();
                    if (state != null) {
                        viewModel.setPassengers(state.passengers - 1);
                    }
                }
        );
        findViewById(R.id.plusButton).setOnClickListener(
                v -> {
                    TaxiState state = viewModel.getState().getValue();
                    if (state != null) {
                        viewModel.setPassengers(state.passengers + 1);
                    }
                }
        );
        seatChip.setOnCheckedChangeListener(
                (button, checked) -> updateOptions());
        luggageChip.setOnCheckedChangeListener(
                (button, checked) -> updateOptions());
        waitChip.setOnCheckedChangeListener(
                (button, checked) -> updateOptions());

        TextWatcher watcher =
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            if (s == kmInput.getText()) {
                                viewModel.setKm(Double.parseDouble(s.toString()));
                            } else if (s == minInput.getText()) {
                                viewModel.setMinutes(Integer.parseInt(s.toString()));
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                };

        kmInput.addTextChangedListener(watcher);
        minInput.addTextChangedListener(watcher);

        demandSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                        double demand = 1.0 + progress / 100.0;
                        viewModel.setDemand(demand);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar bar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar bar) {
                    }
                }
        );

        findViewById(R.id.orderButton).setOnClickListener(v -> openConfirm());
    }

    private void updateOptions() {
        viewModel.setOptions(seatChip.isChecked(), luggageChip.isChecked(), waitChip.isChecked());
    }

    private void render(TaxiState s) {
        passengerCount.setText(String.valueOf(s.passengers));
        totalText.setText("Итого: " + viewModel.total(s) + " ₽");
        demandLabel.setText("Коэффициент спроса: " + String.format(java.util.Locale.US,
                "%.2f", s.demand));

        demandSeekBar.setProgress((int) Math.round((s.demand - 1.0) * 100));

        priceStructure.setData(viewModel.priceParts(s));
    }

    private void openConfirm() {
        TaxiState s = viewModel.getState().getValue();
        if (s == null) {
            return;
        }
        Intent intent = new Intent(this, ConfirmActivity.class);
        intent.putExtra("total", viewModel.total(s));
        intent.putExtra("carClass", s.carClass);
        intent.putExtra("zone", s.zone);
        intent.putExtra("km", s.km);
        intent.putExtra("minutes", s.minutes);
        intent.putExtra("passengers", s.passengers);

        confirmLauncher.launch(intent);
    }
}