package id.ac.ui.cs.advprog.authprofile.repository;

import id.ac.ui.cs.advprog.authprofile.config.TestConfig;
import id.ac.ui.cs.advprog.authprofile.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;


    @BeforeEach
    void setup() {
        // Clear the database before each test
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setName("Test User");
        user.setNik("1234567890123456");
        user.setAddress("Test Address");
        user.setPhoneNumber("081234567890");

        entityManager.persistAndFlush(user);

        // when
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // given
        User user = new User();
        user.setEmail("exists@example.com");
        user.setPassword("password");
        user.setName("Existing User");
        user.setNik("1234567890123456");
        user.setAddress("Test Address");
        user.setPhoneNumber("081234567890");

        entityManager.persistAndFlush(user);

        // when
        boolean exists = userRepository.existsByEmail("exists@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // when
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByNik_ShouldReturnTrue_WhenNikExists() {
        // given
        User user = new User();
        user.setEmail("nik-test@example.com");
        user.setPassword("password");
        user.setName("NIK Test User");
        user.setNik("9876543210987654");
        user.setAddress("Test Address");
        user.setPhoneNumber("081234567890");

        entityManager.persistAndFlush(user);

        // when
        boolean exists = userRepository.existsByNik("9876543210987654");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByNik_ShouldReturnFalse_WhenNikDoesNotExist() {
        // when
        boolean exists = userRepository.existsByNik("1111111111111111");

        // then
        assertThat(exists).isFalse();
    }
}