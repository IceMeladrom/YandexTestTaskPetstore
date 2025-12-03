package ru.dzhioev.petstore.api.clients;

import ru.dzhioev.petstore.api.models.Order;
import ru.dzhioev.petstore.api.specs.ApiSpecification;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class StoreClient extends BaseClient {

    public StoreClient() {
        super(ApiSpecification.getDefaultRequestSpec());
    }

    public Response getInventory() {
        return get("/store/inventory");
    }

    public Response placeOrder(Order order) {
        return post("/store/order", order);
    }

    public Response getOrderById(Long orderId) {
        return get("/store/order/{id}", orderId);
    }

    public Response deleteOrder(Long orderId) {
        return delete("/store/order/{id}", orderId);
    }

    public Map<String, Integer> getInventoryMap() {
        return getInventory()
                .then()
                .extract()
                .jsonPath()
                .getMap("$");
    }

    public Order placeOrderAndGetResponse(Order order) {
        return placeOrder(order)
                .then()
                .extract()
                .as(Order.class);
    }
}