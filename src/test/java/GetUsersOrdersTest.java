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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class GetUsersOrdersTest {
    private StellarBurgerClient client;
    private User user;
    private Faker faker;
    private String accessToken;
    private String ingredientId1;
    private String ingredientId2;

    @Before
    public void before() {
        faker = new Faker();
        client = new StellarBurgerClient(TestConfig.BASE_URI);
        user = new User(faker.internet().emailAddress(),faker.internet().password(),faker.name().fullName());
        ValidatableResponse response = client.registerUser(user);
        Assume.assumeTrue(response.extract().statusCode() == 200);
        accessToken = response.extract().path("accessToken");

        response = client.getIngredients();
        ingredientId1 = response.extract().path("data[0]._id");
        ingredientId2 = response.extract().path("data[1]._id");
    }

    @Test
    @DisplayName("Получение списка заказов авторизованным пользователем")
    public void testGetOrdersAuthUser() {
        // Создание заказа
        Order order = new Order(List.of(ingredientId1, ingredientId2));
        client.createOrderAuthUser(accessToken, order);

        // Получение списка заказов
        ValidatableResponse response = client.getOrdersAuthUser(accessToken);

        // Проверки
        response.statusCode(200)
                .body("success", equalTo(true))
                .body("orders", not(empty()))
                .body("total", greaterThanOrEqualTo(1))
                .body("totalToday", greaterThanOrEqualTo(1));

        // Проверка структуры заказа
        response.body("orders[0].ingredients", not(empty()))
                .body("orders[0]._id", not(emptyOrNullString()))
                .body("orders[0].status", not(emptyOrNullString()))
                .body("orders[0].number", notNullValue())
                .body("orders[0].createdAt", not(emptyOrNullString()))
                .body("orders[0].updatedAt", not(emptyOrNullString()));

        // Проверка отсутствия поля owner
        List<Object> orders = response.extract().path("orders");
        orders.forEach(orderObj -> assertTrue(
                ((java.util.Map<?, ?>) orderObj).keySet().stream()
                        .noneMatch(key -> key.equals("owner"))
        ));
    }

    @Test
    @DisplayName("Получение списка заказов без авторизации")
    public void testGetOrdersUnauthorized() {
        ValidatableResponse response = client.getOrdersNotAuthUser(accessToken);
        response.statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Получение пустого списка заказов, если их нет")
    public void testGetEmptyOrdersList() {
        // Не создаем заказов
        ValidatableResponse response = client.getOrdersAuthUser(accessToken);
        response.statusCode(200)
                .body("success", equalTo(true))
                .body("orders", empty())
                .body("total", equalTo(0))
                .body("totalToday", equalTo(0));
    }

     @After
    public void after() {
        if (accessToken != null) {
            client.deleteUser(accessToken);
        }
    }
}