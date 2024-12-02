package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import java.util.Map;

@LambdaUrlConfig
@LambdaHandler(
    lambdaName = "hello_world",
    roleName = "hello_world-role",
    aliasName = "${lambdas_alias_name}",
    logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED)
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        System.out.println("Hello from lambda");
        if ("/hello".equals(getPath(request))) {
            return buildResponse(200, Map.of("statusCode", 200, "message", "Hello from Lambda"));
        }
        String message = String.format("Bad request syntax or unsupported method. " +
            "Request path: %s. HTTP method: %s", getPath(request), getMethod(request));
        return buildResponse(400, Map.of("statusCode", 400, "message", message));
    }

    private APIGatewayV2HTTPResponse buildResponse(int statusCode, Object body) {
        return APIGatewayV2HTTPResponse.builder()
            .withStatusCode(statusCode)
            .withHeaders(responseHeaders)
            .withBody(gson.toJson(body))
            .build();
    }

    private String getMethod(APIGatewayV2HTTPEvent requestEvent) {
        return requestEvent.getRequestContext().getHttp().getMethod();
    }

    private String getPath(APIGatewayV2HTTPEvent requestEvent) {
        return requestEvent.getRequestContext().getHttp().getPath();
    }

}
