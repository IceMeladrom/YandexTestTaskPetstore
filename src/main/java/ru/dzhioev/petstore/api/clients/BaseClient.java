package ru.dzhioev.petstore.api.clients;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static io.restassured.RestAssured.given;

@Slf4j
public abstract class BaseClient {

    protected RequestSpecification requestSpec;

    protected BaseClient(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    protected Response get(String path) {
        log.debug("GET request to: {}", path);
        return given()
                .spec(requestSpec)
                .when()
                .get(path);
    }

    protected Response get(String path, Map<String, ?> queryParams) {
        log.debug("GET request to: {} with params: {}", path, queryParams);
        return given()
                .spec(requestSpec)
                .queryParams(queryParams)
                .when()
                .get(path);
    }

    protected Response get(String path, Object pathParam) {
        log.debug("GET request to: {} with path param: {}", path, pathParam);
        return given()
                .spec(requestSpec)
                .pathParam("id", pathParam)
                .when()
                .get(path);
    }

    protected Response post(String path, Object body) {
        log.debug("POST request to: {} with body: {}", path, body);
        return given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post(path);
    }

    protected Response put(String path, Object body) {
        log.debug("PUT request to: {} with body: {}", path, body);
        return given()
                .spec(requestSpec)
                .body(body)
                .when()
                .put(path);
    }

    protected Response delete(String path) {
        log.debug("DELETE request to: {}", path);
        return given()
                .spec(requestSpec)
                .when()
                .delete(path);
    }

    protected Response delete(String path, Object pathParam) {
        log.debug("DELETE request to: {} with path param: {}", path, pathParam);
        return given()
                .spec(requestSpec)
                .pathParam("id", pathParam)
                .when()
                .delete(path);
    }
}