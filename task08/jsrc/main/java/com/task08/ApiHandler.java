package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
    layers = "sdk-layer")
@LambdaLayer(
    layerName = "sdk-layer",
    libraries = {"lib/commons-io-2.18.0.jar"},
    runtime = DeploymentRuntime.JAVA11,
    architectures = {Architecture.ARM64},
    artifactExtension = ArtifactExtension.ZIP)
@LambdaUrlConfig(
    authType = AuthType.NONE,
    invokeMode = InvokeMode.BUFFERED)
public class ApiHandler implements RequestHandler<Object, Object> {

    private static final URLConnection CONNECTION;

    static {
        try {
            CONNECTION = new URL("https://api.open-meteo.com/v1/forecast?" +
                "latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&" +
                "hourly=temperature_2m,relative_humidity_2m,wind_speed_10m").openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getWeather() {
        try {
            return new String(IOUtils.toByteArray(((InputStream) CONNECTION.getContent())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	public Object handleRequest(Object request, Context context) {
        context.getLogger().log("Start");
		return getWeather();
	}

}
