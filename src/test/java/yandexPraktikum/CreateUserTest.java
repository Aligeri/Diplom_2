package yandexPraktikum;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import yandexPraktikum.client.User;
import yandexPraktikum.client.UserRequest;
import yandexPraktikum.model.CreateUserResponse;
import yandexPraktikum.model.ResponseErrorMessage;

import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static yandexPraktikum.util.UtilClass.*;

@RunWith(Parameterized.class)
public class CreateUserTest {

    public UserRequest userRequest;
    private String accessToken;

    @Before
    public void setup() {
        userRequest = new UserRequest();
    }

    @After
    public void tearDown() {
        if(accessToken != null) {
            userRequest.deleteUser(accessToken.substring(7));
        }
    }

    @Test
    @DisplayName("Успешное создание пользователя")
    public void successfulCreateUser() {
        User user = User.createRandomUser();
        Response response = userRequest.createUserResponse(user);
        accessToken = response.body().as(CreateUserResponse.class).getAccessToken();
        response.then()
                .statusCode(SC_OK);
        Assert.assertTrue(INCORRECT_BODY, response.body().as(CreateUserResponse.class).isSuccess());
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void createUserAlreadyRegistered() {
        User user = User.createRandomUser();

        Response response = userRequest.createUserResponse(user);

        accessToken = response.body().as(CreateUserResponse.class).getAccessToken();

        Response responseSecondRegistrationUser = userRequest.createUserResponse(user);
        responseSecondRegistrationUser.then()
                .statusCode(SC_FORBIDDEN);

        ResponseErrorMessage bodyResponseErrorMessage = responseSecondRegistrationUser.body().as(ResponseErrorMessage.class);

        Map<String, String> forbiddenRegisteredDataMap = new HashMap<>();
        forbiddenRegisteredDataMap.put(SUCCESS, FALSE);
        forbiddenRegisteredDataMap.put(MESSAGE, USER_EXIST);

        Assert.assertEquals(INCORRECT_BODY,forbiddenRegisteredDataMap.toString(), bodyResponseErrorMessage.toString());
    }
}