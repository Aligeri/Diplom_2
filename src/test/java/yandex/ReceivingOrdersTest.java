package yandex;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import yandex.client.Order;
import yandex.client.OrderRequest;
import yandex.client.User;
import yandex.client.UserRequest;
import yandex.model.CreateUserResponse;
import yandex.model.ResponseErrorMessage;
import yandex.model.UserOrdersResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static yandex.util.UtilClass.*;

public class ReceivingOrdersTest {

    public UserRequest userRequest;
    public OrderRequest orderRequest;
    private String accessToken;

    @Before
    public void setup() {
        userRequest = new UserRequest();
        orderRequest = new OrderRequest();
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userRequest.deleteUser(accessToken.substring(7));
        }
    }

    @Test
    @DisplayName("Получение заказа авторизованного пользователя")
    public void receivingOrderFromAuthorizedUser() {
        User user = User.createRandomUser();
        Response responseCreate = userRequest.createUserResponse(user);
        accessToken = responseCreate.body().as(CreateUserResponse.class).getAccessToken();
        responseCreate.then().statusCode(SC_OK);

        Response responseAuthorization = userRequest.authorizationUserResponse(user);
        responseAuthorization.then().statusCode(SC_OK);
        Order order = new Order(List.of(CRATOR_BUN, SPASE_SAUCE, MINERAL_RINGS, BIOCUTLET));
        Response responseOrder = orderRequest.createOrderResponse(accessToken.substring(7), order);
        responseOrder.then().statusCode(SC_OK);

        Response receivingOrderResponse = orderRequest.receivingUserOrdersResponse(accessToken.substring(7));
        receivingOrderResponse.then().statusCode(SC_OK);
        ArrayList orderUserList = receivingOrderResponse.then().extract().path(ORDERS_URL);
        Assert.assertTrue(EMPTY_ORDER, orderUserList.size() > 0);
        Assert.assertTrue(EMPTY_ORDER, receivingOrderResponse.body().as(UserOrdersResponse.class).getTotal() > 0);
    }

    @Test
    @DisplayName("Получение заказа неавторизованного пользователя")
    public void receivingOrderFromUnauthorizedUser() {
        Response response = orderRequest.receivingUserOrdersResponse();
        response.then().statusCode(SC_UNAUTHORIZED);

        Map<String, String> unauthorizedDataMap = new HashMap<>();
        unauthorizedDataMap.put(SUCCESS, FALSE);
        unauthorizedDataMap.put(MESSAGE, SHOULD_BE_AUTHORIZED);

        ResponseErrorMessage bodyResponseErrorMessage = response.body().as(ResponseErrorMessage.class);
        Assert.assertEquals(INCORRECT_RESPONSE_BODY,unauthorizedDataMap.toString(), bodyResponseErrorMessage.toString());
    }
}