package id.ac.ui.cs.advprog.authprofile.repository;

import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacillianRepository extends JpaRepository<Pacillian, Long> {
    Optional<Pacillian> findByEmail(String email);
}

