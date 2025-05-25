package id.ac.ui.cs.advprog.authprofile.repository;

import id.ac.ui.cs.advprog.authprofile.config.TestConfig;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class CareGiverRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CareGiverRepository careGiverRepository;

    @BeforeEach
    void setup() {
        // Clear the database before each test
        careGiverRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByEmail_ShouldReturnCareGiver_WhenCareGiverExists() {
        // given
        CareGiver careGiver = new CareGiver();
        careGiver.setEmail("doctor@example.com");
        careGiver.setPassword("password");
        careGiver.setName("Dr. Smith");
        careGiver.setNik("1234567890123456");
        careGiver.setAddress("Doctor Address");
        careGiver.setPhoneNumber("081234567890");
        careGiver.setSpeciality("Cardiology");
        careGiver.setWorkAddress("Heart Hospital");

        entityManager.persistAndFlush(careGiver);

        // when
        Optional<CareGiver> found = careGiverRepository.findByEmail("doctor@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("doctor@example.com");
        assertThat(found.get().getName()).isEqualTo("Dr. Smith");
        assertThat(found.get().getSpeciality()).isEqualTo("Cardiology");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenCareGiverDoesNotExist() {
        // when
        Optional<CareGiver> found = careGiverRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingCareGivers() {
        // given
        CareGiver careGiver1 = createCareGiver("smith@example.com", "Dr. Smith", "1111111111111111", "Cardiology", "Hospital 1");
        CareGiver careGiver2 = createCareGiver("johnson@example.com", "Dr. Johnson", "2222222222222222", "Neurology", "Hospital 2");
        CareGiver careGiver3 = createCareGiver("smithson@example.com", "Dr. Smithson", "3333333333333333", "Dermatology", "Hospital 3");

        entityManager.persistAndFlush(careGiver1);
        entityManager.persistAndFlush(careGiver2);
        entityManager.persistAndFlush(careGiver3);

        // when
        List<CareGiver> foundBySmith = careGiverRepository.findByNameContainingIgnoreCase("smith");
        List<CareGiver> foundByDr = careGiverRepository.findByNameContainingIgnoreCase("dr");
        List<CareGiver> foundByNonexistent = careGiverRepository.findByNameContainingIgnoreCase("nonexistent");

        // then
        assertThat(foundBySmith).hasSize(2);
        assertThat(foundBySmith).extracting(CareGiver::getName)
                .containsExactlyInAnyOrder("Dr. Smith", "Dr. Smithson");

        assertThat(foundByDr).hasSize(3);
        assertThat(foundByDr).extracting(CareGiver::getName)
                .containsExactlyInAnyOrder("Dr. Smith", "Dr. Johnson", "Dr. Smithson");

        assertThat(foundByNonexistent).isEmpty();
    }

    @Test
    void findBySpecialityContainingIgnoreCase_ShouldReturnMatchingCareGivers() {
        // given
        CareGiver careGiver1 = createCareGiver("cardio1@example.com", "Dr. Cardio One", "4444444444444444", "Cardiology", "Hospital 4");
        CareGiver careGiver2 = createCareGiver("neuro@example.com", "Dr. Neuro", "5555555555555555", "Neurology", "Hospital 5");
        CareGiver careGiver3 = createCareGiver("cardio2@example.com", "Dr. Cardio Two", "6666666666666666", "Cardiology", "Hospital 6");

        entityManager.persistAndFlush(careGiver1);
        entityManager.persistAndFlush(careGiver2);
        entityManager.persistAndFlush(careGiver3);

        // when
        List<CareGiver> foundByCardio = careGiverRepository.findBySpecialityContainingIgnoreCase("cardio");
        List<CareGiver> foundByNeuro = careGiverRepository.findBySpecialityContainingIgnoreCase("neuro");
        List<CareGiver> foundByLogy = careGiverRepository.findBySpecialityContainingIgnoreCase("logy");
        List<CareGiver> foundByNonexistent = careGiverRepository.findBySpecialityContainingIgnoreCase("nonexistent");

        // then
        assertThat(foundByCardio).hasSize(2);
        assertThat(foundByCardio).extracting(CareGiver::getEmail)
                .containsExactlyInAnyOrder("cardio1@example.com", "cardio2@example.com");

        assertThat(foundByNeuro).hasSize(1);
        assertThat(foundByNeuro.get(0).getEmail()).isEqualTo("neuro@example.com");

        assertThat(foundByLogy).hasSize(3);
        assertThat(foundByLogy).extracting(CareGiver::getEmail)
                .containsExactlyInAnyOrder("cardio1@example.com", "neuro@example.com", "cardio2@example.com");

        assertThat(foundByNonexistent).isEmpty();
    }

    @Test
    void findByNameAndSpeciality_ShouldReturnMatchingCareGivers() {
        // given
        CareGiver careGiver1 = createCareGiver("smith.cardio@example.com", "Dr. Smith", "7777777777777777", "Cardiology", "Hospital 7");
        CareGiver careGiver2 = createCareGiver("smith.neuro@example.com", "Dr. Smith", "8888888888888888", "Neurology", "Hospital 8");
        CareGiver careGiver3 = createCareGiver("johnson.cardio@example.com", "Dr. Johnson", "9999999999999999", "Cardiology", "Hospital 9");

        entityManager.persistAndFlush(careGiver1);
        entityManager.persistAndFlush(careGiver2);
        entityManager.persistAndFlush(careGiver3);

        // when
        List<CareGiver> foundBySmithCardio = careGiverRepository.findByNameAndSpeciality("Smith", "Cardiology");
        List<CareGiver> foundBySmithNeuro = careGiverRepository.findByNameAndSpeciality("Smith", "Neurology");
        List<CareGiver> foundByJohnsonCardio = careGiverRepository.findByNameAndSpeciality("Johnson", "Cardiology");
        List<CareGiver> foundByNonexistent = careGiverRepository.findByNameAndSpeciality("nonexistent", "nonexistent");

        // then
        assertThat(foundBySmithCardio).hasSize(1);
        assertThat(foundBySmithCardio.get(0).getEmail()).isEqualTo("smith.cardio@example.com");

        assertThat(foundBySmithNeuro).hasSize(1);
        assertThat(foundBySmithNeuro.get(0).getEmail()).isEqualTo("smith.neuro@example.com");

        assertThat(foundByJohnsonCardio).hasSize(1);
        assertThat(foundByJohnsonCardio.get(0).getEmail()).isEqualTo("johnson.cardio@example.com");

        assertThat(foundByNonexistent).isEmpty();
    }

    @Test
    void findCareGiversWithFilters_ShouldReturnFilteredResults() {
        // given
        CareGiver careGiver1 = createCareGiverWithRating("alice@example.com", "Alice", "1111111111111111", "Cardiology", "Hospital 1", 4.5);
        CareGiver careGiver2 = createCareGiverWithRating("bob@example.com", "Bob", "2222222222222222", "Neurology", "Hospital 2", 4.8);
        CareGiver careGiver3 = createCareGiverWithRating("charlie@example.com", "Charlie", "3333333333333333", "Cardiology", "Hospital 3", 4.2);
        CareGiver careGiver4 = createCareGiverWithRating("alice2@example.com", "Alice", "4444444444444444", "Dermatology", "Hospital 4", 4.0);

        entityManager.persist(careGiver1);
        entityManager.persist(careGiver2);
        entityManager.persist(careGiver3);
        entityManager.persist(careGiver4);
        entityManager.flush();

        // when & then
        // Test case 1: both name and speciality provided
        List<CareGiver> result1 = careGiverRepository.findCareGiversWithFilters("Ali", "Cardio");
        assertThat(result1).hasSize(1);
        assertThat(result1.get(0).getEmail()).isEqualTo("alice@example.com");

        // Test case 2: name is null, speciality provided
        List<CareGiver> result2 = careGiverRepository.findCareGiversWithFilters(null, "Cardiology");
        assertThat(result2).hasSize(2);
        assertThat(result2).extracting(CareGiver::getEmail)
                .containsExactlyInAnyOrder("alice@example.com", "charlie@example.com");

        // Test case 3: speciality is null, name provided
        List<CareGiver> result3 = careGiverRepository.findCareGiversWithFilters("Bob", null);
        assertThat(result3).hasSize(1);
        assertThat(result3.get(0).getEmail()).isEqualTo("bob@example.com");

        // Test case 4: both null
        List<CareGiver> result4 = careGiverRepository.findCareGiversWithFilters(null, null);
        assertThat(result4).hasSize(4);
    }

    // NEW TESTS FOR PAGINATED METHODS

    @Test
    void findByNameContainingIgnoreCaseWithPagination_ShouldReturnPagedResults() {
        // given
        CareGiver careGiver1 = createCareGiverWithRating("alice@example.com", "Alice", "1111111111111111", "Cardiology", "Hospital 1", 4.5);
        CareGiver careGiver2 = createCareGiverWithRating("alice2@example.com", "Alice Smith", "2222222222222222", "Neurology", "Hospital 2", 4.8);
        CareGiver careGiver3 = createCareGiverWithRating("bob@example.com", "Bob", "3333333333333333", "Cardiology", "Hospital 3", 4.2);

        entityManager.persist(careGiver1);
        entityManager.persist(careGiver2);
        entityManager.persist(careGiver3);
        entityManager.flush();

        // when & then
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "averageRating"));

        // Test paginated search by name
        Page<CareGiver> result = careGiverRepository.findByNameContainingIgnoreCase("Alice", pageable);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent()).extracting(CareGiver::getEmail)
                .containsExactly("alice2@example.com", "alice@example.com");
    }

    @Test
    void findBySpecialityContainingIgnoreCaseWithPagination_ShouldReturnPagedResults() {
        // given
        CareGiver careGiver1 = createCareGiverWithRating("alice@example.com", "Alice", "1111111111111111", "Cardiology", "Hospital 1", 4.5);
        CareGiver careGiver2 = createCareGiverWithRating("bob@example.com", "Bob", "2222222222222222", "Neurology", "Hospital 2", 4.8);
        CareGiver careGiver3 = createCareGiverWithRating("charlie@example.com", "Charlie", "3333333333333333", "Cardiology", "Hospital 3", 4.2);

        entityManager.persist(careGiver1);
        entityManager.persist(careGiver2);
        entityManager.persist(careGiver3);
        entityManager.flush();

        // when & then
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "averageRating"));

        // Test paginated search by speciality
        Page<CareGiver> result = careGiverRepository.findBySpecialityContainingIgnoreCase("Cardiology", pageable);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent()).extracting(CareGiver::getEmail)
                .containsExactly("alice@example.com", "charlie@example.com");
    }

    @Test
    void findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase_ShouldReturnPagedResults() {
        // given
        CareGiver careGiver1 = createCareGiverWithRating("alice.cardio@example.com", "Alice", "1111111111111111", "Cardiology", "Hospital 1", 4.5);
        CareGiver careGiver2 = createCareGiverWithRating("alice.neuro@example.com", "Alice", "2222222222222222", "Neurology", "Hospital 2", 4.8);
        CareGiver careGiver3 = createCareGiverWithRating("bob.cardio@example.com", "Bob", "3333333333333333", "Cardiology", "Hospital 3", 4.2);

        entityManager.persist(careGiver1);
        entityManager.persist(careGiver2);
        entityManager.persist(careGiver3);
        entityManager.flush();

        // when & then
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "averageRating"));

        // Test paginated search by both name and speciality
        Page<CareGiver> result = careGiverRepository.findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(
                "Alice", "Cardiology", pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("alice.cardio@example.com");
    }

    @Test
    void findNameSuggestions_ShouldReturnDistinctNamesContainingPrefix() {
        // given
        CareGiver careGiver1 = createCareGiver("alice@example.com", "Alice", "1111111111111111", "Cardiology", "Hospital 1");
        CareGiver careGiver2 = createCareGiver("alice2@example.com", "alice", "2222222222222222", "Cardiology", "Hospital 2");
        CareGiver careGiver3 = createCareGiver("bob@example.com", "Bob", "3333333333333333", "Neurology", "Hospital 3");

        entityManager.persist(careGiver1);
        entityManager.persist(careGiver2);
        entityManager.persist(careGiver3);
        entityManager.flush();

        // when & then
        List<String> suggestionsA = careGiverRepository.findNameSuggestions("A");
        assertThat(suggestionsA).containsExactlyInAnyOrder("Alice", "alice");

        List<String> suggestionsLowerA = careGiverRepository.findNameSuggestions("a");
        assertThat(suggestionsLowerA).containsExactlyInAnyOrder("Alice", "alice");

        List<String> suggestionsB = careGiverRepository.findNameSuggestions("B");
        assertThat(suggestionsB).containsExactly("Bob");

        List<String> suggestionsZ = careGiverRepository.findNameSuggestions("Z");
        assertThat(suggestionsZ).isEmpty();
    }

    @Test
    void findSpecialitySuggestions_ShouldReturnDistinctSpecialitiesContainingQuery() {
        // given
        CareGiver careGiver1 = createCareGiver("cardio@example.com", "Dr. Cardio", "1111111111111111", "Cardiology", "Hospital 1");
        CareGiver careGiver2 = createCareGiver("neuro@example.com", "Dr. Neuro", "2222222222222222", "Neurology", "Hospital 2");
        CareGiver careGiver3 = createCareGiver("cardio2@example.com", "Dr. Cardio2", "3333333333333333", "cardio", "Hospital 3");

        entityManager.persist(careGiver1);
        entityManager.persist(careGiver2);
        entityManager.persist(careGiver3);
        entityManager.flush();

        // when & then
        List<String> suggestionsCard = careGiverRepository.findSpecialitySuggestions("card");
        assertThat(suggestionsCard).containsExactlyInAnyOrder("Cardiology", "cardio");

        List<String> suggestionsNeuro = careGiverRepository.findSpecialitySuggestions("neuro");
        assertThat(suggestionsNeuro).containsExactly("Neurology");

        List<String> suggestionsLogy = careGiverRepository.findSpecialitySuggestions("logy");
        assertThat(suggestionsLogy).containsExactlyInAnyOrder("Cardiology", "Neurology");

        List<String> suggestionsNone = careGiverRepository.findSpecialitySuggestions("nonexistent");
        assertThat(suggestionsNone).isEmpty();
    }

    // ADDITIONAL TESTS FOR EDGE CASES

    @Test
    void findCareGiversWithFilters_WithEmptyResults_ShouldReturnEmptyList() {
        // given
        CareGiver careGiver1 = createCareGiver("alice@example.com", "Alice", "1111111111111111", "Cardiology", "Hospital 1");
        entityManager.persistAndFlush(careGiver1);

        // when
        List<CareGiver> result = careGiverRepository.findCareGiversWithFilters("NonExistent", "NonExistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCase_WithPagination_ShouldHandleLargePageSize() {
        // given
        CareGiver careGiver1 = createCareGiver("alice@example.com", "Alice", "1111111111111111", "Cardiology", "Hospital 1");
        CareGiver careGiver2 = createCareGiver("bob@example.com", "Bob", "2222222222222222", "Neurology", "Hospital 2");
        entityManager.persist(careGiver1);
        entityManager.persist(careGiver2);
        entityManager.flush();

        // when
        Pageable pageable = PageRequest.of(0, 100);
        Page<CareGiver> result = careGiverRepository.findByNameContainingIgnoreCase("", pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    void findBySpecialityContainingIgnoreCase_WithDifferentSortOrders_ShouldReturnSortedResults() {
        // given
        CareGiver careGiver1 = createCareGiverWithRating("alice@example.com", "Alice", "1111111111111111", "Cardiology", "Hospital 1", 4.5);
        CareGiver careGiver2 = createCareGiverWithRating("bob@example.com", "Bob", "2222222222222222", "Cardiology", "Hospital 2", 4.8);
        CareGiver careGiver3 = createCareGiverWithRating("charlie@example.com", "Charlie", "3333333333333333", "Cardiology", "Hospital 3", 4.2);

        entityManager.persist(careGiver1);
        entityManager.persist(careGiver2);
        entityManager.persist(careGiver3);
        entityManager.flush();

        // when & then
        // Test sorting by rating descending
        Pageable pageableRatingDesc = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "averageRating"));
        Page<CareGiver> resultRatingDesc = careGiverRepository.findBySpecialityContainingIgnoreCase("Cardiology", pageableRatingDesc);
        assertThat(resultRatingDesc.getContent()).extracting(CareGiver::getEmail)
                .containsExactly("bob@example.com", "alice@example.com", "charlie@example.com");

        // Test sorting by name ascending
        Pageable pageableNameAsc = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<CareGiver> resultNameAsc = careGiverRepository.findBySpecialityContainingIgnoreCase("Cardiology", pageableNameAsc);
        assertThat(resultNameAsc.getContent()).extracting(CareGiver::getEmail)
                .containsExactly("alice@example.com", "bob@example.com", "charlie@example.com");
    }

    @Test
    void findCareGiversWithFilters_WithCaseInsensitiveFilters_ShouldReturnMatchingResults() {
        // given
        CareGiver careGiver1 = createCareGiver("alice@example.com", "Alice", "1111111111111111", "Cardiology", "Hospital 1");
        CareGiver careGiver2 = createCareGiver("bob@example.com", "BOB", "2222222222222222", "cardiology", "Hospital 2");

        entityManager.persist(careGiver1);
        entityManager.persist(careGiver2);
        entityManager.flush();

        // when & then
        // Test case insensitive name filter
        List<CareGiver> resultName = careGiverRepository.findCareGiversWithFilters("alice", null);
        assertThat(resultName).hasSize(1);
        assertThat(resultName.get(0).getEmail()).isEqualTo("alice@example.com");

        // Test case insensitive speciality filter
        List<CareGiver> resultSpeciality = careGiverRepository.findCareGiversWithFilters(null, "CARDIOLOGY");
        assertThat(resultSpeciality).hasSize(2);
    }

    // Helper methods
    private CareGiver createCareGiver(String email, String name, String nik, String speciality, String workAddress) {
        CareGiver careGiver = new CareGiver();
        careGiver.setEmail(email);
        careGiver.setPassword("password");
        careGiver.setName(name);
        careGiver.setNik(nik);
        careGiver.setAddress("Address for " + name);
        careGiver.setPhoneNumber("08" + nik.substring(0, 10));
        careGiver.setSpeciality(speciality);
        careGiver.setWorkAddress(workAddress);
        return careGiver;
    }

    private CareGiver createCareGiverWithRating(String email, String name, String nik, String speciality, String workAddress, Double rating) {
        CareGiver careGiver = createCareGiver(email, name, nik, speciality, workAddress);
        careGiver.setAverageRating(rating);
        careGiver.setRatingCount(10); // Default rating count
        return careGiver;
    }
}