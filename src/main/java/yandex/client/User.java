package yandex.client;

import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class User {

    private final String email;
    private final String password;
    private final String name;

    public static User createRandomUser() {
        Faker faker = new Faker();
        final String email = faker.internet().emailAddress();
        final String password = faker.internet().password(6, 8);
        final String userName = faker.name().username();
        return new User (email, password, userName);
    }

    public Map<String, String> inputDataMapForCreateUser() {
        Map<String, String> inputDataMap = new HashMap<>();
        inputDataMap.put("email", email);
        inputDataMap.put("password", password);
        inputDataMap.put("name", name);
        return inputDataMap;
    }

    public Map<String, String> inputDataMapForAuthorization() {
        Map<String, String> inputDataMap = new HashMap<>();
        inputDataMap.put("email", email);
        inputDataMap.put("password", password);
        return inputDataMap;
    }
}