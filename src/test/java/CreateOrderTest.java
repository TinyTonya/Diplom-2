import client.StellarBurgerClient;
import com.github.javafaker.Faker;
import config.TestConfig;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.Order;
import model.User;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;

public class CreateOrderTest {
    private StellarBurgerClient client;
    private User user;
    private Faker faker;
    private String accessToken;
    private String ingredientId1;
    private String ingredientId2;
    private String ingredientId3;

    @Before
    public void before() {
        faker = new Faker();
        client = new StellarBurgerClient(TestConfig.BASE_URI);
        user = new User(faker.internet().emailAddress(), faker.internet().password(), faker.name().fullName());
        ValidatableResponse response = client.registerUser(user);
        Assume.assumeTrue(response.extract().statusCode() == 200);
        accessToken = response.extract().path("accessToken");

        response = client.getIngredients();
        ingredientId1 = response.extract().path("data[0]._id");
        ingredientId2 = response.extract().path("data[1]._id");
        ingredientId3 = response.extract().path("data[2]._id");
    }

    @Test
    @DisplayName("Оформление заказа с ингредиентами авторизованным пользователем")
    public void testCreateOrderWithIngredients() {
        Order order = new Order(Arrays.asList(ingredientId1, ingredientId2));
        ValidatableResponse response = client.createOrderAuthUser(accessToken, order);
        response.statusCode(200);
        response.assertThat().body("name", notNullValue());
        response.assertThat().body("order.number", notNullValue());
        response.assertThat().body("success", equalTo(true));
    }

    @Test
    @DisplayName("Оформление заказа без ингредиентов авторизованным пользователем")
    public void testCreateOrderWithoutIngredients() {
        Order order = new Order(Collections.emptyList());
        ValidatableResponse response = client.createOrderAuthUser(accessToken, order);
        response.statusCode(400);
        response.assertThat().body("success", equalTo(false));
        response.assertThat().body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Оформление заказа с неверным id ингредиента авторизованным пользователем")
    public void testCreateOrderWithInvalidIngredientId() {
        Order order = new Order(Arrays.asList(ingredientId1, ingredientId2, "0000000000000000000000000"));
        ValidatableResponse response = client.createOrderAuthUser(accessToken, order);
        response.statusCode(500);
    }

    @Test
    @DisplayName("Оформление заказа не авторизованным пользователем")
    public void testCreateOrderNotAuthUser() {
        Order order = new Order(Arrays.asList(ingredientId1, ingredientId2, ingredientId3));
        ValidatableResponse response = client.createOrderNotAuthUser(order);
        response.statusCode(401);
        response.assertThat().body("success", equalTo(false));
        response.assertThat().body("message", equalTo("You should be authorised"));
    }

    @After
    public void after() {
        if (accessToken != null) {
            client.deleteUser(accessToken);
        }
    }
}