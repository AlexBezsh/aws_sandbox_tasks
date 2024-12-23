package com.task10;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import com.task10.handlers.GetReservationsHandler;
import com.task10.handlers.GetTableByIdHandler;
import com.task10.handlers.GetTablesHandler;
import com.task10.handlers.PostReservationsHandler;
import com.task10.handlers.PostSignInHandler;
import com.task10.handlers.PostSignupHandler;
import com.task10.handlers.PostTablesHandler;
import com.task10.models.RouteKey;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID;
import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED)
@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${booking_userpool}")
@EnvironmentVariables({
    @EnvironmentVariable(key = "tables_table", value = "${tables_table}"),
    @EnvironmentVariable(key = "reservations_table", value = "${reservations_table}"),
    @EnvironmentVariable(key = "COGNITO_ID", value = "${booking_userpool}", valueTransformer = USER_POOL_NAME_TO_USER_POOL_ID),
    @EnvironmentVariable(key = "CLIENT_ID", value = "${booking_userpool}", valueTransformer = USER_POOL_NAME_TO_CLIENT_ID)})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent,
    APIGatewayProxyResponseEvent> {

    private static final Map<RouteKey, RequestHandler<APIGatewayProxyRequestEvent,
        APIGatewayProxyResponseEvent>> HANDLERS = new HashMap<>();
    private static final CognitoIdentityProviderClient COGNITO = initCognitoClient();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DynamoDB DYNAMO_DB = new DynamoDB(Regions.EU_CENTRAL_1);

    static {
        HANDLERS.put(new RouteKey("POST", "/signup"), new PostSignupHandler(GSON, COGNITO));
        HANDLERS.put(new RouteKey("POST", "/signin"), new PostSignInHandler(GSON, COGNITO));
        HANDLERS.put(new RouteKey("GET", "/tables"), new GetTablesHandler(GSON, DYNAMO_DB));
        HANDLERS.put(new RouteKey("POST", "/tables"), new PostTablesHandler(GSON, DYNAMO_DB));
        HANDLERS.put(new RouteKey("GET", "/tables/.+"), new GetTableByIdHandler(GSON, DYNAMO_DB));
        HANDLERS.put(new RouteKey("POST", "/reservations"), new PostReservationsHandler(GSON, DYNAMO_DB));
        HANDLERS.put(new RouteKey("GET", "/reservations"), new GetReservationsHandler(GSON, DYNAMO_DB));
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("ApiHandler: start");
        RouteKey routeKey = getRouteKey(request);
        logger.log("ApiHandler: route key is " + routeKey);
        return HANDLERS.entrySet().stream()
            .filter(entry -> entry.getKey().getMethod().equals(routeKey.getMethod())
                && routeKey.getPath().matches(entry.getKey().getPath()))
            .peek(entry -> logger.log("Handler: " + entry.getValue()))
            .map(entry -> entry.getValue().handleRequest(request, context))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Handler not found"));
    }

    private RouteKey getRouteKey(APIGatewayProxyRequestEvent request) {
        return new RouteKey(request.getHttpMethod(), request.getPath());
    }

    private static CognitoIdentityProviderClient initCognitoClient() {
        return CognitoIdentityProviderClient.builder()
            .region(Region.EU_CENTRAL_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

}
