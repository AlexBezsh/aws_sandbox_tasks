package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.task10.models.ErrorResponse;
import com.task10.models.SignInRequest;
import com.task10.models.SignInResponse;
import java.util.Map;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;

public class PostSignInHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson;
    private final CognitoIdentityProviderClient cognito;

    public PostSignInHandler(Gson gson, CognitoIdentityProviderClient cognito) {
        this.gson = gson;
        this.cognito = cognito;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent event, Context context) {
        try {
            SignInRequest request = gson.fromJson(event.getBody(), SignInRequest.class);
            String accessToken = cognitoSignIn(request.getEmail(), request.getPassword())
                .authenticationResult()
                .idToken();

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(gson.toJson(new SignInResponse(accessToken)));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }

    protected AdminInitiateAuthResponse cognitoSignIn(String username, String password) {
        String userPool = System.getenv("booking_userpool");
        Map<String, String> authParams = Map.of(
            "USERNAME", username,
            "PASSWORD", password
        );
        return cognito.adminInitiateAuth(AdminInitiateAuthRequest.builder()
            .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
            .authParameters(authParams)
            .userPoolId(userPool)
            .clientId(userPool)
            .build());
    }

}
