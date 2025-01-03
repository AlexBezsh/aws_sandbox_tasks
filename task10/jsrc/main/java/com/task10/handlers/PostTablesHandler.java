package com.task10.handlers;

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
import com.task10.models.CreateTableResponse;
import com.task10.models.ErrorResponse;
import com.task10.models.TableInfo;
import java.util.Map;

public class PostTablesHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson;
    private final DynamoDB dynamoDB;

    public PostTablesHandler(Gson gson, DynamoDB dynamoDB) {
        this.gson = gson;
        this.dynamoDB = dynamoDB;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("PostTablesHandler: start");
        try {
            logger.log("PostTablesHandler: parsing request");
            TableInfo tableInfo = gson.fromJson(event.getBody(), TableInfo.class);
            logger.log("PostTablesHandler: getting DynamoDB table");
            Table tables = dynamoDB.getTable(System.getenv("tables_table"));
            logger.log("PostTablesHandler: creating table");
            Map<String, Object> table = gson.fromJson(event.getBody(),
                new TypeToken<Map<String, Object>>() {}.getType());
            logger.log("PostTablesHandler: uploading table to DynamoDB");
            tables.putItem(Item.fromMap(table));
            logger.log("PostTablesHandler: returning response");
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(gson.toJson(new CreateTableResponse(tableInfo.getId())));
        } catch (Exception e) {
            logger.log("PostTablesHandler: exception occurred. Reason: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }

}