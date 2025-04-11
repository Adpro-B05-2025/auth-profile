package id.ac.ui.cs.advprog.authprofile.dto;

import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BaseRegisterRequestTest {

    @Test
    void testNoArgsConstructor() {
        // when
        BaseRegisterRequest request = new BaseRegisterRequest();

        // then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPassword()).isNull();
        assertThat(request.getName()).isNull();
        assertThat(request.getNik()).isNull();
        assertThat(request.getAddress()).isNull();
        assertThat(request.getPhoneNumber()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String name = "Test Name";
        String nik = "1234567890123456";
        String address = "123 Main St";
        String phoneNumber = "0812345678";

        // when
        BaseRegisterRequest request = new BaseRegisterRequest(
                email, password, name, nik, address, phoneNumber
        );

        // then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getNik()).isEqualTo(nik);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    void testSettersAndGetters() {
        // given
        BaseRegisterRequest request = new BaseRegisterRequest();
        String email = "test@example.com";
        String password = "password123";
        String name = "Test Name";
        String nik = "1234567890123456";
        String address = "123 Main St";
        String phoneNumber = "0812345678";

        // when
        request.setEmail(email);
        request.setPassword(password);
        request.setName(name);
        request.setNik(nik);
        request.setAddress(address);
        request.setPhoneNumber(phoneNumber);

        // then
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getNik()).isEqualTo(nik);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    void testEqualsAndHashCode() {
        // given
        BaseRegisterRequest request1 = new BaseRegisterRequest(
                "test@example.com", "password123", "Test Name",
                "1234567890123456", "123 Main St", "0812345678"
        );

        BaseRegisterRequest request2 = new BaseRegisterRequest(
                "test@example.com", "password123", "Test Name",
                "1234567890123456", "123 Main St", "0812345678"
        );

        BaseRegisterRequest request3 = new BaseRegisterRequest(
                "other@example.com", "password123", "Test Name",
                "1234567890123456", "123 Main St", "0812345678"
        );

        // then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.hashCode()).isNotEqualTo(request3.hashCode());
    }

    @Test
    void testToString() {
        // given
        BaseRegisterRequest request = new BaseRegisterRequest(
                "test@example.com", "password123", "Test Name",
                "1234567890123456", "123 Main St", "0812345678"
        );

        // when
        String toString = request.toString();

        // then
        assertThat(toString).contains("test@example.com");
        assertThat(toString).contains("password123");
        assertThat(toString).contains("Test Name");
        assertThat(toString).contains("1234567890123456");
        assertThat(toString).contains("123 Main St");
        assertThat(toString).contains("0812345678");
    }

    @Test
    void testBuilderPattern() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String name = "Test Name";
        String nik = "1234567890123456";
        String address = "123 Main St";
        String phoneNumber = "0812345678";

        // when
        BaseRegisterRequest request = BaseRegisterRequest.builder()
                .email(email)
                .password(password)
                .name(name)
                .nik(nik)
                .address(address)
                .phoneNumber(phoneNumber)
                .build();

        // then
        assertThat(request.getEmail()).isEqualTo(email);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getNik()).isEqualTo(nik);
        assertThat(request.getAddress()).isEqualTo(address);
        assertThat(request.getPhoneNumber()).isEqualTo(phoneNumber);
    }
}