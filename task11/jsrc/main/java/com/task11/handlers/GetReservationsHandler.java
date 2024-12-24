package com.task11.handlers;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.task11.models.ErrorResponse;
import com.task11.models.Reservation;
import com.task11.models.ReservationsResponse;
import java.util.ArrayList;
import java.util.List;

public class GetReservationsHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson;
    private final DynamoDB dynamoDB;

    public GetReservationsHandler(Gson gson, DynamoDB dynamoDB) {
        this.gson = gson;
        this.dynamoDB = dynamoDB;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("GetReservationsHandler: start");
        try {
            logger.log("GetReservationsHandler: getting reservations DynamodDB table");
            Table reservations = dynamoDB.getTable(System.getenv("reservations_table"));
            List<Reservation> result = new ArrayList<>();
            logger.log("GetReservationsHandler: fetching reservations and converting them");
            reservations.scan().forEach(t -> result.add(getReservation(t, logger)));
            logger.log("GetReservationsHandler: returning response");
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(gson.toJson(new ReservationsResponse(result)));
        } catch (Exception e) {
            logger.log("GetReservationsHandler: exception occurred. Reason: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }

    private Reservation getReservation(Item item, LambdaLogger logger) {
        logger.log("GetReservationsHandler: converting  Item to Reservation");
        Reservation result = new Reservation();
        result.setTableNumber(item.getInt("tableNumber"));
        result.setClientName(item.getString("clientName"));
        result.setPhoneNumber(item.getString("phoneNumber"));
        result.setDate(item.getString("date"));
        result.setSlotTimeStart(item.getString("slotTimeStart"));
        result.setSlotTimeEnd(item.getString("slotTimeEnd"));
        return result;
    }

}
