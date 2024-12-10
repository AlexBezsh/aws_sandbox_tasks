package com.task03;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import java.util.Map;

@LambdaHandler(
    lambdaName = "hello_world",
    roleName = "hello_world-role",
    aliasName = "${lambdas_alias_name}",
    logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED)
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, Map<String, Object>> {

    public Map<String, Object> handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        System.out.println("Hello from lambda");
        return Map.of("statusCode", 200, "message", "Hello from Lambda");
    }

}
