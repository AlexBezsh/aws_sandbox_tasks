package com.task05;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.RetentionSetting;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import static com.syndicate.deployment.model.ResourceType.DYNAMODB_TABLE;

@LambdaHandler(
    lambdaName = "api_handler",
    roleName = "api_handler-role",
    isPublishVersion = true,
    aliasName = "${lambdas_alias_name}",
    logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED)
@DependsOn(name = "Events", resourceType = DYNAMODB_TABLE)
@EnvironmentVariables({@EnvironmentVariable(key = "table", value = "${target_table}")})
public class ApiHandler implements RequestHandler<ApiHandler.Event, Map<String, Object>> {

    public Map<String, Object> handleRequest(Event request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Request principal ID: " + request.getPrincipalId());
        logger.log("Request content: " + request.getContent());

        logger.log("Generating id");
        String id = UUID.randomUUID().toString();

        logger.log("Getting DynamoDB table");
        DynamoDB dynamoDb = new DynamoDB(Regions.EU_CENTRAL_1);
        Table events = dynamoDb.getTable(System.getenv("table"));

        logger.log("Creating DynamoDB item");
        Map<String, Object> dynamoDbItem = Map.of(
            "id", id,
            "principalId", request.getPrincipalId(),
            "createdAt", LocalDateTime.now().toString(),
            "body", request.getContent());

        logger.log("Saving item");
        events.putItem(Item.fromMap(dynamoDbItem));

        logger.log("Returning response");
        return Map.of(
            "statusCode", 201,
            "event", dynamoDbItem);
    }

    public static class Event {
        private Integer principalId;
        private Map<String, String> content;

        public void setPrincipalId(Integer principalId) {
            this.principalId = principalId;
        }

        public int getPrincipalId() {
            return principalId;
        }

        public void setContent(Map<String, String> content) {
            this.content = content;
        }

        public Map<String, String> getContent() {
            return content;
        }

    }

}
