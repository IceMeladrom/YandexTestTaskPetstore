package ru.dzhioev.petstore.api.clients;

import ru.dzhioev.petstore.api.models.User;
import ru.dzhioev.petstore.api.specs.ApiSpecification;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@Slf4j
public class UserClient extends BaseClient {

    public UserClient() {
        super(ApiSpecification.getDefaultRequestSpec());
    }

    public Response createUser(User user) {
        return post("/user", user);
    }

    public Response createUsersWithList(List<User> users) {
        return post("/user/createWithList", users);
    }

    public Response createUsersWithArray(User[] users) {
        return post("/user/createWithArray", users);
    }

    public Response getUserByUsername(String username) {
        return given()
                .spec(requestSpec)
                .pathParam("username", username)
                .when()
                .get("/user/{username}");
    }

    public Response updateUser(String username, User user) {
        return given()
                .spec(requestSpec)
                .pathParam("username", username)
                .body(user)
                .when()
                .put("/user/{username}");
    }

    public Response deleteUser(String username) {
        return given()
                .spec(requestSpec)
                .pathParam("username", username)
                .when()
                .delete("/user/{username}");
    }

    public Response loginUser(String username, String password) {
        return get("/user/login", Map.of(
                "username", username,
                "password", password
        ));
    }

    public Response logoutUser() {
        return get("/user/logout");
    }

    public User createUserAndGetResponse(User user) {
        return createUser(user)
                .then()
                .extract()
                .as(User.class);
    }
}