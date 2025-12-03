package ru.dzhioev.petstore.utils;

import lombok.experimental.UtilityClass;
import net.datafaker.Faker;
import ru.dzhioev.petstore.api.models.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class TestDataGenerator {

    private static final Faker faker = new Faker(Locale.ENGLISH);

    private static final long MAX_32_BIT = Integer.MAX_VALUE;

    public static long generateValidId() {
        return ThreadLocalRandom.current().nextLong(1, MAX_32_BIT);
    }

    public static Pet generatePet() {
        return Pet.builder()
                .id(generateValidId())
                .category(generateCategory())
                .name(faker.funnyName().name())
                .photoUrls(Collections.singletonList(faker.internet().image()))
                .tags(Arrays.asList(
                        Tag.builder().id(1L).name("tag1").build(),
                        Tag.builder().id(2L).name("tag2").build()
                ))
                .status(Pet.PetStatus.AVAILABLE)
                .build();
    }

    public static Pet generatePetWithId(Long id) {
        Pet pet = generatePet();
        pet.setId(id);
        return pet;
    }

    public static Pet generatePetWithoutId() {
        Pet pet = generatePet();
        pet.setId(null);
        return pet;
    }

    public static Pet generatePetWithStatus(Pet.PetStatus status) {
        Pet pet = generatePet();
        pet.setStatus(status);
        return pet;
    }

    public static Category generateCategory() {
        return Category.builder()
                .id(generateValidId())
                .name(faker.animal().name())
                .build();
    }

    public static Order generateOrder(Long petId) {
        return Order.builder()
                .id(generateValidId())
                .petId(petId)
                .quantity(faker.number().numberBetween(1, 10))
                .shipDate(LocalDateTime.now())
                .status(Order.OrderStatus.PLACED)
                .complete(false)
                .build();
    }

    public static User generateUser() {
        return User.builder()
                .id(generateValidId())
                .username(faker.name().name())
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(faker.word().adjective())
                .phone(faker.phoneNumber().phoneNumber())
                .userStatus(faker.number().numberBetween(0, 2))
                .build();
    }

    public static List<User> generateUserList(int count) {
        return faker.collection(TestDataGenerator::generateUser)
                .len(count)
                .generate();
    }
}