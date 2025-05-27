package id.ac.ui.cs.advprog.authprofile.dto;

import id.ac.ui.cs.advprog.authprofile.dto.response.TokenValidationResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TokenValidationResponseTest {

    @Test
    void createTokenValidationResponse_ShouldSetAllFields() {
        // given
        boolean valid = true;
        Long userId = 1L;
        String username = "test@example.com";
        List<String> roles = Arrays.asList("ROLE_PACILLIAN");

        // when
        TokenValidationResponse response = new TokenValidationResponse(valid, userId, username, roles);

        // then
        assertThat(response.isValid()).isEqualTo(valid);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getRoles()).isEqualTo(roles);
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyObject() {
        // when
        TokenValidationResponse response = new TokenValidationResponse();

        // then
        assertThat(response.isValid()).isFalse();
        assertThat(response.getUserId()).isNull();
        assertThat(response.getUsername()).isNull();
        assertThat(response.getRoles()).isNull();
    }

    @Test
    void setterMethods_ShouldUpdateFields() {
        // given
        TokenValidationResponse response = new TokenValidationResponse();

        // when
        response.setValid(true);
        response.setUserId(2L);
        response.setUsername("updated@example.com");
        List<String> roles = Arrays.asList("ROLE_CAREGIVER");
        response.setRoles(roles);

        // then
        assertThat(response.isValid()).isTrue();
        assertThat(response.getUserId()).isEqualTo(2L);
        assertThat(response.getUsername()).isEqualTo("updated@example.com");
        assertThat(response.getRoles()).isEqualTo(roles);
    }

    @Test
    void equals_AndHashCode_ShouldWorkCorrectly() {
        // given
        TokenValidationResponse response1 = new TokenValidationResponse(
                true, 1L, "test@example.com", Arrays.asList("ROLE_PACILLIAN"));
        TokenValidationResponse response2 = new TokenValidationResponse(
                true, 1L, "test@example.com", Arrays.asList("ROLE_PACILLIAN"));
        TokenValidationResponse response3 = new TokenValidationResponse(
                false, 2L, "other@example.com", Arrays.asList("ROLE_CAREGIVER"));

        // then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());

        assertThat(response1).isNotEqualTo(response3);
        assertThat(response1.hashCode()).isNotEqualTo(response3.hashCode());
    }
}