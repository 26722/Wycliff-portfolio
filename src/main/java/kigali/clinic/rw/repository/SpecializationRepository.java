package kigali.clinic.rw.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.model.Specialization;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {
    List<Specialization> findByName(String name);
    boolean existsByName(String name);

    // B3: IS EMPTY tests the doctors collection directly instead of counting.
    @Query("""
            SELECT s
            FROM Specialization s
            WHERE s.doctors IS EMPTY
            """)
    List<Specialization> findUnusedSpecializations();
}
