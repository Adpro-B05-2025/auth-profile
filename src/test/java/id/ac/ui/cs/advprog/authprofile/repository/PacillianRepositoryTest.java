package id.ac.ui.cs.advprog.authprofile.repository;

import id.ac.ui.cs.advprog.authprofile.config.TestConfig;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
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
class PacillianRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PacillianRepository pacillianRepository;


    @BeforeEach
    void setup() {
        // Clear the database before each test
        pacillianRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByEmail_ShouldReturnPacillian_WhenPacillianExists() {
        // given
        Pacillian pacillian = new Pacillian();
        pacillian.setEmail("pacillian@example.com");
        pacillian.setPassword("password");
        pacillian.setName("Test Pacillian");
        pacillian.setNik("1234567890123456");
        pacillian.setAddress("Test Address");
        pacillian.setPhoneNumber("081234567890");
        pacillian.setMedicalHistory("No significant medical history");

        entityManager.persistAndFlush(pacillian);

        // when
        Optional<Pacillian> found = pacillianRepository.findByEmail("pacillian@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("pacillian@example.com");
        assertThat(found.get().getName()).isEqualTo("Test Pacillian");
        assertThat(found.get().getMedicalHistory()).isEqualTo("No significant medical history");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenPacillianDoesNotExist() {
        // when
        Optional<Pacillian> found = pacillianRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void save_ShouldPersistPacillian() {
        // given
        Pacillian pacillian = new Pacillian();
        pacillian.setEmail("save-test@example.com");
        pacillian.setPassword("password");
        pacillian.setName("Save Test");
        pacillian.setNik("1234567890123457");
        pacillian.setAddress("Save Address");
        pacillian.setPhoneNumber("081234567891");
        pacillian.setMedicalHistory("Test medical history for save");

        // when
        Pacillian saved = pacillianRepository.save(pacillian);
        entityManager.flush();

        // then
        assertThat(saved.getId()).isNotNull();

        // Verify it can be retrieved
        Optional<Pacillian> found = pacillianRepository.findByEmail("save-test@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getMedicalHistory()).isEqualTo("Test medical history for save");
    }

    @Test
    void findById_ShouldReturnPacillian_WhenPacillianExists() {
        // given
        Pacillian pacillian = new Pacillian();
        pacillian.setEmail("findbyid@example.com");
        pacillian.setPassword("password");
        pacillian.setName("FindById Test");
        pacillian.setNik("7654321098765432");
        pacillian.setAddress("FindById Address");
        pacillian.setPhoneNumber("087654321098");
        pacillian.setMedicalHistory("FindById medical history");

        Pacillian savedPacillian = entityManager.persistAndFlush(pacillian);

        // when
        Optional<Pacillian> found = pacillianRepository.findById(savedPacillian.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("findbyid@example.com");
        assertThat(found.get().getName()).isEqualTo("FindById Test");
    }

    @Test
    void delete_ShouldRemovePacillian() {
        // given
        Pacillian pacillian = new Pacillian();
        pacillian.setEmail("delete-test@example.com");
        pacillian.setPassword("password");
        pacillian.setName("Delete Test");
        pacillian.setNik("8765432109876543");
        pacillian.setAddress("Delete Address");
        pacillian.setPhoneNumber("088765432109");
        pacillian.setMedicalHistory("Delete medical history");

        Pacillian savedPacillian = entityManager.persistAndFlush(pacillian);

        // Ensure it exists first
        assertThat(pacillianRepository.findById(savedPacillian.getId())).isPresent();

        // when
        pacillianRepository.delete(savedPacillian);
        entityManager.flush();

        // then
        assertThat(pacillianRepository.findById(savedPacillian.getId())).isEmpty();
    }
}
