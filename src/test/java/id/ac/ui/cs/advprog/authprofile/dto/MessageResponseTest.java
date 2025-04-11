package id.ac.ui.cs.advprog.authprofile.dto;

import id.ac.ui.cs.advprog.authprofile.dto.response.MessageResponse;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MessageResponseTest {

    @Test
    void testNoArgsConstructor() {
        // when
        MessageResponse response = new MessageResponse();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.isSuccess()).isFalse();
    }

    @Test
    void testAllArgsConstructor() {
        // given
        String message = "Operation successful";
        boolean success = true;

        // when
        MessageResponse response = new MessageResponse(message, success);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.isSuccess()).isEqualTo(success);
    }

    @Test
    void testSingleArgConstructor() {
        // given
        String message = "Operation successful";

        // when
        MessageResponse response = new MessageResponse(message);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.isSuccess()).isTrue(); // Default value should be true
    }

    @Test
    void testSettersAndGetters() {
        // given
        MessageResponse response = new MessageResponse();
        String message = "Operation successful";
        boolean success = true;

        // when
        response.setMessage(message);
        response.setSuccess(success);

        // then
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.isSuccess()).isEqualTo(success);
    }

    @Test
    void testEqualsAndHashCode() {
        // given
        MessageResponse response1 = new MessageResponse("Operation successful", true);
        MessageResponse response2 = new MessageResponse("Operation successful", true);
        MessageResponse response3 = new MessageResponse("Operation failed", false);

        // then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1).isNotEqualTo(response3);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1.hashCode()).isNotEqualTo(response3.hashCode());
    }

    @Test
    void testToString() {
        // given
        MessageResponse response = new MessageResponse("Operation successful", true);

        // when
        String toString = response.toString();

        // then
        assertThat(toString).contains("Operation successful");
        assertThat(toString).contains("true");
    }
}