package yandexPraktikum;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import yandexPraktikum.client.User;
import yandexPraktikum.client.UserRequest;
import yandexPraktikum.model.CreateUserResponse;
import yandexPraktikum.model.ResponseErrorMessage;

import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static yandexPraktikum.util.UtilClass.*;

public class AuthorizationUserTest {

    public UserRequest userRequest;
    public User user;
    private String accessToken;

    @Before
    public void setup() {
        userRequest = new UserRequest();
        user = User.createRandomUser();
    }

    @After
    public void tearDown() {
        if(accessToken != null) {
            userRequest.deleteUser(accessToken.substring(7));
        }
    }

    @Test
    @DisplayName("Логин под существующим пользователем")
    public void successUserAuthorization() {
        Response responseCreate = userRequest.createUserResponse(user);
        accessToken = responseCreate.body().as(CreateUserResponse.class).getAccessToken();
        responseCreate.then().statusCode(SC_OK);

        Response responseAuthorization = userRequest.authorizationUserResponse(user);
        responseAuthorization.then().statusCode(SC_OK);
        Assert.assertTrue(INCORRECT_BODY, responseAuthorization.body().as(CreateUserResponse.class).isSuccess());
    }

    @Test
    @DisplayName("Логин с неверным логином")
    public void userAuthorizationWithWrongLogin() {
        Response responseAuthorization = userRequest.authorizationUserResponse(user);
        responseAuthorization.then().statusCode(SC_UNAUTHORIZED);
        ResponseErrorMessage bodyResponseErrorMessage = responseAuthorization.body().as(ResponseErrorMessage.class);

        Map<String, String> authorizationInvalidLoginDataMap = new HashMap<>();
        authorizationInvalidLoginDataMap.put(SUCCESS, FALSE);
        authorizationInvalidLoginDataMap.put(MESSAGE, EMAIL_PASS_INCORRECT);

        Assert.assertEquals(INCORRECT_RESPONSE_BODY,authorizationInvalidLoginDataMap.toString(), bodyResponseErrorMessage.toString());
    }

    @Test
    @DisplayName("Логин с неверным паролем")
    public void userAuthorizationWithWrongPassword() {
        Response responseCreate = userRequest.createUserResponse(user);
        accessToken = responseCreate.body().as(CreateUserResponse.class).getAccessToken();
        responseCreate.then().statusCode(SC_OK);

        Map<String, String> inputDataMap = new HashMap<>();
        inputDataMap.put(EMAIL, user.email);
        inputDataMap.put(PASSWORD, NOT_CORRECT);

        Response responseAuthorization = userRequest.authorizationUserResponse(inputDataMap);
        responseAuthorization.then().statusCode(SC_UNAUTHORIZED);
        ResponseErrorMessage bodyResponseErrorMessage = responseAuthorization.body().as(ResponseErrorMessage.class);

        Map<String, String> authorizationInvalidPasswordDataMap = new HashMap<>();
        authorizationInvalidPasswordDataMap.put(SUCCESS, FALSE);
        authorizationInvalidPasswordDataMap.put(MESSAGE, EMAIL_PASS_INCORRECT);

        Assert.assertEquals(INCORRECT_RESPONSE_BODY,authorizationInvalidPasswordDataMap.toString(), bodyResponseErrorMessage.toString());
    }
}