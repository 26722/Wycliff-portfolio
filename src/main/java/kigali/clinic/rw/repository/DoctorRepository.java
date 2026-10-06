package kigali.clinic.rw.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.model.Doctor;
import kigali.clinic.rw.model.Office;

@Repository 
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    List<Doctor> findByFirstName(String firstName);
    List<Doctor> findByLastName(String lastName);
    Optional<Doctor> findByOffice(Office office);
    boolean existsByOffice(Office office);

    // B1
    @Query("""
            SELECT d
            FROM Doctor d
            JOIN d.specializations s
            WHERE LOWER(s.name) = LOWER(:name)
            """)
    List<Doctor> findBySpecializationName(@Param("name") String name);

    // B2
    @Query("""
            SELECT d
            FROM Doctor d
            WHERE d.office IS NULL
            ORDER BY d.lastName
            """)
    List<Doctor> findDoctorsWithoutOffice();
}
