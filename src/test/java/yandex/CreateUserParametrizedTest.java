package yandex;

import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import yandex.client.User;
import yandex.client.UserRequest;
import yandex.model.CreateUserResponse;
import yandex.model.ResponseErrorMessage;

import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static yandex.util.UtilClass.*;

@RunWith(Parameterized.class)
public class CreateUserParametrizedTest {

    private String email;
    private String password;
    private String name;
    private String accessToken = null;

    public CreateUserParametrizedTest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Parameterized.Parameters(name = "Тестовые данные: {0}, {1}, {2}")
    public static Object[][] getUserData() {
        Faker faker = new Faker();
        return new Object[][]{
                {null, faker.internet().password(6, 8), faker.name().username()},
                {faker.internet().emailAddress(), null, faker.name().username()},
                {faker.internet().emailAddress(), faker.internet().password(6, 8), null},
        };
    }

    public UserRequest userRequest;

    @Before
    public void setup() {
        userRequest = new UserRequest();
    }

    @Test
    @DisplayName("Создание пользователя и не заполнить одно из обязательных полей")
    public void createUserEmptyFields() {
        User user = new User(email, password, name);

        Response response = userRequest.createUserResponse(user);

        var statusCode = response.getStatusCode();

        if (statusCode == SC_FORBIDDEN) {

            response.then()
                    .statusCode(SC_FORBIDDEN);

            ResponseErrorMessage bodyResponseErrorMessage = response.body().as(ResponseErrorMessage.class);

            Map<String, String> forbiddenRegisteredDataMap = new HashMap<>();
            forbiddenRegisteredDataMap.put(SUCCESS, FALSE);
            forbiddenRegisteredDataMap.put(MESSAGE, EMPTY_REQUIRED_FIELD);

            Assert.assertEquals(INCORRECT_RESPONSE_BODY, forbiddenRegisteredDataMap.toString(), bodyResponseErrorMessage.toString());
        }
        else if (statusCode == SC_OK){
            try {
                accessToken = response.body().as(CreateUserResponse.class).getAccessToken();
                userRequest.deleteUser(accessToken.substring(7));
            } catch (IllegalArgumentException e) {
                System.out.println(DELETE_USER_ERROR);
            }
        }
        else {
            System.out.println(ERROR);
        }
    }
}