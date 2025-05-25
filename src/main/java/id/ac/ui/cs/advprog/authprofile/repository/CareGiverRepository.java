// Add these methods to your existing CareGiverRepository interface

package id.ac.ui.cs.advprog.authprofile.repository;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareGiverRepository extends JpaRepository<CareGiver, Long> {

    // Existing methods...
    Optional<CareGiver> findByEmail(String email);

    @Query("SELECT c FROM CareGiver c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<CareGiver> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT c FROM CareGiver c WHERE LOWER(c.speciality) LIKE LOWER(CONCAT('%', :speciality, '%'))")
    List<CareGiver> findBySpecialityContainingIgnoreCase(@Param("speciality") String speciality);

    @Query("SELECT c FROM CareGiver c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND " +
            "LOWER(c.speciality) LIKE LOWER(CONCAT('%', :speciality, '%'))")
    List<CareGiver> findByNameAndSpeciality(@Param("name") String name, @Param("speciality") String speciality);

    @Query("SELECT c FROM CareGiver c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND " +
            "LOWER(c.speciality) LIKE LOWER(CONCAT('%', :speciality, '%'))")
    Page<CareGiver> findByNameContainingIgnoreCaseAndSpecialityContainingIgnoreCase(
            @Param("name") String name,
            @Param("speciality") String speciality,
            Pageable pageable);

    @Query("SELECT c FROM CareGiver c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<CareGiver> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT c FROM CareGiver c WHERE LOWER(c.speciality) LIKE LOWER(CONCAT('%', :speciality, '%'))")
    Page<CareGiver> findBySpecialityContainingIgnoreCase(@Param("speciality") String speciality, Pageable pageable);

    /**
     * Find caregivers with filters (non-paginated version)
     * This method handles all combinations of name and speciality filters
     */
    @Query("SELECT c FROM CareGiver c WHERE " +
            "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:speciality IS NULL OR LOWER(c.speciality) LIKE LOWER(CONCAT('%', :speciality, '%')))")
    List<CareGiver> findCareGiversWithFilters(
            @Param("name") String name,
            @Param("speciality") String speciality);

    /**
     * Get name suggestions for autocomplete (limit results for performance)
     */
    @Query("SELECT DISTINCT c.name FROM CareGiver c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :prefix, '%')) " +
            "ORDER BY c.name")
    List<String> findNameSuggestions(@Param("prefix") String prefix);

    /**
     * Get speciality suggestions for autocomplete (limit results for performance)
     */
    @Query("SELECT DISTINCT c.speciality FROM CareGiver c WHERE " +
            "LOWER(c.speciality) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY c.speciality")
    List<String> findSpecialitySuggestions(@Param("query") String query);


}