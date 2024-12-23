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
import com.task10.models.ErrorResponse;
import com.task10.models.TableInfo;
import com.task10.models.TablesResponse;
import java.util.ArrayList;
import java.util.List;

public class GetTablesHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson;
    private final DynamoDB dynamoDB;

    public GetTablesHandler(Gson gson, DynamoDB dynamoDB) {
        this.gson = gson;
        this.dynamoDB = dynamoDB;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("GetTablesHandler: start");
        try {
            logger.log("GetTablesHandler: getting DynamoDB table");
            Table tables = dynamoDB.getTable(System.getenv("tables_table"));
            List<TableInfo> result = new ArrayList<>();
            logger.log("GetTablesHandler: fetching tables and converting them");
            tables.scan().forEach(t -> result.add(getTableInfo(t, logger)));
            logger.log("GetTablesHandler: returning response");
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(gson.toJson(new TablesResponse(result)));
        } catch (Exception e) {
            logger.log("GetTablesHandler: exception occurred. Reason: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }

    public static TableInfo getTableInfo(Item item, LambdaLogger logger) {
        logger.log("GetTablesHandler: converting Item to TableInfo");
        TableInfo result = new TableInfo();
        result.setId(item.getBigInteger("id"));
        result.setNumber(item.getInt("number"));
        result.setPlaces(item.getInt("places"));
        result.setVip(item.getBOOL("isVip"));
        result.setMinOrder(item.getInt("minOrder"));
        return result;
    }

}