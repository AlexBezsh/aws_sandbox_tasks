package com.task10.handlers;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.task10.models.CreateReservationResponse;
import com.task10.models.ErrorResponse;
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
        try {
            String id = UUID.randomUUID().toString();
            Map<String, Object> reservation = gson.fromJson(event.getBody(),
                new TypeToken<Map<String, Object>>() {}.getType());
            reservation.put("id", id);
            Table table = dynamoDB.getTable(System.getenv("reservations_table"));
            table.putItem(Item.fromMap(reservation));
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(gson.toJson(new CreateReservationResponse(id)));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }

}