package com.task11.models;

public class CreateReservationResponse {

    private String reservationId;

    public CreateReservationResponse(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

}
