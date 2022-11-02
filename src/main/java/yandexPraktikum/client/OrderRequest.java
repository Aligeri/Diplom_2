package yandexPraktikum.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static yandexPraktikum.util.UtilClass.ORDERS_URL;

public class OrderRequest extends Base {

    @Step("Создать заказ")
    public Response createOrderResponse(String accessToken, Order order) {
        if (accessToken == null){
            return given()
                    .spec(getBaseSpec())
                    .body(order.inputDataMapForOrder())
                    .when()
                    .post(ORDERS_URL);
        }
        return given()
                .spec(getBaseSpec())
                .auth().oauth2(accessToken)
                .and()
                .body(order.inputDataMapForOrder())
                .when()
                .post(ORDERS_URL);
    }

    @Step("Получить заказ авторизованого пользователя")
    public Response receivingUserOrdersResponse (String accessToken) {
        return given()
                .spec(getBaseSpec())
                .auth().oauth2(accessToken)
                .when()
                .get(ORDERS_URL);
    }

    @Step("Получить заказ не авторизованого пользователя")
    public Response receivingUserOrdersResponse () {
        return given()
                .spec(getBaseSpec())
                .when()
                .get(ORDERS_URL);
    }
}