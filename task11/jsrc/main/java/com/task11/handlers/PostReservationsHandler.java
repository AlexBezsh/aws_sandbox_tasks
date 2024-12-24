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
import com.google.gson.reflect.TypeToken;
import com.task11.excetion.ValidationException;
import com.task11.models.CreateReservationResponse;
import com.task11.models.ErrorResponse;
import java.util.Map;
import java.util.UUID;

public class PostReservationsHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson;
    private final DynamoDB dynamoDB;

    public PostReservationsHandler(Gson gson, DynamoDB dynamoDB) {
        this.gson = gson;
        this.dynamoDB = dynamoDB;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("PostReservationsHandler: start");
        try {
            String id = UUID.randomUUID().toString();
            logger.log("PostReservationsHandler: parsing request");
            Map<String, Object> reservation = gson.fromJson(event.getBody(),
                new TypeToken<Map<String, Object>>() {}.getType());
            logger.log("PostReservationsHandler: validating reservation");
            validate(reservation);
            reservation.put("id", id);
            logger.log("PostReservationsHandler: getting DynamoDB table");
            Table table = dynamoDB.getTable(System.getenv("reservations_table"));
            logger.log("PostReservationsHandler: putting item into DynamoDB table");
            table.putItem(Item.fromMap(reservation));
            logger.log("PostReservationsHandler: returning response");
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(gson.toJson(new CreateReservationResponse(id)));
        } catch (Exception e) {
            logger.log("PostReservationsHandler: exception occurred. Reason: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }

    private void validate(Map<String, Object> reservation) {
        Integer tableNumber = ((Double) reservation.get("tableNumber")).intValue();
        if (!isTableFound(tableNumber)) {
            throw new ValidationException("Table " + tableNumber + " not found");
        }
        if (isReservationFound(tableNumber)) {
            throw new ValidationException("Table " + tableNumber + " is already reserved");
        }
    }

    private boolean isTableFound(Integer number) {
        Table tables = dynamoDB.getTable(System.getenv("tables_table"));
        for (Item item : tables.scan()) {
            if (item.getInt("number") == number) {
                return true;
            }
        }
        return false;
    }

    private boolean isReservationFound(Integer number) {
        Table reservations = dynamoDB.getTable(System.getenv("reservations_table"));
        for (Item item : reservations.scan()) {
            if (item.getInt("tableNumber") == number) {
                return true;
            }
        }
        return false;
    }

}
