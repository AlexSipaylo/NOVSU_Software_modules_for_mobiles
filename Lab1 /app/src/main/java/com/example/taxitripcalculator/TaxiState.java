package com.example.taxitripcalculator;

public class TaxiState {
    public final String carClass;
    public final String zone;
    public final double km;
    public final int minutes;
    public final int passengers;
    public final boolean seat;
    public final boolean luggage;
    public final boolean waiting;
    public final double demand;

    public TaxiState(String carClass, String zone, double km, int minutes, int passengers,
            boolean seat, boolean luggage, boolean waiting, double demand) {
        this.carClass = carClass;
        this.zone = zone;
        this.km = km;
        this.minutes = minutes;
        this.passengers = passengers;
        this.seat = seat;
        this.luggage = luggage;
        this.waiting = waiting;
        this.demand = demand;
    }

    public static TaxiState initial() {
        return new TaxiState("Эконом", "Город", 5, 10, 1,
                false, false, false, 1.0);
    }

    public TaxiState withCarClass(String value) {
        return new TaxiState(value, zone, km, minutes, passengers, seat, luggage, waiting, demand);
    }

    public TaxiState withZone(String value) {
        return new TaxiState(carClass, value, km, minutes, passengers,
                seat, luggage, waiting, demand);
    }

    public TaxiState withKm(double value) {
        return new TaxiState(carClass, zone, Math.max(0, value), minutes, passengers, seat,
                luggage, waiting, demand);
    }

    public TaxiState withMinutes(int value) {
        return new TaxiState(carClass, zone, km, Math.max(0, value), passengers, seat, luggage,
                waiting, demand);
    }

    public TaxiState withPassengers(int value) {
        return new TaxiState(carClass, zone, km, minutes, Math.max(1, value), seat, luggage,
                waiting, demand);
    }

    public TaxiState withOptions(boolean seat, boolean luggage, boolean waiting) {
        return new TaxiState(carClass, zone, km, minutes, passengers,
                seat, luggage, waiting, demand);
    }
}
