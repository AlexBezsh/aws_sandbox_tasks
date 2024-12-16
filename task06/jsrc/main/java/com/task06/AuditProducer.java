package com.task06;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "audit_producer",
	roleName = "audit_producer-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 1)
@EnvironmentVariables({@EnvironmentVariable(key = "table", value = "${target_table}")})
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

	public Void handleRequest(DynamodbEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Getting stream record");
        StreamRecord record = event.getRecords().get(0).getDynamodb();

        logger.log("Getting DynamoDB table");
        DynamoDB dynamoDb = new DynamoDB(Regions.EU_CENTRAL_1);
        Table auditTable = dynamoDb.getTable(System.getenv("table"));

        logger.log("Creating DynamoDB item");
        Map<String, Object> dbItem;
        String uuid = UUID.randomUUID().toString();
        String modificationTime = LocalDateTime.now().toString();
        if (record.getOldImage() == null) {
            logger.log("Old image is null");
            dbItem = Map.of(
                "id", uuid,
                "itemKey", record.getNewImage().get("key").getS(),
                "modificationTime", modificationTime,
                "newValue", Map.of(
                    "key", record.getNewImage().get("key").getS(),
                    "value", Integer.parseInt(record.getNewImage().get("value").getN())));
        } else {
            logger.log("Old image is present");
            dbItem = Map.of(
                "id", uuid,
                "itemKey", record.getNewImage().get("key").getS(),
                "modificationTime", modificationTime,
                "updatedAttribute", "value",
                "oldValue", Integer.parseInt(record.getOldImage().get("value").getN()),
                "newValue", Integer.parseInt(record.getNewImage().get("value").getN())
            );
        }

        logger.log("Saving item");
        auditTable.putItem(Item.fromMap(dbItem));
        logger.log("Item successfully saved");

        return null;
	}

}
