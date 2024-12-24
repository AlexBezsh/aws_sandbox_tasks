package com.task11.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.task11.models.ErrorResponse;
import com.task11.models.SignInRequest;
import com.task11.models.SignInResponse;
import java.util.Map;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import static com.task11.handlers.PostSignupHandler.validatePassword;

public class PostSignInHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson;
    private final CognitoIdentityProviderClient cognito;
    private final String clientId = System.getenv("CLIENT_ID");
    private final String cognitoId = System.getenv("COGNITO_ID");

    public PostSignInHandler(Gson gson, CognitoIdentityProviderClient cognito) {
        this.gson = gson;
        this.cognito = cognito;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("PostSignInHandler: start");
        try {
            logger.log("PostSignInHandler: parsing request");
            SignInRequest request = gson.fromJson(event.getBody(), SignInRequest.class);
            logger.log("PostSignInHandler: validating password");
            validatePassword(request.getPassword(), logger);
            logger.log("PostSignInHandler: performing sign in");
            String accessToken = cognitoSignIn(request.getEmail(), request.getPassword(), logger)
                .authenticationResult()
                .idToken();
            logger.log("PostSignInHandler: returning response");
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(gson.toJson(new SignInResponse(accessToken)));
        } catch (Exception e) {
            logger.log("PostSignInHandler: exception occurred. Reason: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }

    protected AdminInitiateAuthResponse cognitoSignIn(String username, String password, LambdaLogger logger) {
        Map<String, String> authParams = Map.of(
            "USERNAME", username,
            "PASSWORD", password);
        logger.log("PostSignInHandler: cognito id is " + cognitoId);
        logger.log("PostSignInHandler: cognito client id is " + clientId);
        return cognito.adminInitiateAuth(AdminInitiateAuthRequest.builder()
            .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
            .authParameters(authParams)
            .userPoolId(cognitoId)
            .clientId(clientId)
            .build());
    }

}
