package ru.dzhioev.petstore.api.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("petId")
    private Long petId;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("shipDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime shipDate;

    @JsonProperty("status")
    private OrderStatus status;

    @JsonProperty("complete")
    private Boolean complete;

    @Getter
    public enum OrderStatus {
        PLACED("placed"),
        APPROVED("approved"),
        DELIVERED("delivered");

        private final String value;

        OrderStatus(String value) {
            this.value = value;
        }

    }
}