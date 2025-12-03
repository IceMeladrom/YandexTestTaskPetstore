package ru.dzhioev.petstore;

import ru.dzhioev.petstore.api.clients.PetClient;
import ru.dzhioev.petstore.api.clients.StoreClient;
import ru.dzhioev.petstore.api.models.Order;
import ru.dzhioev.petstore.api.models.Pet;
import ru.dzhioev.petstore.utils.TestDataGenerator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Tag("store")
@Tag("regression")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Store Tests")
class StoreTests {

    private StoreClient storeClient;
    private PetClient petClient;

    @BeforeEach
    void setUp() {
        storeClient = new StoreClient();
        petClient = new PetClient();
    }

    @Nested
    @DisplayName("GET /store/inventory Tests")
    class GetInventoryTests {

        @Test
        @DisplayName("Should return inventory map with 200 status")
        void shouldReturnInventoryMapWith200Status() {
            storeClient.getInventory()
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Should return non-null inventory map")
        void shouldReturnNonNullInventoryMap() {
            Map<String, Integer> inventory = storeClient.getInventoryMap();
            assertThat(inventory).isNotNull();
        }
    }

    @Nested
    @DisplayName("POST /store/order Tests")
    class PlaceOrderTests {

        private Pet testPet;
        private Order testOrder;

        @BeforeEach
        void setUp() {
            testPet = TestDataGenerator.generatePet();
            testPet = petClient.createPetAndGetResponse(testPet);

            testOrder = Order.builder()
                    .id(TestDataGenerator.generateValidId())
                    .petId(testPet.getId())
                    .quantity(1)
                    .complete(false)
                    .build();
        }

        @AfterEach
        void tearDown() {
            if (testOrder != null && testOrder.getId() != null) {
                storeClient.deleteOrder(testOrder.getId());
            }
            if (testPet != null && testPet.getId() != null) {
                petClient.deletePet(testPet.getId());
            }
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should create order with valid data")
            void shouldCreateOrderWithValidData() {
                Order response = storeClient.placeOrder(testOrder)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Order.class);

                assertAll(
                        () -> assertThat(response.getId()).isEqualTo(testOrder.getId()),
                        () -> assertThat(response.getPetId()).isEqualTo(testOrder.getPetId()),
                        () -> assertThat(response.getQuantity()).isEqualTo(testOrder.getQuantity()),
                        () -> assertThat(response.getComplete()).isEqualTo(testOrder.getComplete())
                );
            }

            @Test
            @DisplayName("Should create order with complete true")
            void shouldCreateOrderWithCompleteTrue() {
                testOrder.setComplete(true);

                Order response = storeClient.placeOrder(testOrder)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Order.class);

                assertThat(response.getComplete()).isTrue();
            }

            @Test
            @DisplayName("Should create order with quantity greater than 1")
            void shouldCreateOrderWithQuantityGreaterThanOne() {
                testOrder.setQuantity(5);

                Order response = storeClient.placeOrder(testOrder)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Order.class);

                assertThat(response.getQuantity()).isEqualTo(5);
            }

            @ParameterizedTest
            @EnumSource(Order.OrderStatus.class)
            @DisplayName("Should create order with status: {0}")
            void shouldCreateOrderWithStatus(Order.OrderStatus status) {
                testOrder.setStatus(status);

                Order response = storeClient.placeOrder(testOrder)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Order.class);

                assertThat(response.getStatus()).isEqualTo(status);
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 400 for order without required fields")
            void shouldReturn400ForOrderWithoutRequiredFields() {
                Order invalidOrder = Order.builder().build();

                storeClient.placeOrder(invalidOrder)
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 for order with negative ID")
            void shouldReturn400ForOrderWithNegativeId() {
                Order order = Order.builder()
                        .id(-1L)
                        .petId(1L)
                        .quantity(1)
                        .build();

                storeClient.placeOrder(order)
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 for order with negative pet ID")
            void shouldReturn400ForOrderWithNegativePetId() {
                Order order = Order.builder()
                        .id(1L)
                        .petId(-1L)
                        .quantity(1)
                        .build();

                storeClient.placeOrder(order)
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 for order with negative quantity")
            void shouldReturn400ForOrderWithNegativeQuantity() {
                Order order = Order.builder()
                        .id(1L)
                        .petId(1L)
                        .quantity(-1)
                        .build();

                storeClient.placeOrder(order)
                        .then()
                        .statusCode(400);
            }
        }
    }

    @Nested
    @DisplayName("GET /store/order/{orderId} Tests")
    class GetOrderByIdTests {

        private Pet testPet;
        private Order testOrder;

        @BeforeEach
        void setUp() {
            testPet = TestDataGenerator.generatePet();
            testPet = petClient.createPetAndGetResponse(testPet);

            testOrder = Order.builder()
                    .id(TestDataGenerator.generateValidId())
                    .petId(testPet.getId())
                    .quantity(1)
                    .complete(false)
                    .build();

            testOrder = storeClient.placeOrderAndGetResponse(testOrder);
        }

        @AfterEach
        void tearDown() {
            if (testOrder != null && testOrder.getId() != null) {
                storeClient.deleteOrder(testOrder.getId());
            }
            if (testPet != null && testPet.getId() != null) {
                petClient.deletePet(testPet.getId());
            }
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should retrieve created order by ID")
            void shouldRetrieveCreatedOrderById() {
                Order retrievedOrder = storeClient.getOrderById(testOrder.getId())
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Order.class);

                assertAll(
                        () -> assertThat(retrievedOrder.getId()).isEqualTo(testOrder.getId()),
                        () -> assertThat(retrievedOrder.getPetId()).isEqualTo(testOrder.getPetId()),
                        () -> assertThat(retrievedOrder.getQuantity()).isEqualTo(testOrder.getQuantity())
                );
            }

            @ParameterizedTest
            @ValueSource(longs = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
            @DisplayName("Should get existing order with ID in range 1-10: {0}")
            void shouldGetExistingOrderWithIdInRange1to10(long orderId) {
                Pet pet = TestDataGenerator.generatePet();
                pet = petClient.createPetAndGetResponse(pet);

                Order order = Order.builder()
                        .id(orderId)
                        .petId(pet.getId())
                        .quantity(1)
                        .complete(false)
                        .build();

                try {
                    storeClient.placeOrder(order)
                            .then()
                            .statusCode(200);

                    storeClient.getOrderById(orderId)
                            .then()
                            .statusCode(200);

                    storeClient.deleteOrder(orderId);
                } finally {
                    petClient.deletePet(pet.getId());
                }
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 404 for non-existent order")
            void shouldReturn404ForNonExistentOrder() {
                storeClient.getOrderById(999999L)
                        .then()
                        .statusCode(404);
            }

            @Test
            @DisplayName("Should return 400 for order ID 0")
            void shouldReturn400ForOrderId0() {
                storeClient.getOrderById(0L)
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 for negative order ID")
            void shouldReturn400ForNegativeOrderId() {
                storeClient.getOrderById(-1L)
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 for order ID greater than 10")
            void shouldReturn400ForOrderIdGreaterThan10() {
                storeClient.getOrderById(11L)
                        .then()
                        .statusCode(400);
            }
        }
    }

    @Nested
    @DisplayName("DELETE /store/order/{orderId} Tests")
    class DeleteOrderTests {

        private Pet testPet;
        private Order testOrder;

        @BeforeEach
        void setUp() {
            testPet = TestDataGenerator.generatePet();
            testPet = petClient.createPetAndGetResponse(testPet);

            testOrder = Order.builder()
                    .id(TestDataGenerator.generateValidId())
                    .petId(testPet.getId())
                    .quantity(1)
                    .build();

            testOrder = storeClient.placeOrderAndGetResponse(testOrder);
        }

        @AfterEach
        void tearDown() {
            if (testPet != null && testPet.getId() != null) {
                petClient.deletePet(testPet.getId());
            }
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should delete existing order")
            void shouldDeleteExistingOrder() {
                storeClient.deleteOrder(testOrder.getId())
                        .then()
                        .statusCode(200);

                storeClient.getOrderById(testOrder.getId())
                        .then()
                        .statusCode(404);
            }

            @Test
            @DisplayName("Should delete order with ID 1")
            void shouldDeleteOrderWithId1() {
                Pet pet = TestDataGenerator.generatePet();
                pet = petClient.createPetAndGetResponse(pet);

                Order order = Order.builder()
                        .id(1L)
                        .petId(pet.getId())
                        .quantity(1)
                        .build();

                try {
                    order = storeClient.placeOrderAndGetResponse(order);

                    storeClient.deleteOrder(1L)
                            .then()
                            .statusCode(200);
                } finally {
                    petClient.deletePet(pet.getId());
                }
            }

            @Test
            @DisplayName("Should return 200 when deleting already deleted order")
            void shouldReturn200WhenDeletingAlreadyDeletedOrder() {
                storeClient.deleteOrder(testOrder.getId())
                        .then()
                        .statusCode(200);

                storeClient.deleteOrder(testOrder.getId())
                        .then()
                        .statusCode(200);
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 400 for order ID 0")
            void shouldReturn400ForOrderId0() {
                storeClient.deleteOrder(0L)
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 for negative order ID")
            void shouldReturn400ForNegativeOrderId() {
                storeClient.deleteOrder(-1L)
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 404 for non-existent order")
            void shouldReturn404ForNonExistentOrder() {
                storeClient.deleteOrder(999999L)
                        .then()
                        .statusCode(404);
            }
        }
    }

    @Nested
    @DisplayName("Order Lifecycle Tests")
    class OrderLifecycleTests {

        private Pet testPet;
        private Order testOrder;

        @BeforeEach
        void setUp() {
            testPet = TestDataGenerator.generatePet();
            testPet = petClient.createPetAndGetResponse(testPet);

            testOrder = Order.builder()
                    .id(TestDataGenerator.generateValidId())
                    .petId(testPet.getId())
                    .quantity(2)
                    .complete(false)
                    .build();
        }

        @AfterEach
        void tearDown() {
            if (testPet != null && testPet.getId() != null) {
                petClient.deletePet(testPet.getId());
            }
        }

        @Test
        @DisplayName("Should complete order lifecycle")
        void shouldCompleteOrderLifecycle() {
            Order createdOrder = storeClient.placeOrder(testOrder)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(Order.class);

            assertThat(createdOrder.getId()).isEqualTo(testOrder.getId());

            Order retrievedOrder = storeClient.getOrderById(testOrder.getId())
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(Order.class);

            assertThat(retrievedOrder.getId()).isEqualTo(testOrder.getId());

            storeClient.deleteOrder(testOrder.getId())
                    .then()
                    .statusCode(200);

            storeClient.getOrderById(testOrder.getId())
                    .then()
                    .statusCode(404);
        }

        @ParameterizedTest
        @EnumSource(Order.OrderStatus.class)
        @DisplayName("Should handle order with status: {0}")
        void shouldHandleOrderWithStatus(Order.OrderStatus status) {
            testOrder.setStatus(status);

            Order response = storeClient.placeOrder(testOrder)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(Order.class);

            assertThat(response.getStatus()).isEqualTo(status);

            storeClient.deleteOrder(testOrder.getId());
        }
    }
}