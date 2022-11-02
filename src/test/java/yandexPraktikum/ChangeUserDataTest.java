package yandexPraktikum;

import com.github.javafaker.Faker;
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

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static yandexPraktikum.util.UtilClass.*;

@RunWith(Parameterized.class)
public class ChangeUserDataTest {

    private String email;
    private String name;

    public ChangeUserDataTest(String email, String name) {
        this.email = email;
        this.name = name;
    }

    @Parameterized.Parameters (name = "Тестовые данные: {0}, {1}")
    public static Object[][] getChangingUserData() {
        Faker faker = new Faker();
        return new Object[][] {
                {null, faker.name().username()},
                {faker.internet().emailAddress(), faker.name().username()},
                {faker.internet().emailAddress(),  null},
        };
    }

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
    @DisplayName("Изменение данных пользователя с авторизацией")
    public void changeUserDataWithAuthorization() {
        User user = User.createRandomUser();
        Response responseCreate = userRequest.createUserResponse(user);
        accessToken = responseCreate.body().as(CreateUserResponse.class).getAccessToken();
        responseCreate.then().statusCode(SC_OK);

        Response responseAuthorization = userRequest.authorizationUserResponse(user);
        responseAuthorization.then().statusCode(SC_OK);

        Map<String, String> inputDataMap = new HashMap<>();
        if (email != null){
            inputDataMap.put(EMAIL, email);
        }

        if (name != null){
            inputDataMap.put(NAME, name);
        }

        Response changingUserData = userRequest.changeUserDataResponse(accessToken.substring(7), inputDataMap);
        changingUserData.then().statusCode(SC_OK);
        Assert.assertTrue(INCORRECT_BODY, changingUserData.body().as(CreateUserResponse.class).isSuccess());

        CreateUserResponse getDataUser = userRequest.getUserDataResponse(accessToken.substring(7)).body().as(CreateUserResponse.class);
        if (email != null){
            Assert.assertEquals(EMAIL_WAS_NOT_CHANGED, email, getDataUser.getUser().getEmail());
        }

        if (name != null){
            Assert.assertEquals(NAME_WAS_NOT_CHANGED, name, getDataUser.getUser().getName());
        }
    }

    @Test
    @DisplayName("Изменение данных пользователя без авторизации")
    public void changeUserDataWithoutAuthorization() {
        User user = User.createRandomUser();
        Response responseCreate = userRequest.createUserResponse(user);
        accessToken = responseCreate.body().as(CreateUserResponse.class).getAccessToken();
        responseCreate.then().statusCode(SC_OK);

        Map<String, String> inputDataMap = new HashMap<>();
        if (email != null){
            inputDataMap.put(EMAIL, email);
        }

        if (name != null){
            inputDataMap.put(NAME, name);
        }

        Response changingUserData = userRequest.changeUserDataResponse(inputDataMap);
        changingUserData.then().statusCode(SC_UNAUTHORIZED);

        Map<String, String> unauthorizedDataMap = new HashMap<>();
        unauthorizedDataMap.put(SUCCESS, FALSE);
        unauthorizedDataMap.put(MESSAGE, SHOULD_BE_AUTHORIZED);

        ResponseErrorMessage bodyResponseErrorMessage = changingUserData.body().as(ResponseErrorMessage.class);
        Assert.assertEquals(INCORRECT_RESPONSE_BODY,unauthorizedDataMap.toString(), bodyResponseErrorMessage.toString());
    }
}