package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.task10.models.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeliveryMediumType;

public class PostSignupHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson;
    private final CognitoIdentityProviderClient cognito;

    public PostSignupHandler(Gson gson, CognitoIdentityProviderClient cognito) {
        this.gson = gson;
        this.cognito = cognito;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent event, Context context) {
        try {
            SignUpRequest request = gson.fromJson(event.getBody(), SignUpRequest.class);
            cognitoSignUp(request);
            return new APIGatewayProxyResponseEvent().withStatusCode(200);
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400);
        }
    }

    private void cognitoSignUp(SignUpRequest request) {
        cognito.adminCreateUser(AdminCreateUserRequest.builder()
            .userPoolId(System.getenv("booking_userpool"))
            .username(request.getEmail())
            .temporaryPassword(request.getPassword())
            .userAttributes(
                AttributeType.builder()
                    .name("given_name")
                    .value(request.getFirstName())
                    .build(),
                AttributeType.builder()
                    .name("family_name")
                    .value(request.getLastName())
                    .build(),
                AttributeType.builder()
                    .name("email")
                    .value(request.getEmail())
                    .build(),
                AttributeType.builder()
                    .name("email_verified")
                    .value("true")
                    .build())
            .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
            .messageAction("SUPPRESS")
            .forceAliasCreation(Boolean.FALSE)
            .build()
        );
    }

}
