package ru.dzhioev.petstore;

import ru.dzhioev.petstore.api.clients.UserClient;
import ru.dzhioev.petstore.api.models.User;
import ru.dzhioev.petstore.utils.TestDataGenerator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Tag("user")
@Tag("regression")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("User Tests")
class UserTests {

    private UserClient userClient;

    @BeforeEach
    void setUp() {
        userClient = new UserClient();
    }

    @Nested
    @DisplayName("POST /user Tests")
    class CreateUserTests {

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should create user with valid data")
            void shouldCreateUserWithValidData() {
                User user = TestDataGenerator.generateUser();
                String uniqueUsername = "testuser_" + System.currentTimeMillis();
                user.setUsername(uniqueUsername);

                userClient.createUser(user)
                        .then()
                        .statusCode(200);

                User retrievedUser = userClient.getUserByUsername(uniqueUsername)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(User.class);

                assertAll(
                        () -> assertThat(retrievedUser.getUsername()).isEqualTo(uniqueUsername),
                        () -> assertThat(retrievedUser.getEmail()).isEqualTo(user.getEmail()),
                        () -> assertThat(retrievedUser.getFirstName()).isEqualTo(user.getFirstName())
                );

                userClient.deleteUser(uniqueUsername);
            }

            @Test
            @DisplayName("Should create user with only username")
            void shouldCreateUserWithOnlyUsername() {
                String uniqueUsername = "minimal_" + System.currentTimeMillis();
                User user = User.builder()
                        .username(uniqueUsername)
                        .build();

                userClient.createUser(user)
                        .then()
                        .statusCode(200);

                userClient.deleteUser(uniqueUsername);
            }

            @Test
            @DisplayName("Should create user with all fields")
            void shouldCreateUserWithAllFields() {
                User user = TestDataGenerator.generateUser();
                String uniqueUsername = "fulluser_" + System.currentTimeMillis();
                user.setUsername(uniqueUsername);
                user.setUserStatus(1);

                userClient.createUser(user)
                        .then()
                        .statusCode(200);

                User retrievedUser = userClient.getUserByUsername(uniqueUsername)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(User.class);

                assertThat(retrievedUser.getUserStatus()).isEqualTo(1);

                userClient.deleteUser(uniqueUsername);
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 400 for invalid user data")
            void shouldReturn400ForInvalidUserData() {
                User invalidUser = User.builder()
                        .id(-1L)
                        .userStatus(-1)
                        .build();

                userClient.createUser(invalidUser)
                        .then()
                        .statusCode(400);
            }
        }
    }

    @Nested
    @DisplayName("POST /user/createWithArray Tests")
    class CreateUsersWithArrayTests {

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should create users with array")
            void shouldCreateUsersWithArray() {
                String timestamp = String.valueOf(System.currentTimeMillis());
                User user1 = TestDataGenerator.generateUser();
                user1.setUsername("arrayuser1_" + timestamp);

                User user2 = TestDataGenerator.generateUser();
                user2.setUsername("arrayuser2_" + timestamp);

                User[] users = {user1, user2};

                userClient.createUsersWithArray(users)
                        .then()
                        .statusCode(200);

                assertAll(
                        () -> userClient.getUserByUsername(user1.getUsername())
                                .then()
                                .statusCode(200),
                        () -> userClient.getUserByUsername(user2.getUsername())
                                .then()
                                .statusCode(200)
                );

                userClient.deleteUser(user1.getUsername());
                userClient.deleteUser(user2.getUsername());
            }

            @Test
            @DisplayName("Should create single user with array")
            void shouldCreateSingleUserWithArray() {
                String uniqueUsername = "singlearray_" + System.currentTimeMillis();
                User user = TestDataGenerator.generateUser();
                user.setUsername(uniqueUsername);

                User[] users = {user};

                userClient.createUsersWithArray(users)
                        .then()
                        .statusCode(200);

                userClient.deleteUser(uniqueUsername);
            }
        }
    }

    @Nested
    @DisplayName("POST /user/createWithList Tests")
    class CreateUsersWithListTests {

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should create users with list")
            void shouldCreateUsersWithList() {
                String timestamp = String.valueOf(System.currentTimeMillis());
                List<User> users = TestDataGenerator.generateUserList(3);

                for (int i = 0; i < users.size(); i++) {
                    users.get(i).setUsername("listuser" + i + "_" + timestamp);
                }

                userClient.createUsersWithList(users)
                        .then()
                        .statusCode(200);

                users.forEach(user ->
                        userClient.getUserByUsername(user.getUsername())
                                .then()
                                .statusCode(200)
                );

                users.forEach(user -> userClient.deleteUser(user.getUsername()));
            }
        }
    }

    @Nested
    @DisplayName("GET /user/{username} Tests")
    class GetUserByUsernameTests {

        private User existingUser;
        private String uniqueUsername;

        @BeforeEach
        void setUp() {
            uniqueUsername = "testuser_" + System.currentTimeMillis();
            existingUser = TestDataGenerator.generateUser();
            existingUser.setUsername(uniqueUsername);

            userClient.createUser(existingUser)
                    .then()
                    .statusCode(200);
        }

        @AfterEach
        void tearDown() {
            userClient.deleteUser(uniqueUsername);
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should get user by username")
            void shouldGetUserByUsername() {
                User retrievedUser = userClient.getUserByUsername(uniqueUsername)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(User.class);

                assertAll(
                        () -> assertThat(retrievedUser.getUsername()).isEqualTo(uniqueUsername),
                        () -> assertThat(retrievedUser.getEmail()).isEqualTo(existingUser.getEmail()),
                        () -> assertThat(retrievedUser.getFirstName()).isEqualTo(existingUser.getFirstName())
                );
            }

            @Test
            @DisplayName("Should get user with special characters in username")
            void shouldGetUserWithSpecialCharactersInUsername() {
                String specialUsername = "user.name-123_test_" + System.currentTimeMillis();
                User specialUser = TestDataGenerator.generateUser();
                specialUser.setUsername(specialUsername);

                userClient.createUser(specialUser)
                        .then()
                        .statusCode(200);

                userClient.getUserByUsername(specialUsername)
                        .then()
                        .statusCode(200);

                userClient.deleteUser(specialUsername);
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 404 for non-existent user")
            void shouldReturn404ForNonExistentUser() {
                String nonExistentUsername = "nonexistent_" + UUID.randomUUID();
                userClient.getUserByUsername(nonExistentUsername)
                        .then()
                        .statusCode(404);
            }

            @Test
            @DisplayName("Should return 400 for empty username")
            void shouldReturn400ForEmptyUsername() {
                userClient.getUserByUsername("")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 404 for deleted user")
            void shouldReturn404ForDeletedUser() {
                String username = "tobedeleted_" + System.currentTimeMillis();
                User user = TestDataGenerator.generateUser();
                user.setUsername(username);

                userClient.createUser(user)
                        .then()
                        .statusCode(200);

                userClient.deleteUser(username)
                        .then()
                        .statusCode(200);

                userClient.getUserByUsername(username)
                        .then()
                        .statusCode(404);
            }
        }
    }

    @Nested
    @DisplayName("PUT /user/{username} Tests")
    class UpdateUserTests {

        private User existingUser;
        private String uniqueUsername;

        @BeforeEach
        void setUp() {
            uniqueUsername = "updateuser_" + System.currentTimeMillis();
            existingUser = TestDataGenerator.generateUser();
            existingUser.setUsername(uniqueUsername);

            userClient.createUser(existingUser)
                    .then()
                    .statusCode(200);
        }

        @AfterEach
        void tearDown() {
            userClient.deleteUser(uniqueUsername);
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should update existing user")
            void shouldUpdateExistingUser() {
                existingUser.setEmail("updated.email@test.com");
                existingUser.setFirstName("UpdatedFirstName");
                existingUser.setLastName("UpdatedLastName");

                userClient.updateUser(uniqueUsername, existingUser)
                        .then()
                        .statusCode(200);

                User updatedUser = userClient.getUserByUsername(uniqueUsername)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(User.class);

                assertAll(
                        () -> assertThat(updatedUser.getEmail()).isEqualTo("updated.email@test.com"),
                        () -> assertThat(updatedUser.getFirstName()).isEqualTo("UpdatedFirstName"),
                        () -> assertThat(updatedUser.getLastName()).isEqualTo("UpdatedLastName")
                );
            }

            @Test
            @DisplayName("Should update user password")
            void shouldUpdateUserPassword() {
                String newPassword = "NewPassword123";
                existingUser.setPassword(newPassword);

                userClient.updateUser(uniqueUsername, existingUser)
                        .then()
                        .statusCode(200);
            }

            @Test
            @DisplayName("Should update user status")
            void shouldUpdateUserStatus() {
                existingUser.setUserStatus(2);

                userClient.updateUser(uniqueUsername, existingUser)
                        .then()
                        .statusCode(200);

                User updatedUser = userClient.getUserByUsername(uniqueUsername)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(User.class);

                assertThat(updatedUser.getUserStatus()).isEqualTo(2);
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 404 for non-existent user")
            void shouldReturn404ForNonExistentUser() {
                String nonExistentUsername = "nonexistent_" + UUID.randomUUID();
                User user = TestDataGenerator.generateUser();

                userClient.updateUser(nonExistentUsername, user)
                        .then()
                        .statusCode(404);
            }

            @Test
            @DisplayName("Should return 400 for invalid user data")
            void shouldReturn400ForInvalidUserData() {
                existingUser.setId(-1L);

                userClient.updateUser(uniqueUsername, existingUser)
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 when updating with mismatched username")
            void shouldReturn400WhenUpdatingWithMismatchedUsername() {
                String anotherUsername = "anotheruser_" + System.currentTimeMillis();
                User anotherUser = TestDataGenerator.generateUser();
                anotherUser.setUsername(anotherUsername);

                userClient.createUser(anotherUser)
                        .then()
                        .statusCode(200);

                anotherUser.setEmail("different@email.com");

                userClient.updateUser(uniqueUsername, anotherUser)
                        .then()
                        .statusCode(400);

                userClient.deleteUser(anotherUsername);
            }
        }
    }

    @Nested
    @DisplayName("DELETE /user/{username} Tests")
    class DeleteUserTests {

        private String uniqueUsername;

        @BeforeEach
        void setUp() {
            uniqueUsername = "deleteuser_" + System.currentTimeMillis();
            User existingUser = TestDataGenerator.generateUser();
            existingUser.setUsername(uniqueUsername);

            userClient.createUser(existingUser)
                    .then()
                    .statusCode(200);
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should delete existing user")
            void shouldDeleteExistingUser() {
                userClient.deleteUser(uniqueUsername)
                        .then()
                        .statusCode(200);

                userClient.getUserByUsername(uniqueUsername)
                        .then()
                        .statusCode(404);
            }

            @Test
            @DisplayName("Should return 404 when deleting already deleted user")
            void shouldReturn404WhenDeletingAlreadyDeletedUser() {
                userClient.deleteUser(uniqueUsername)
                        .then()
                        .statusCode(200);

                userClient.deleteUser(uniqueUsername)
                        .then()
                        .statusCode(404);
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 400 for empty username")
            void shouldReturn400ForEmptyUsername() {
                userClient.deleteUser("")
                        .then()
                        .statusCode(400);
            }
        }
    }

    @Nested
    @DisplayName("GET /user/login Tests")
    class LoginUserTests {

        private String uniqueUsername;

        @BeforeEach
        void setUp() {
            uniqueUsername = "loginuser_" + System.currentTimeMillis();
            User testUser = User.builder()
                    .username(uniqueUsername)
                    .password("testpassword123")
                    .email("login@test.com")
                    .build();

            userClient.createUser(testUser)
                    .then()
                    .statusCode(200);
        }

        @AfterEach
        void tearDown() {
            userClient.deleteUser(uniqueUsername);
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should login with valid credentials")
            void shouldLoginWithValidCredentials() {
                userClient.loginUser(uniqueUsername, "testpassword123")
                        .then()
                        .statusCode(200);
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 400 for wrong password")
            void shouldReturn400ForWrongPassword() {
                userClient.loginUser(uniqueUsername, "wrongpassword")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 for non-existent user")
            void shouldReturn400ForNonExistentUser() {
                String nonExistentUsername = "nonexistent_" + UUID.randomUUID();
                userClient.loginUser(nonExistentUsername, "password")
                        .then()
                        .statusCode(400);
            }

            @ParameterizedTest
            @CsvSource({
                    "'', password",
                    "username, ''",
                    "'', ''"
            })
            @DisplayName("Should return 400 for empty credentials")
            void shouldReturn400ForEmptyCredentials(String username, String password) {
                userClient.loginUser(username, password)
                        .then()
                        .statusCode(400);
            }
        }
    }

    @Nested
    @DisplayName("GET /user/logout Tests")
    class LogoutUserTests {

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should logout successfully")
            void shouldLogoutSuccessfully() {
                userClient.logoutUser()
                        .then()
                        .statusCode(200);
            }
        }
    }

    @Nested
    @DisplayName("User Lifecycle Tests")
    class UserLifecycleTests {

        @Test
        @DisplayName("Should complete user lifecycle")
        void shouldCompleteUserLifecycle() {
            String uniqueUsername = "lifecycle_" + System.currentTimeMillis();
            User user = TestDataGenerator.generateUser();
            user.setUsername(uniqueUsername);

            userClient.createUser(user)
                    .then()
                    .statusCode(200);

            User retrievedUser = userClient.getUserByUsername(uniqueUsername)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(User.class);

            assertThat(retrievedUser.getUsername()).isEqualTo(uniqueUsername);

            user.setEmail("updated@email.com");
            user.setPhone("+1234567890");

            userClient.updateUser(uniqueUsername, user)
                    .then()
                    .statusCode(200);

            User updatedUser = userClient.getUserByUsername(uniqueUsername)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(User.class);

            assertThat(updatedUser.getEmail()).isEqualTo("updated@email.com");

            userClient.deleteUser(uniqueUsername)
                    .then()
                    .statusCode(200);

            userClient.getUserByUsername(uniqueUsername)
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("Should handle multiple user operations")
        void shouldHandleMultipleUserOperations() {
            String timestamp = String.valueOf(System.currentTimeMillis());
            List<User> users = TestDataGenerator.generateUserList(2);

            for (int i = 0; i < users.size(); i++) {
                users.get(i).setUsername("multiuser" + i + "_" + timestamp);
            }

            userClient.createUsersWithList(users)
                    .then()
                    .statusCode(200);

            users.forEach(user -> {
                userClient.getUserByUsername(user.getUsername())
                        .then()
                        .statusCode(200);
            });

            users.forEach(user -> userClient.deleteUser(user.getUsername()));
        }

        @Test
        @DisplayName("Should login, perform operations, and logout")
        void shouldLoginPerformOperationsAndLogout() {
            String uniqueUsername = "opsuser_" + System.currentTimeMillis();
            User user = User.builder()
                    .username(uniqueUsername)
                    .password("opspassword")
                    .email("ops@test.com")
                    .build();

            userClient.createUser(user)
                    .then()
                    .statusCode(200);

            userClient.loginUser(uniqueUsername, "opspassword")
                    .then()
                    .statusCode(200);

            user.setFirstName("Operations");
            user.setLastName("User");

            userClient.updateUser(uniqueUsername, user)
                    .then()
                    .statusCode(200);

            userClient.logoutUser()
                    .then()
                    .statusCode(200);

            userClient.deleteUser(uniqueUsername);
        }
    }
}