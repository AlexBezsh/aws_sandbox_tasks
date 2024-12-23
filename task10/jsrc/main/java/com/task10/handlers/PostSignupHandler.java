package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.task10.excetion.ValidationException;
import com.task10.models.SignUpRequest;
import java.util.Map;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeliveryMediumType;

public class PostSignupHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Gson gson;
    private final CognitoIdentityProviderClient cognito;
    private final String clientId = System.getenv("CLIENT_ID");
    private final String cognitoId = System.getenv("COGNITO_ID");

    public PostSignupHandler(Gson gson, CognitoIdentityProviderClient cognito) {
        this.gson = gson;
        this.cognito = cognito;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("PostSignupHandler: start");
        try {
            logger.log("PostSignupHandler: parsing request");
            SignUpRequest request = gson.fromJson(event.getBody(), SignUpRequest.class);
            logger.log("PostSignupHandler: validating password");
            validatePassword(request.getPassword(), logger);
            logger.log("PostSignupHandler: performing cognito sign up");
            cognitoSignUp(request);
            logger.log("PostSignupHandler: confirming sign up");
            confirmSignUp(request);
            logger.log("PostSignupHandler: returning response");
            return new APIGatewayProxyResponseEvent().withStatusCode(200);
        } catch (Exception e) {
            logger.log("PostSignupHandler: exception occurred. Reason: " + e.getMessage());
            return new APIGatewayProxyResponseEvent().withStatusCode(400);
        }
    }

    public static void validatePassword(String password, LambdaLogger logger) {
        if (!password.matches("(?=.*[$%^*_-]).{12,}")) {
            logger.log("Password is invalid: " + password);
            throw new ValidationException("Invalid password");
        }
    }

    private void cognitoSignUp(SignUpRequest request) {
        cognito.adminCreateUser(AdminCreateUserRequest.builder()
            .userPoolId(System.getenv("COGNITO_ID"))
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

    protected AdminRespondToAuthChallengeResponse confirmSignUp(SignUpRequest request) {
        AdminInitiateAuthResponse
            response = cognitoSignIn(request.getEmail(), request.getPassword());

        if (!ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(response.challengeNameAsString())) {
            throw new RuntimeException("unexpected challenge: " + response.challengeNameAsString());
        }

        Map<String, String> challengeResponses = Map.of(
            "USERNAME", request.getEmail(),
            "PASSWORD", request.getPassword(),
            "NEW_PASSWORD", request.getPassword()
        );

        return cognito.adminRespondToAuthChallenge(AdminRespondToAuthChallengeRequest.builder()
            .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
            .challengeResponses(challengeResponses)
            .userPoolId(cognitoId)
            .clientId(clientId)
            .session(response.session())
            .build());
    }

    protected AdminInitiateAuthResponse cognitoSignIn(String nickName, String password) {
        Map<String, String> authParams = Map.of(
            "USERNAME", nickName,
            "PASSWORD", password);

        return cognito.adminInitiateAuth(AdminInitiateAuthRequest.builder()
            .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
            .authParameters(authParams)
            .userPoolId(cognitoId)
            .clientId(clientId)
            .build());
    }

}
