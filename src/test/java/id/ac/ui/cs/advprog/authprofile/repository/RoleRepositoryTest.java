package id.ac.ui.cs.advprog.authprofile.repository;

import id.ac.ui.cs.advprog.authprofile.config.TestConfig;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setup() {
        // Clear the database before each test
        roleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByName_ShouldReturnRole_WhenRoleExists() {
        // given
        Role pacillianRole = new Role();
        pacillianRole.setName(Role.ERole.ROLE_PACILLIAN);

        Role caregiverRole = new Role();
        caregiverRole.setName(Role.ERole.ROLE_CAREGIVER);

        entityManager.persistAndFlush(pacillianRole);
        entityManager.persistAndFlush(caregiverRole);

        // when
        Optional<Role> foundPacillian = roleRepository.findByName(Role.ERole.ROLE_PACILLIAN);
        Optional<Role> foundCareGiver = roleRepository.findByName(Role.ERole.ROLE_CAREGIVER);

        // then
        assertThat(foundPacillian).isPresent();
        assertThat(foundPacillian.get().getName()).isEqualTo(Role.ERole.ROLE_PACILLIAN);

        assertThat(foundCareGiver).isPresent();
        assertThat(foundCareGiver.get().getName()).isEqualTo(Role.ERole.ROLE_CAREGIVER);
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenRoleDoesNotExist() {
        // when - no need to clear, BeforeEach already does this
        Optional<Role> foundPacillian = roleRepository.findByName(Role.ERole.ROLE_PACILLIAN);
        Optional<Role> foundCareGiver = roleRepository.findByName(Role.ERole.ROLE_CAREGIVER);

        // then
        assertThat(foundPacillian).isEmpty();
        assertThat(foundCareGiver).isEmpty();
    }

    @Test
    void save_ShouldPersistRole() {
        // given
        Role customRole = new Role();
        customRole.setName(Role.ERole.ROLE_PACILLIAN);

        // when
        Role savedRole = roleRepository.save(customRole);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to ensure we're reading from DB

        // then
        assertThat(savedRole.getId()).isNotNull();

        // Verify by ID, not by name to avoid duplicates
        Optional<Role> found = roleRepository.findById(savedRole.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(Role.ERole.ROLE_PACILLIAN);
    }

    @Test
    void count_ShouldReturnNumberOfRoles() {
        // Initially count should be 0 because of @BeforeEach
        assertThat(roleRepository.count()).isEqualTo(0);

        // Add roles
        Role pacillianRole = new Role();
        pacillianRole.setName(Role.ERole.ROLE_PACILLIAN);

        Role caregiverRole = new Role();
        caregiverRole.setName(Role.ERole.ROLE_CAREGIVER);

        entityManager.persistAndFlush(pacillianRole);
        entityManager.persistAndFlush(caregiverRole);

        // Count should be 2
        assertThat(roleRepository.count()).isEqualTo(2);
    }

    @Test
    void findAll_ShouldReturnAllRoles() {
        // Add roles
        Role pacillianRole = new Role();
        pacillianRole.setName(Role.ERole.ROLE_PACILLIAN);

        Role caregiverRole = new Role();
        caregiverRole.setName(Role.ERole.ROLE_CAREGIVER);

        entityManager.persistAndFlush(pacillianRole);
        entityManager.persistAndFlush(caregiverRole);

        // when
        List<Role> allRoles = roleRepository.findAll();

        // then
        assertThat(allRoles).hasSize(2);
        assertThat(allRoles).extracting(Role::getName)
                .containsExactlyInAnyOrder(Role.ERole.ROLE_PACILLIAN, Role.ERole.ROLE_CAREGIVER);
    }

    @Test
    void deleteById_ShouldRemoveRole() {
        // given
        Role pacillianRole = new Role();
        pacillianRole.setName(Role.ERole.ROLE_PACILLIAN);

        Role savedRole = entityManager.persistAndFlush(pacillianRole);
        Integer roleId = savedRole.getId();

        // Ensure it exists first
        assertThat(roleRepository.findById(roleId)).isPresent();

        // when
        roleRepository.deleteById(roleId);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(roleRepository.findById(roleId)).isEmpty();
    }
}