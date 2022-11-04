package yandex;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import yandex.client.Order;
import yandex.client.OrderRequest;
import yandex.client.User;
import yandex.client.UserRequest;
import yandex.model.CreateOrderResponse;
import yandex.model.CreateUserResponse;
import yandex.model.ResponseErrorMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.*;
import static yandex.util.UtilClass.*;

@RunWith(Parameterized.class)
public class CreateOrderTest {

    public UserRequest userRequest;
    public OrderRequest orderRequest;
    public User user;
    private String accessToken = null;
    private boolean availabilityIngredients;
    private boolean ingredientsCorrectHash;

    public CreateOrderTest(boolean availabilityIngredients, boolean ingredientsCorrectHash) {
        this.availabilityIngredients = availabilityIngredients;
        this.ingredientsCorrectHash = ingredientsCorrectHash;
    }

    @Parameterized.Parameters (name = "Тестовые данные: {0}, {1}")
    public static Object[][] getOrderData() {
        return new Object[][]{
                {true, true},
                {false, false},
                {true, false},
        };
    }

    @Before
    public void setup() {
        userRequest = new UserRequest();
        orderRequest = new OrderRequest();
        user = User.createRandomUser();
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userRequest.deleteUser(accessToken.substring(7));
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    public void creatingOrderWithAuthorized() {
        Response responseCreate = userRequest.createUserResponse(user);
        accessToken = responseCreate.body().as(CreateUserResponse.class).getAccessToken();
        responseCreate.then().statusCode(SC_OK);

        Response responseAuthorization = userRequest.authorizationUserResponse(user);
        responseAuthorization.then().statusCode(SC_OK);

        List<String> ingredients = List.of();

        if (availabilityIngredients) {
            if (ingredientsCorrectHash) {
                ingredients = List.of(CRATOR_BUN, SPASE_SAUCE, MINERAL_RINGS, BIOCUTLET);
            } else {
                ingredients = List.of(INCORRECT_HASH, NULL_HASH, INCORRECT_HASH, NULL_HASH);
            }
        }

        Order order = new Order(ingredients);

        Response responseOrder = orderRequest.createOrderResponse(accessToken.substring(7), order);

        if (availabilityIngredients) {
            if (ingredientsCorrectHash) {
                responseOrder.then().statusCode(SC_OK);
                Assert.assertTrue(INCORRECT_BODY, responseOrder.body().as(CreateOrderResponse.class).isSuccess());
                Assert.assertEquals(NAME_WAS_NOT_CHANGED, user.getName(), responseOrder.body().as(CreateOrderResponse.class).getOrder().getOwner().getName());
            } else {
                responseOrder.then().statusCode(SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            responseOrder.then().statusCode(SC_BAD_REQUEST);
            ResponseErrorMessage bodyResponseErrorMessage = responseOrder.body().as(ResponseErrorMessage.class);
            Map<String, String> invalidRequestDataMap = new HashMap<>();
            invalidRequestDataMap.put(SUCCESS, FALSE);
            invalidRequestDataMap.put(MESSAGE, INGREDIENT_NOT_PROVIDED);

            Assert.assertEquals(INCORRECT_RESPONSE_BODY, invalidRequestDataMap.toString(), bodyResponseErrorMessage.toString());
        }
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void creatingOrderWithoutAuthorized(){
        List<String> ingredients = List.of();

        if (availabilityIngredients) {
            if (ingredientsCorrectHash) {
                ingredients = List.of(CRATOR_BUN, SPASE_SAUCE, MINERAL_RINGS, BIOCUTLET);
            } else {
                ingredients = List.of(INCORRECT_HASH, NULL_HASH, INCORRECT_HASH, NULL_HASH);
            }
        }

        Order order = new Order(ingredients);
        Response responseOrder = orderRequest.createOrderResponse(accessToken, order);
        if (availabilityIngredients) {
            if (ingredientsCorrectHash) {
                responseOrder.then().statusCode(SC_OK);
                Assert.assertTrue(INCORRECT_BODY, responseOrder.body().as(CreateOrderResponse.class).isSuccess());
            } else {
                responseOrder.then().statusCode(SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            responseOrder.then().statusCode(SC_BAD_REQUEST);
            ResponseErrorMessage bodyResponseErrorMessage = responseOrder.body().as(ResponseErrorMessage.class);
            Map<String, String> invalidRequestDataMap = new HashMap<>();
            invalidRequestDataMap.put(SUCCESS, FALSE);
            invalidRequestDataMap.put(MESSAGE, INGREDIENT_NOT_PROVIDED);

            Assert.assertEquals(INCORRECT_RESPONSE_BODY, invalidRequestDataMap.toString(), bodyResponseErrorMessage.toString());
        }
    }
}