package ru.dzhioev.petstore.api.clients;

import ru.dzhioev.petstore.api.models.Pet;
import ru.dzhioev.petstore.api.specs.ApiSpecification;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@Slf4j
public class PetClient extends BaseClient {

    public PetClient() {
        super(ApiSpecification.getDefaultRequestSpec());
    }

    public Response addPet(Pet pet) {
        return post("/pet", pet);
    }

    public Response updatePet(Pet pet) {
        return put("/pet", pet);
    }

    public Response findPetsByStatus(Pet.PetStatus... statuses) {
        String statusParam = String.join(",",
                java.util.Arrays.stream(statuses)
                        .map(Pet.PetStatus::getValue)
                        .toArray(String[]::new));

        return get("/pet/findByStatus", Map.of("status", statusParam));
    }

    public Response findPetsByStatus(String status) {
        return get("/pet/findByStatus", Map.of("status", status));
    }

    public Response findPetsByTags(List<String> tags) {
        String tagsParam = String.join(",", tags);
        return get("/pet/findByTags", Map.of("tags", tagsParam));
    }

    public Response getPetById(Long petId) {
        return get("/pet/{id}", petId);
    }

    public Response updatePetWithForm(Long petId, String name, String status) {
        return given()
                .spec(requestSpec)
                .contentType("application/x-www-form-urlencoded")
                .pathParam("id", petId)
                .formParam("name", name)
                .formParam("status", status)
                .when()
                .post("/pet/{id}");
    }

    public Response uploadImage(Long petId, File file, String additionalMetadata) {
        return given()
                .spec(ApiSpecification.getMultipartRequestSpec())
                .pathParam("id", petId)
                .multiPart("file", file)
                .multiPart("additionalMetadata", additionalMetadata)
                .when()
                .post("/pet/{id}/uploadImage");
    }

    public Response deletePet(Long petId) {
        return delete("/pet/{id}", petId);
    }

    public List<Pet> getPetsByStatus(Pet.PetStatus... statuses) {
        return findPetsByStatus(statuses)
                .then()
                .extract()
                .as(new TypeRef<List<Pet>>() {});
    }

    public Pet createPetAndGetResponse(Pet pet) {
        return addPet(pet)
                .then()
                .extract()
                .as(Pet.class);
    }
}