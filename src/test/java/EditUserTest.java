import client.StellarBurgerClient;
import com.github.javafaker.Faker;
import config.TestConfig;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.User;
import org.junit.Assume;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

    public class EditUserTest {
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
            accessToken = response.extract().path("accessToken");
        }

        @Test
        @DisplayName("Изменение email у авторизованного юзера")
        public void testEditUserAuthEmail() {
            User updatedUser = new User("somerandomnotexistingemail@example.com", user.getPassword(), user.getName());
            ValidatableResponse response = client.editUserAuth(accessToken, updatedUser);
            response.assertThat().statusCode(200);
            response.assertThat().body("success", equalTo(true));
            response.assertThat().body("user.email", equalTo(updatedUser.getEmail()));
        }

        @Test
        @DisplayName("Изменение password у авторизованного юзера")
        public void testEditUserAuthPassword() {
            User updatedUser = new User(user.getEmail(), "newpassword", user.getName());
            ValidatableResponse response = client.editUserAuth(accessToken, updatedUser);
            response.assertThat().statusCode(200);
            response.assertThat().body("success", equalTo(true));
            response.assertThat().body("user.email", equalTo(updatedUser.getEmail()));
            response.assertThat().body("user.name", equalTo(updatedUser.getName()));
        }

        @Test
        @DisplayName("Изменение Name у авторизованного юзера")
        public void testEditUserAuthName() {
            User updatedUser = new User(user.getEmail(), user.getPassword(), "John Smith");
            ValidatableResponse response = client.editUserAuth(accessToken, updatedUser);
            response.assertThat().statusCode(200);
            response.assertThat().body("success", equalTo(true));
            response.assertThat().body("user.name", equalTo(updatedUser.getName()));
        }

        @Test
        @DisplayName("Изменение email у НЕавторизованного юзера")
        public void testEditUserNotAuthEmail() {
            User updatedUser = new User("some_randomnewemail@example.com", user.getPassword(), user.getName());
            ValidatableResponse response = client.editUserNotAuth(updatedUser);
            response.assertThat().statusCode(401);
            response.assertThat().body("success", equalTo(false));
            response.assertThat().body("message", equalTo("You should be authorised"));
        }

        @Test
        @DisplayName("Изменение password у НЕавторизованного юзера")
        public void testEditUserNotAuthPassword() {
            User updatedUser = new User(user.getEmail(), "newpassword", user.getName());
            ValidatableResponse response = client.editUserNotAuth(updatedUser);
            response.assertThat().statusCode(401);
            response.assertThat().body("success", equalTo(false));
            response.assertThat().body("message", equalTo("You should be authorised"));
        }

        @Test
        @DisplayName("Изменение Name у НЕавторизованного юзера")
        public void testEditUserNotAuthName() {
            User updatedUser = new User(user.getEmail(), user.getPassword(), "John Smith");
            ValidatableResponse response = client.editUserNotAuth(updatedUser);
            response.assertThat().statusCode(401);
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
