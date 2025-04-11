package id.ac.ui.cs.advprog.authprofile.dto;

import id.ac.ui.cs.advprog.authprofile.dto.response.JwtResponse;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class JwtResponseTest {

    @Test
    void testNoArgsConstructor() {
        // when
        JwtResponse response = new JwtResponse();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNull();
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getId()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getName()).isNull();
        assertThat(response.getRoles()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        // given
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String type = "Custom";
        Long id = 1L;
        String email = "user@example.com";
        String name = "Test User";
        List<String> roles = Arrays.asList("ROLE_PACILLIAN", "ROLE_CAREGIVER");

        // when
        JwtResponse response = new JwtResponse(token, type, id, email, name, roles);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getType()).isEqualTo(type);
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo(name);
        assertThat(response.getRoles()).isEqualTo(roles);
    }

    @Test
    void testParameterizedConstructor() {
        // given
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        Long id = 1L;
        String email = "user@example.com";
        String name = "Test User";
        List<String> roles = Arrays.asList("ROLE_PACILLIAN", "ROLE_CAREGIVER");

        // when
        JwtResponse response = new JwtResponse(token, id, email, name, roles);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getType()).isEqualTo("Bearer"); // Default type
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo(name);
        assertThat(response.getRoles()).isEqualTo(roles);
    }

    @Test
    void testSettersAndGetters() {
        // given
        JwtResponse response = new JwtResponse();
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String type = "Custom";
        Long id = 1L;
        String email = "user@example.com";
        String name = "Test User";
        List<String> roles = Arrays.asList("ROLE_PACILLIAN", "ROLE_CAREGIVER");

        // when
        response.setToken(token);
        response.setType(type);
        response.setId(id);
        response.setEmail(email);
        response.setName(name);
        response.setRoles(roles);

        // then
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getType()).isEqualTo(type);
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo(name);
        assertThat(response.getRoles()).isEqualTo(roles);
    }

    @Test
    void testEqualsAndHashCode() {
        // given
        List<String> roles = Arrays.asList("ROLE_PACILLIAN", "ROLE_CAREGIVER");

        JwtResponse response1 = new JwtResponse(
                "token1", "Bearer", 1L, "user@example.com", "Test User", roles
        );

        JwtResponse response2 = new JwtResponse(
                "token1", "Bearer", 1L, "user@example.com", "Test User", roles
        );

        JwtResponse response3 = new JwtResponse(
                "token2", "Bearer", 1L, "user@example.com", "Test User", roles
        );

        // then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1).isNotEqualTo(response3);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1.hashCode()).isNotEqualTo(response3.hashCode());
    }

    @Test
    void testToString() {
        // given
        List<String> roles = Arrays.asList("ROLE_PACILLIAN", "ROLE_CAREGIVER");
        JwtResponse response = new JwtResponse(
                "token", "Bearer", 1L, "user@example.com", "Test User", roles
        );

        // when
        String toString = response.toString();

        // then
        assertThat(toString).contains("token");
        assertThat(toString).contains("Bearer");
        assertThat(toString).contains("1");
        assertThat(toString).contains("user@example.com");
        assertThat(toString).contains("Test User");
        assertThat(toString).contains("ROLE_PACILLIAN");
        assertThat(toString).contains("ROLE_CAREGIVER");
    }
}