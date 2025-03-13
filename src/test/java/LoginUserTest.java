import client.StellarBurgerClient;
import com.github.javafaker.Faker;
import config.TestConfig;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.Credentials;
import model.User;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

public class LoginUserTest {
    private User user;
    private StellarBurgerClient client;
    private Faker faker;
    private String accessToken;

    @Before
    public void before() {
        faker = new Faker();
        client = new StellarBurgerClient(TestConfig.BASE_URI);
        user = new User(faker.internet().emailAddress(),faker.internet().password(),faker.name().fullName());
        ValidatableResponse response = client.registerUser(user);
        Assume.assumeTrue(response.extract().statusCode() == 200);
    }

    @Test
    @DisplayName("Логин юзера с валидными кредами")
    public void testValidLogin() {
        ValidatableResponse response = client.loginUser(new Credentials(user.getEmail(), user.getPassword()));
        response.assertThat().statusCode(200);
        response.assertThat().body("success", equalTo(true));
        accessToken = response.extract().path("accessToken");
    }

    @Test
    @DisplayName("Логин юзера без email")
    public void testLoginWithoutEmail() {
        ValidatableResponse response = client.loginUser(new Credentials(null, user.getPassword()));
        response.assertThat().statusCode(401);
        response.assertThat().body("success", equalTo(false));
        response.assertThat().body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин юзера с некорректным email")
    public void testLoginWithNotValidEmail() {
        ValidatableResponse response = client.loginUser(new Credentials("some_not_correct_email_for_this_user@mail.com", user.getPassword()));
        response.assertThat().statusCode(401);
        response.assertThat().body("success", equalTo(false));
        response.assertThat().body("message", equalTo("email or password are incorrect"));
    }
    @Test
    @DisplayName("Логин юзера без password")
    public void testLoginWithoutPassword() {
        ValidatableResponse response = client.loginUser(new Credentials(user.getEmail(), null));
        response.assertThat().statusCode(401);
        response.assertThat().body("success", equalTo(false));
        response.assertThat().body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин юзера с некорректным password")
    public void testLoginWithNotValidPassword() {
        ValidatableResponse response = client.loginUser(new Credentials(user.getEmail(), "some_not_correct_password_for_this_user"));
        response.assertThat().statusCode(401);
        response.assertThat().body("success", equalTo(false));
        response.assertThat().body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин юзера с некорректными email и password")
    public void testLoginWithNotValidCredentials() {
        ValidatableResponse response = client.loginUser(new Credentials("some_not_correct_email_for_this_user@mail.com", "some_not_correct_password_for_this_user"));
        response.assertThat().statusCode(401);
        response.assertThat().body("success", equalTo(false));
        response.assertThat().body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void after() {
        if (accessToken != null) {
            client.deleteUser(accessToken);
        }
    }
}
