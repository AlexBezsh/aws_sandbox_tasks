package com.task11.handlers;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.task11.models.ErrorResponse;
import java.math.BigInteger;
import static com.task11.handlers.GetTablesHandler.getTableInfo;

public class GetTableByIdHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson;
    private final DynamoDB dynamoDB;

    public GetTableByIdHandler(Gson gson, DynamoDB dynamoDB) {
        this.gson = gson;
        this.dynamoDB = dynamoDB;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("GetTableByIdHandler: start");
        try {
            logger.log("GetTableByIdHandler: getting DynamoDB table");
            Table tables = dynamoDB.getTable(System.getenv("tables_table"));
            logger.log("GetTableByIdHandler: getting table id from URL " + event.getPath());
            String id = event.getPath().substring(8);
            logger.log("GetTableByIdHandler: table id is " + id);
            logger.log("GetTableByIdHandler: fetching table");
            Item item = tables.getItem(new PrimaryKey(new KeyAttribute("id", new BigInteger(id))));
            logger.log("GetTableByIdHandler: returning response");
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(gson.toJson(getTableInfo(item, logger)));
        } catch (Exception e) {
            logger.log("GetTableByIdHandler: exception occurred. Reason: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }

}
