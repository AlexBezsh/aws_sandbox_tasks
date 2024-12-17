package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@LambdaHandler(
    lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED)
@RuleEventSource(targetRule = "uuid_trigger")
@EnvironmentVariables({@EnvironmentVariable(key = "bucket", value = "${target_bucket}")})
public class UuidGenerator implements RequestHandler<Object, Void> {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public Void handleRequest(Object request, Context context) {
		LambdaLogger logger = context.getLogger();
        logger.log("Generating 10 random UUIDs");
        List<String> uuids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            uuids.add(UUID.randomUUID().toString());
        }

        logger.log("Converting uuids to JSON");
        String result = gson.toJson(new RandomUuids(uuids));

        logger.log("Getting bucket name");
        String bucket = System.getenv("bucket");
        logger.log("Bucket name: " + bucket);

        logger.log("Creating file name");
        String fileName = LocalDateTime.now().toString();
        logger.log("File name: " + fileName);

        logger.log("Creating S3 client");
        try (S3Client s3Client = S3Client.builder().region(Region.EU_CENTRAL_1).build()) {
            logger.log("Uploading file");
            s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(fileName).build(),
                RequestBody.fromBytes(result.getBytes()));
            logger.log("File successfully uploaded");
        }
		return null;
	}

    private static class RandomUuids {

        private List<String> ids;

        public RandomUuids(List<String> ids) {
            this.ids = ids;
        }

        public void setIds(List<String> ids) {
            this.ids = ids;
        }

        public List<String> getIds() {
            return ids;
        }
    }

}
