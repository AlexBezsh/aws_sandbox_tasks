package com.task11.models;

import java.util.List;

public class ReservationsResponse {

    private List<Reservation> reservations;

    public ReservationsResponse(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
