package id.ac.ui.cs.advprog.authprofile.dto;

import id.ac.ui.cs.advprog.authprofile.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {

    @Test
    void testNoArgsConstructor() {
        // when
        LoginRequest loginRequest = new LoginRequest();

        // then
        assertThat(loginRequest).isNotNull();
        assertThat(loginRequest.getEmail()).isNull();
        assertThat(loginRequest.getPassword()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        // given
        String email = "test@example.com";
        String password = "password123";

        // when
        LoginRequest loginRequest = new LoginRequest(email, password);

        // then
        assertThat(loginRequest).isNotNull();
        assertThat(loginRequest.getEmail()).isEqualTo(email);
        assertThat(loginRequest.getPassword()).isEqualTo(password);
    }

    @Test
    void testSettersAndGetters() {
        // given
        LoginRequest loginRequest = new LoginRequest();
        String email = "test@example.com";
        String password = "password123";

        // when
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        // then
        assertThat(loginRequest.getEmail()).isEqualTo(email);
        assertThat(loginRequest.getPassword()).isEqualTo(password);
    }

    @Test
    void testEqualsAndHashCode() {
        // given
        LoginRequest loginRequest1 = new LoginRequest("test@example.com", "password123");
        LoginRequest loginRequest2 = new LoginRequest("test@example.com", "password123");
        LoginRequest loginRequest3 = new LoginRequest("other@example.com", "password123");

        // then
        assertThat(loginRequest1).isEqualTo(loginRequest2);
        assertThat(loginRequest1).isNotEqualTo(loginRequest3);
        assertThat(loginRequest1.hashCode()).isEqualTo(loginRequest2.hashCode());
        assertThat(loginRequest1.hashCode()).isNotEqualTo(loginRequest3.hashCode());
    }

    @Test
    void testToString() {
        // given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");

        // when
        String toString = loginRequest.toString();

        // then
        assertThat(toString).contains("test@example.com");
        assertThat(toString).contains("password123");
    }
}
