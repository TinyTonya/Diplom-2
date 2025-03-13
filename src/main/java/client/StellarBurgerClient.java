package client;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.ValidatableResponse;
import model.Credentials;
import model.Order;
import model.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

public class StellarBurgerClient {

    private String baseUri;


    public StellarBurgerClient(String baseUri) {
        this.baseUri = baseUri;
    }

    @Step("Регистрация юзера")
    public ValidatableResponse registerUser(User user) {
        ValidatableResponse response = given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .body(user)
                .post("api/auth/register")
                .then()
                .log()
                .all();
            return response;
    }

    @Step("Логин юзера")
    public ValidatableResponse loginUser(Credentials credentials) {
        ValidatableResponse response = given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .body(credentials)
                .post("api/auth/login")
                .then()
                .log()
                .all();
                return response;
    }

    @Step("Изменение данных юзера с авторизацией")
    public ValidatableResponse editUserAuth(String accessToken, User user) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken)
                .body(user)
                .patch("api/auth/user/")
                .then()
                .log()
                .all();
    }

    @Step("Изменение данных юзера без авторизации")
    public ValidatableResponse editUserNotAuth(User user) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .body(user)
                .patch("api/auth/user/")
                .then()
                .log()
                .all();
    }

    @Step("Удаление юзера")
    public ValidatableResponse deleteUser(String accessToken) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken)
                .delete("api/auth/user")
                .then()
                .log()
                .all()
                .statusCode(202)
                .body("success", equalTo(true))
                .body("message", equalTo("User successfully removed"));
    }

    @Step("Получение данных об ингредиентах")
    public ValidatableResponse getIngredients() {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .get("api/ingredients")
                .then()
                .log()
                .all();
    }

    @Step("Создание заказа авторизованным юзером")
    public ValidatableResponse createOrderAuthUser(String accessToken, Order order) {
        ValidatableResponse response = given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/html")
                .header("Authorization", accessToken)
                .body(order)
                .post("api/orders")
                .then()
                .log()
                .all();
        return response;
    }

    @Step("Создание заказа НЕавторизованным юзером")
    public ValidatableResponse createOrderNotAuthUser(Order order) {
        ValidatableResponse response = given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .body(order)
                .post("api/orders")
                .then()
                .log()
                .all();
        return response;
    }

    @Step("Получение списка заказов авторизованным пользователем")
    public ValidatableResponse getOrdersAuthUser(String accessToken) {
        ValidatableResponse response= given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken)
                .get("api/orders")
                .then()
                .log()
                .all();
        return response;
    }

    @Step("Получение списка заказов не авторизованным пользователем")
    public ValidatableResponse getOrdersNotAuthUser(String accessToken) {
        ValidatableResponse response= given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseUri)
                .header("Content-Type", "application/json")
                .get("api/orders")
                .then()
                .log()
                .all();
        return response;
    }
}
