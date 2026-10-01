package com.example.taxitripcalculator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TaxiViewModel extends ViewModel {
    private final MutableLiveData<TaxiState> state = new MutableLiveData<>(TaxiState.initial());

    public LiveData<TaxiState> getState() {
        return state;
    }

    private void update(TaxiState newState) {
        state.setValue(newState);
    }

    private TaxiState current() {
        if (state.getValue() == null) {
            return TaxiState.initial();
        }
        return state.getValue();
    }

    public void setCarClass(String value) {
        update(current().withCarClass(value));
    }

    public void setZone(String value) {
        update(current().withZone(value));
    }

    public void setKm(double value) {
        update(current().withKm(value));
    }

    public void setMinutes(int value) {
        update(current().withMinutes(value));
    }

    public void setPassengers(int value) {
        update(current().withPassengers(value));
    }

    public void setOptions(boolean seat, boolean luggage, boolean waiting) {
        update(current().withOptions(seat, luggage, waiting));
    }

    public void setDemand(double value) {
        TaxiState s = current();
        update(new TaxiState(s.carClass, s.zone, s.km, s.minutes, s.passengers, s.seat,
                s.luggage, s.waiting, value)
        );
    }

    public int total(TaxiState s) {
        double base = baseFor(s.carClass);
        double kmPrice = kmPriceFor(s.carClass, s.zone);
        double minutePrice = minutePriceFor(s.carClass);
        double options = 0;
        if (s.seat) {
            options += 100;
        }

        if (s.luggage) {
            options += 80;
        }

        if (s.waiting) {
            options += 150;
        }

        double passengersExtra = Math.max(0, s.passengers - 4) * 50;

        double result = (base + s.km * kmPrice + s.minutes * minutePrice
                + options + passengersExtra) * s.demand;

        return (int) Math.round(result);
    }


    public double[] priceParts(TaxiState s) {
        double base = baseFor(s.carClass);
        double km = s.km * kmPriceFor(s.carClass, s.zone);
        double minutes = s.minutes * minutePriceFor(s.carClass);
        double options = (s.seat ? 100 : 0) + (s.luggage ? 80 : 0) + (s.waiting ? 150 : 0);
        double passenger = Math.max(0, s.passengers - 4) * 50;

        return new double[] {base, km, minutes, options + passenger};
    }

    private double baseFor(String car) {
        switch (car) {
            case "Комфорт":
                return 180;
            case "Бизнес":
                return 350;
            default:
                return 120;
        }
    }

    private double kmPriceFor(String car, String zone) {
        double city;
        if (car.equals("Комфорт")) {
            city = 32;
        } else if (car.equals("Бизнес")) {
            city = 55;
        } else {
            city = 22;
        }

        if (zone.equals("За городом")) {
            return city * 1.5;
        }
        return city;
    }

    private double minutePriceFor(String car) {
        switch (car) {
            case "Комфорт":
                return 7;
            case "Бизнес":
                return 12;
            default:
                return 5;
        }
    }
}