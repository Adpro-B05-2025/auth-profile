package id.ac.ui.cs.advprog.authprofile.repository;

import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface CareGiverRepository extends JpaRepository<CareGiver, Long> {
    Optional<CareGiver> findByEmail(String email);

    @Query("SELECT c FROM CareGiver c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<CareGiver> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT c FROM CareGiver c WHERE LOWER(c.speciality) LIKE LOWER(CONCAT('%', :speciality, '%'))")
    List<CareGiver> findBySpecialityContainingIgnoreCase(@Param("speciality") String speciality);

    @Query("SELECT DISTINCT c FROM CareGiver c JOIN c.workingSchedules ws WHERE ws.dayOfWeek = :dayOfWeek AND ws.isAvailable = true")
    List<CareGiver> findByAvailableDayOfWeek(@Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT c FROM CareGiver c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND " +
            "LOWER(c.speciality) LIKE LOWER(CONCAT('%', :speciality, '%'))")
    List<CareGiver> findByNameAndSpeciality(@Param("name") String name, @Param("speciality") String speciality);
}