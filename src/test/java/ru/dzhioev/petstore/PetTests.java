package ru.dzhioev.petstore;

import ru.dzhioev.petstore.api.clients.PetClient;
import ru.dzhioev.petstore.api.models.Pet;
import ru.dzhioev.petstore.utils.TestDataGenerator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Tag("pet")
@Tag("regression")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Pet Tests")
class PetTests {

    private PetClient petClient;
    private static File testImageFile;

    @BeforeAll
    static void setUpClass() throws IOException {
        Path tempFile = Files.createTempFile("test-pet-", ".jpg");
        Files.write(tempFile, "fake image content".getBytes());
        testImageFile = tempFile.toFile();
        testImageFile.deleteOnExit();
    }

    @BeforeEach
    void setUp() {
        petClient = new PetClient();
    }

    @AfterAll
    static void tearDownClass() {
        if (testImageFile != null && testImageFile.exists()) {
            testImageFile.delete();
        }
    }

    @Nested
    @DisplayName("POST /pet Tests")
    class AddPetTests {

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should create pet with valid data")
            void shouldCreatePetWithValidData() {
                Pet pet = TestDataGenerator.generatePet();

                Pet createdPet = petClient.addPet(pet)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Pet.class);

                assertAll(
                        () -> assertThat(createdPet.getId()).isEqualTo(pet.getId()),
                        () -> assertThat(createdPet.getName()).isEqualTo(pet.getName()),
                        () -> assertThat(createdPet.getStatus()).isEqualTo(pet.getStatus()),
                        () -> assertThat(createdPet.getPhotoUrls()).containsExactlyElementsOf(pet.getPhotoUrls())
                );

                petClient.deletePet(pet.getId());
            }

            @Test
            @DisplayName("Should create pet with only required fields")
            void shouldCreatePetWithOnlyRequiredFields() {
                Pet minimalPet = Pet.builder()
                        .name("Required Pet")
                        .photoUrls(List.of("http://example.com/photo.jpg"))
                        .build();

                Pet createdPet = petClient.addPet(minimalPet)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Pet.class);

                assertAll(
                        () -> assertThat(createdPet.getName()).isEqualTo("Required Pet"),
                        () -> assertThat(createdPet.getPhotoUrls()).containsExactly("http://example.com/photo.jpg")
                );

                petClient.deletePet(createdPet.getId());
            }

            @ParameterizedTest
            @EnumSource(Pet.PetStatus.class)
            @DisplayName("Should create pet with status: {0}")
            void shouldCreatePetWithStatus(Pet.PetStatus status) {
                Pet pet = TestDataGenerator.generatePet();
                pet.setStatus(status);

                Pet createdPet = petClient.addPet(pet)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Pet.class);

                assertThat(createdPet.getStatus()).isEqualTo(status);

                petClient.deletePet(pet.getId());
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 405 for invalid pet input")
            void shouldReturn405ForInvalidPetInput() {
                Pet invalidPet = Pet.builder().build();

                petClient.addPet(invalidPet)
                        .then()
                        .statusCode(405);
            }

            @Test
            @DisplayName("Should return 405 for pet without name")
            void shouldReturn405ForPetWithoutName() {
                Pet invalidPet = Pet.builder()
                        .photoUrls(List.of("http://example.com/photo.jpg"))
                        .build();

                petClient.addPet(invalidPet)
                        .then()
                        .statusCode(405);
            }

            @Test
            @DisplayName("Should return 405 for pet without photoUrls")
            void shouldReturn405ForPetWithoutPhotoUrls() {
                Pet invalidPet = Pet.builder()
                        .name("No Photo Pet")
                        .build();

                petClient.addPet(invalidPet)
                        .then()
                        .statusCode(405);
            }
        }
    }

    @Nested
    @DisplayName("PUT /pet Tests")
    class UpdatePetTests {

        private Pet existingPet;

        @BeforeEach
        void setUp() {
            existingPet = TestDataGenerator.generatePet();
            existingPet = petClient.addPet(existingPet)
                    .then()
                    .extract()
                    .as(Pet.class);
        }

        @AfterEach
        void tearDown() {
            if (existingPet != null && existingPet.getId() != null) {
                petClient.deletePet(existingPet.getId());
            }
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should update existing pet")
            void shouldUpdateExistingPet() {
                existingPet.setName("Updated Name");
                existingPet.setStatus(Pet.PetStatus.SOLD);

                Pet updatedPet = petClient.updatePet(existingPet)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Pet.class);

                assertAll(
                        () -> assertThat(updatedPet.getName()).isEqualTo("Updated Name"),
                        () -> assertThat(updatedPet.getStatus()).isEqualTo(Pet.PetStatus.SOLD)
                );
            }

            @Test
            @DisplayName("Should update pet photo URLs")
            void shouldUpdatePetPhotoUrls() {
                existingPet.setPhotoUrls(List.of("new1.jpg", "new2.jpg"));

                Pet updatedPet = petClient.updatePet(existingPet)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Pet.class);

                assertThat(updatedPet.getPhotoUrls()).containsExactly("new1.jpg", "new2.jpg");
            }

            @Test
            @DisplayName("Should update pet category")
            void shouldUpdatePetCategory() {
                existingPet.getCategory().setName("Updated Category");

                Pet updatedPet = petClient.updatePet(existingPet)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Pet.class);

                assertThat(updatedPet.getCategory().getName()).isEqualTo("Updated Category");
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 400 for invalid ID")
            void shouldReturn400ForInvalidId() {
                Pet pet = TestDataGenerator.generatePet();
                pet.setId(-1L);

                petClient.updatePet(pet)
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 404 for non-existent pet")
            void shouldReturn404ForNonExistentPet() {
                if (petClient.getPetById(999999L).getStatusCode() == 200)
                    petClient.deletePet(999999L);

                Pet nonExistentPet = TestDataGenerator.generatePet();
                nonExistentPet.setId(999999L);

                petClient.updatePet(nonExistentPet)
                        .then()
                        .statusCode(404);
            }

            @Test
            @DisplayName("Should return 405 for invalid pet data")
            void shouldReturn405ForInvalidPetData() {
                existingPet.setName(null);

                petClient.updatePet(existingPet)
                        .then()
                        .statusCode(405);
            }
        }
    }

    @Nested
    @DisplayName("GET /pet/findByStatus Tests")
    class FindPetsByStatusTests {

        private Pet availablePet;
        private Pet pendingPet;

        @BeforeEach
        void setUp() {
            availablePet = TestDataGenerator.generatePetWithStatus(Pet.PetStatus.AVAILABLE);
            availablePet = petClient.addPet(availablePet)
                    .then()
                    .extract()
                    .as(Pet.class);

            pendingPet = TestDataGenerator.generatePetWithStatus(Pet.PetStatus.PENDING);
            pendingPet = petClient.addPet(pendingPet)
                    .then()
                    .extract()
                    .as(Pet.class);
        }

        @AfterEach
        void tearDown() {
            if (availablePet != null && availablePet.getId() != null) {
                petClient.deletePet(availablePet.getId());
            }
            if (pendingPet != null && pendingPet.getId() != null) {
                petClient.deletePet(pendingPet.getId());
            }
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should find pets by available status")
            void shouldFindPetsByAvailableStatus() {
                List<Pet> pets = petClient.getPetsByStatus(Pet.PetStatus.AVAILABLE);

                assertThat(pets).isNotEmpty();
                assertThat(pets).anyMatch(p -> p.getId().equals(availablePet.getId()));
            }

            @Test
            @DisplayName("Should find pets by pending status")
            void shouldFindPetsByPendingStatus() {
                List<Pet> pets = petClient.getPetsByStatus(Pet.PetStatus.PENDING);

                assertThat(pets).isNotEmpty();
                assertThat(pets).anyMatch(p -> p.getId().equals(pendingPet.getId()));
            }

            @Test
            @DisplayName("Should find pets by multiple statuses")
            void shouldFindPetsByMultipleStatuses() {
                List<Pet> pets = petClient.getPetsByStatus(Pet.PetStatus.AVAILABLE, Pet.PetStatus.PENDING);

                assertThat(pets).isNotEmpty();
                assertThat(pets).anyMatch(p -> p.getId().equals(availablePet.getId()));
                assertThat(pets).anyMatch(p -> p.getId().equals(pendingPet.getId()));
            }

            @Test
            @DisplayName("Should find pets by sold status")
            void shouldFindPetsBySoldStatus() {
                Pet soldPet = TestDataGenerator.generatePetWithStatus(Pet.PetStatus.SOLD);
                soldPet = petClient.addPet(soldPet)
                        .then()
                        .extract()
                        .as(Pet.class);

                List<Pet> pets = petClient.getPetsByStatus(Pet.PetStatus.SOLD);

                assertThat(pets).isNotEmpty();
                Pet finalSoldPet = soldPet;
                assertThat(pets).anyMatch(p -> p.getId().equals(finalSoldPet.getId()));

                petClient.deletePet(finalSoldPet.getId());
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 400 for invalid status value")
            void shouldReturn400ForInvalidStatusValue() {
                petClient.findPetsByStatus("invalid_status")
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 400 for empty status")
            void shouldReturn400ForEmptyStatus() {
                petClient.findPetsByStatus("")
                        .then()
                        .statusCode(400);
            }
        }
    }

    @Nested
    @DisplayName("GET /pet/findByTags Tests")
    @Disabled("Deprecated API method")
    class FindPetsByTagsTests {

        private Pet petWithTags;

        @BeforeEach
        void setUp() {
            petWithTags = TestDataGenerator.generatePet();
            petWithTags.getTags().get(0).setName("test-tag-1");
            petWithTags.getTags().get(1).setName("test-tag-2");
            petWithTags = petClient.addPet(petWithTags)
                    .then()
                    .extract()
                    .as(Pet.class);
        }

        @AfterEach
        void tearDown() {
            if (petWithTags != null && petWithTags.getId() != null) {
                petClient.deletePet(petWithTags.getId());
            }
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should find pets by single tag")
            void shouldFindPetsBySingleTag() {
                petClient.findPetsByTags(List.of("test-tag-1"))
                        .then()
                        .statusCode(200);
            }

            @Test
            @DisplayName("Should find pets by multiple tags")
            void shouldFindPetsByMultipleTags() {
                petClient.findPetsByTags(List.of("test-tag-1", "test-tag-2"))
                        .then()
                        .statusCode(200);
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 400 for invalid tag value")
            void shouldReturn400ForInvalidTagValue() {
                petClient.findPetsByTags(List.of(""))
                        .then()
                        .statusCode(400);
            }
        }
    }

    @Nested
    @DisplayName("GET /pet/{petId} Tests")
    class GetPetByIdTests {

        private Pet existingPet;

        @BeforeEach
        void setUp() {
            existingPet = TestDataGenerator.generatePet();
            existingPet = petClient.addPet(existingPet)
                    .then()
                    .extract()
                    .as(Pet.class);
        }

        @AfterEach
        void tearDown() {
            if (existingPet != null && existingPet.getId() != null) {
                petClient.deletePet(existingPet.getId());
            }
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should get pet by valid ID")
            void shouldGetPetByValidId() {
                Pet retrievedPet = petClient.getPetById(existingPet.getId())
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Pet.class);

                assertAll(
                        () -> assertThat(retrievedPet.getId()).isEqualTo(existingPet.getId()),
                        () -> assertThat(retrievedPet.getName()).isEqualTo(existingPet.getName()),
                        () -> assertThat(retrievedPet.getStatus()).isEqualTo(existingPet.getStatus())
                );
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 400 for invalid pet ID")
            void shouldReturn400ForInvalidPetId() {
                petClient.getPetById(-1L)
                        .then()
                        .statusCode(400);
            }

            @Test
            @DisplayName("Should return 404 for non-existent pet")
            void shouldReturn404ForNonExistentPet() {
                if (petClient.getPetById(999999L).getStatusCode() == 200)
                    petClient.deletePet(999999L);

                petClient.getPetById(999999L)
                        .then()
                        .statusCode(404);
            }
        }
    }

    @Nested
    @DisplayName("POST /pet/{petId} Tests")
    class UpdatePetWithFormTests {

        private Pet existingPet;

        @BeforeEach
        void setUp() {
            existingPet = TestDataGenerator.generatePet();
            existingPet = petClient.addPet(existingPet)
                    .then()
                    .extract()
                    .as(Pet.class);
        }

        @AfterEach
        void tearDown() {
            if (existingPet != null && existingPet.getId() != null) {
                petClient.deletePet(existingPet.getId());
            }
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should update pet name with form data")
            void shouldUpdatePetNameWithFormData() {
                petClient.updatePetWithForm(existingPet.getId(), "New Form Name", null)
                        .then()
                        .statusCode(200);

                Pet updatedPet = petClient.getPetById(existingPet.getId())
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Pet.class);

                assertThat(updatedPet.getName()).isEqualTo("New Form Name");
            }

            @Test
            @DisplayName("Should update pet status with form data")
            void shouldUpdatePetStatusWithFormData() {
                petClient.updatePetWithForm(existingPet.getId(), null, Pet.PetStatus.SOLD.name())
                        .then()
                        .statusCode(200);

                Pet updatedPet = petClient.getPetById(existingPet.getId())
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Pet.class);

                assertThat(updatedPet.getStatus()).isEqualTo(Pet.PetStatus.SOLD);
            }

            @Test
            @DisplayName("Should update both name and status with form data")
            void shouldUpdateBothNameAndStatusWithFormData() {
                petClient.updatePetWithForm(existingPet.getId(), "Form Updated", Pet.PetStatus.PENDING.name())
                        .then()
                        .statusCode(200);

                Pet updatedPet = petClient.getPetById(existingPet.getId())
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Pet.class);

                assertAll(
                        () -> assertThat(updatedPet.getName()).isEqualTo("Form Updated"),
                        () -> assertThat(updatedPet.getStatus()).isEqualTo(Pet.PetStatus.PENDING)
                );
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 405 for invalid pet ID in form update")
            void shouldReturn405ForInvalidPetIdInFormUpdate() {
                petClient.updatePetWithForm(-1L, "Name", "status")
                        .then()
                        .statusCode(405);
            }

            @Test
            @DisplayName("Should return 405 for non-existent pet in form update")
            void shouldReturn405ForNonExistentPetInFormUpdate() {
                if (petClient.getPetById(999999L).getStatusCode() == 200)
                    petClient.deletePet(999999L);

                petClient.updatePetWithForm(999999L, "Name", "status")
                        .then()
                        .statusCode(405);
            }
        }
    }

    @Nested
    @DisplayName("POST /pet/{petId}/uploadImage Tests")
    class UploadImageTests {

        private Pet existingPet;

        @BeforeEach
        void setUp() {
            existingPet = TestDataGenerator.generatePet();
            existingPet = petClient.addPet(existingPet)
                    .then()
                    .extract()
                    .as(Pet.class);
        }

        @AfterEach
        void tearDown() {
            if (existingPet != null && existingPet.getId() != null) {
                petClient.deletePet(existingPet.getId());
            }
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should upload image to pet")
            void shouldUploadImageToPet() {
                petClient.uploadImage(existingPet.getId(), testImageFile, "Test metadata")
                        .then()
                        .statusCode(200);
            }

            @Test
            @DisplayName("Should upload image with empty metadata")
            void shouldUploadImageWithEmptyMetadata() {
                petClient.uploadImage(existingPet.getId(), testImageFile, "")
                        .then()
                        .statusCode(200);
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 404 for non-existent pet in upload")
            void shouldReturn404ForNonExistentPetInUpload() {
                if (petClient.getPetById(999999L).getStatusCode() == 200)
                    petClient.deletePet(999999L);

                petClient.uploadImage(999999L, testImageFile, "metadata")
                        .then()
                        .statusCode(404);
            }
        }
    }

    @Nested
    @DisplayName("DELETE /pet/{petId} Tests")
    class DeletePetTests {

        private Pet existingPet;

        @BeforeEach
        void setUp() {
            existingPet = TestDataGenerator.generatePet();
            existingPet = petClient.addPet(existingPet)
                    .then()
                    .extract()
                    .as(Pet.class);
        }

        @Nested
        @DisplayName("Positive Test Cases")
        class PositiveTestCases {

            @Test
            @DisplayName("Should delete existing pet")
            void shouldDeleteExistingPet() {
                petClient.deletePet(existingPet.getId())
                        .then()
                        .statusCode(200);

                petClient.getPetById(existingPet.getId())
                        .then()
                        .statusCode(404);
            }

            @Test
            @DisplayName("Should return 404 when deleting already deleted pet")
            void shouldReturn404WhenDeletingAlreadyDeletedPet() {
                petClient.deletePet(existingPet.getId())
                        .then()
                        .statusCode(200);

                petClient.deletePet(existingPet.getId())
                        .then()
                        .statusCode(404);
            }
        }

        @Nested
        @DisplayName("Negative Test Cases")
        class NegativeTestCases {

            @Test
            @DisplayName("Should return 400 for invalid pet ID")
            void shouldReturn400ForInvalidPetId() {
                petClient.deletePet(-1L)
                        .then()
                        .statusCode(400);
            }
        }
    }

    @Nested
    @DisplayName("Pet Lifecycle Tests")
    class PetLifecycleTests {

        @Test
        @DisplayName("Should complete pet lifecycle")
        void shouldCompletePetLifecycle() {
            Pet pet = TestDataGenerator.generatePet();

            Pet createdPet = petClient.addPet(pet)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(Pet.class);

            assertThat(createdPet.getId()).isEqualTo(pet.getId());

            Pet retrievedPet = petClient.getPetById(pet.getId())
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(Pet.class);

            assertThat(retrievedPet.getId()).isEqualTo(pet.getId());

            pet.setName("Updated Lifecycle Name");
            pet.setStatus(Pet.PetStatus.SOLD);

            Pet updatedPet = petClient.updatePet(pet)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(Pet.class);

            assertThat(updatedPet.getName()).isEqualTo("Updated Lifecycle Name");

            petClient.deletePet(pet.getId())
                    .then()
                    .statusCode(200);

            petClient.getPetById(pet.getId())
                    .then()
                    .statusCode(404);
        }
    }
}