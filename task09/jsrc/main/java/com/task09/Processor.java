package com.task09;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "processor",
	roleName = "processor-role",
	isPublishVersion = true,
    tracingMode = TracingMode.Active,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED)
@EnvironmentVariables(@EnvironmentVariable(key = "table", value = "${target_table}"))
@LambdaUrlConfig(
    authType = AuthType.NONE,
    invokeMode = InvokeMode.BUFFERED)
public class Processor implements RequestHandler<Object, Void> {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Void handleRequest(Object request, Context context) {
        try {
            URLConnection connection = new URL("https://api.open-meteo.com/v1/forecast?latitude=52.52" +
                "&longitude=13.41&current=temperature_2m,wind_speed_10m" +
                "&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m").openConnection();
            String result = new String(((InputStream) connection.getContent()).readAllBytes());
            Forecast forecast = gson.fromJson(result, Forecast.class);
            WeatherItem weatherItem = new WeatherItem();
            weatherItem.setId(UUID.randomUUID().toString());
            weatherItem.setForecast(forecast);

            DynamoDB dynamoDb = new DynamoDB(Regions.EU_CENTRAL_1);
            Table events = dynamoDb.getTable(System.getenv("table"));
            String weatherItemJson = gson.toJson(weatherItem);
            Map<String, Object> resultMap = gson.fromJson(weatherItemJson,
                new TypeToken<Map<String, Object>>() {}.getType());
            events.putItem(Item.fromMap(resultMap));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
	}

    private static class WeatherItem {

        private String id;
        private Forecast forecast;

        private String getId() {
            return id;
        }

        private void setId(String id) {
            this.id = id;
        }

        private Forecast getForecast() {
            return forecast;
        }

        private void setForecast(Forecast forecast) {
            this.forecast = forecast;
        }

    }

    private static class Forecast {

        private BigDecimal elevation;
        private BigDecimal generationtime_ms;
        private Hourly hourly;
        private HourlyUnits hourly_units;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String timezone;
        private String timezone_abbreviation;
        private BigInteger utc_offset_seconds;

        public BigDecimal getElevation() {
            return elevation;
        }

        public void setElevation(BigDecimal elevation) {
            this.elevation = elevation;
        }

        public BigDecimal getGenerationtime_ms() {
            return generationtime_ms;
        }

        public void setGenerationtime_ms(BigDecimal generationtime_ms) {
            this.generationtime_ms = generationtime_ms;
        }

        public Hourly getHourly() {
            return hourly;
        }

        public void setHourly(Hourly hourly) {
            this.hourly = hourly;
        }

        public HourlyUnits getHourly_units() {
            return hourly_units;
        }

        public void setHourly_units(HourlyUnits hourly_units) {
            this.hourly_units = hourly_units;
        }

        public BigDecimal getLatitude() {
            return latitude;
        }

        public void setLatitude(BigDecimal latitude) {
            this.latitude = latitude;
        }

        public BigDecimal getLongitude() {
            return longitude;
        }

        public void setLongitude(BigDecimal longitude) {
            this.longitude = longitude;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getTimezone_abbreviation() {
            return timezone_abbreviation;
        }

        public void setTimezone_abbreviation(String timezone_abbreviation) {
            this.timezone_abbreviation = timezone_abbreviation;
        }

        public BigInteger getUtc_offset_seconds() {
            return utc_offset_seconds;
        }

        public void setUtc_offset_seconds(BigInteger utc_offset_seconds) {
            this.utc_offset_seconds = utc_offset_seconds;
        }
    }

    private static class Hourly {

        private List<String> time;
        private List<BigDecimal> temperature_2m;

        public List<String> getTime() {
            return time;
        }

        public void setTime(List<String> time) {
            this.time = time;
        }

        public List<BigDecimal> getTemperature_2m() {
            return temperature_2m;
        }

        public void setTemperature_2m(List<BigDecimal> temperature_2m) {
            this.temperature_2m = temperature_2m;
        }
    }

    private static class HourlyUnits {

        private String time;
        private String temperature_2m;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getTemperature_2m() {
            return temperature_2m;
        }

        public void setTemperature_2m(String temperature_2m) {
            this.temperature_2m = temperature_2m;
        }

    }

}
