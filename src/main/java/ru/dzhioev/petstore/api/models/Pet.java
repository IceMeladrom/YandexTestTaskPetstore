package ru.dzhioev.petstore.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pet {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("category")
    private Category category;

    @JsonProperty("name")
    private String name;

    @JsonProperty("photoUrls")
    private List<String> photoUrls;

    @JsonProperty("tags")
    private List<Tag> tags;

    @JsonProperty("status")
    private PetStatus status;

    @Getter
    public enum PetStatus {
        AVAILABLE("AVAILABLE"),
        PENDING("PENDING"),
        SOLD("SOLD");

        private final String value;

        PetStatus(String value) {
            this.value = value;
        }

    }
}