package yandex.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static yandex.util.UtilClass.*;


public class UserRequest extends Base {

    @Step("Создать пользователя")
    public Response createUserResponse (User user) {
        return given()
                .spec(getBaseSpec())
                .body(user.inputDataMapForCreateUser())
                .when()
                .post(AUTH_REGISTER_URL);
    }

    @Step("Авторизовать пользователя")
    public Response authorizationUserResponse(User user) {
        return given()
                .spec(getBaseSpec())
                .body(user.inputDataMapForAuthorization())
                .when()
                .post(AUTH_LOGIN_URL);
    }

    @Step("Авторизовать пользователя")
    public Response authorizationUserResponse(Map<String, String> inputData) {
        return given()
                .spec(getBaseSpec())
                .body(inputData)
                .when()
                .post(AUTH_LOGIN_URL);
    }

    @Step("Получить данные пользователя")
    public Response getUserDataResponse(String accessToken) {
        return given()
                .spec(getBaseSpec())
                .auth().oauth2(accessToken)
                .when()
                .get(AUTH_USER_URL);
    }

    @Step("Измененить данные пользователя")
    public Response changeUserDataResponse(String accessToken, Map<String, String> inputData) {
        return given()
                .spec(getBaseSpec())
                .auth().oauth2(accessToken)
                .and()
                .body(inputData)
                .when()
                .patch(AUTH_USER_URL);
    }

    @Step("Измененить данные пользователя")
    public Response changeUserDataResponse(Map<String, String> inputData) {
        return given()
                .spec(getBaseSpec())
                .body(inputData)
                .when()
                .patch(AUTH_USER_URL);
    }

    @Step("Удалить пользователя")
    public void deleteUser(String accessToken) {
        given()
                .spec(getBaseSpec())
                .auth().oauth2(accessToken)
                .when()
                .delete(AUTH_USER_URL)
                .then()
                .statusCode(SC_ACCEPTED);
    }
}