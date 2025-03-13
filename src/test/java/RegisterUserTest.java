import com.github.javafaker.Faker;
import model.Credentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import client.StellarBurgerClient;
import config.TestConfig;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.User;
import static org.hamcrest.Matchers.equalTo;

public class RegisterUserTest {
    private User user;
    private StellarBurgerClient client;
    private Faker faker;
    private String accessToken;

    @Before
    public void before() {
        faker = new Faker();
        client = new StellarBurgerClient(TestConfig.BASE_URI);
        user = new User(faker.internet().emailAddress(),faker.internet().password(),faker.name().fullName());
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    public void testCreateUniqueUser() {
        ValidatableResponse response = client.registerUser(user);
        response.statusCode(200);
        response.body("success", equalTo(true));
        response.body("user.email", equalTo(user.getEmail()));
        response.body("user.name", equalTo(user.getName()));
        accessToken = response.extract().path("accessToken");

    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void testCreateExistingUser() {
        // Первая регистрация
        ValidatableResponse firstResponse = client.registerUser(user);
        firstResponse.statusCode(200);
        accessToken = firstResponse.extract().path("accessToken");


        // Попытка повторной регистрации
        ValidatableResponse response = client.registerUser(user);
        response.statusCode(403);
        response.body("success", equalTo(false));
        response.body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без email")
    public void testCreateUserWithoutRequiredEmail() {
        // Создаем пользователя без email
        User userWithoutEmail = new User(null, user.getPassword(), user.getName());

        ValidatableResponse response = client.registerUser(userWithoutEmail);
        response.statusCode(403);
        response.body("success", equalTo(false));
        response.body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    public void testCreateUserWithoutRequiredPassword() {
        // Создаем пользователя без пароля
        User userWithoutPassword = new User(user.getEmail(), null, user.getName());

        ValidatableResponse response = client.registerUser(userWithoutPassword);
        response.statusCode(403);
        response.body("success", equalTo(false));
        response.body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    public void testCreateUserWithoutRequiredName() {
        // Создаем пользователя без имени
        User userWithoutName = new User(user.getEmail(), user.getPassword(), null);

        ValidatableResponse response = client.registerUser(userWithoutName);
        response.statusCode(403);
        response.body("success", equalTo(false));
        response.body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            client.deleteUser(accessToken);
            }
        }
    }

