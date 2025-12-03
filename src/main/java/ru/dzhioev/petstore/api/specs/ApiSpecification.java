package ru.dzhioev.petstore.api.specs;

import ru.dzhioev.petstore.config.TestConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.lessThan;

@UtilityClass
public class ApiSpecification {

    private static final TestConfig config = TestConfig.getInstance();

    public static RequestSpecification getDefaultRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(config.getBaseUrl())
                .setContentType(ContentType.JSON)
                .addHeader("api_key", config.getApiKey())
                .log(LogDetail.ALL)
                .build();
    }

    public static RequestSpecification getMultipartRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(config.getBaseUrl())
                .addHeader("api_key", config.getApiKey())
                .setContentType("multipart/form-data")
                .log(LogDetail.ALL)
                .build();
    }

    public static ResponseSpecification getSuccessResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectResponseTime(lessThan((long) config.getTimeout()), TimeUnit.SECONDS)
                .log(LogDetail.ALL)
                .build();
    }
}