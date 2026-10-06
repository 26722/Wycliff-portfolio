package kigali.clinic.rw.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.model.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    List<Patient> findByFirstName(String firstName);
    List<Patient> findByLastName(String lastName);
    boolean existsByFirstNameAndLastNameAndDateOfBirth(String firstName, String lastName, Date dateOfBirth);
    Optional<Patient> findBySocialSecurityNumber(String socialSecurityNumber);

    // A1
    List<Patient> findByLastNameIgnoreCaseOrderByFirstNameAsc(String lastName);

    // B4: DISTINCT so a patient with several appointments is listed once.
    @Query("""
            SELECT DISTINCT a.patient
            FROM Appointment a
            WHERE a.doctor.id = :doctorId
            """)
    List<Patient> findPatientsOfDoctor(@Param("doctorId") UUID doctorId);

    // C2
    @Query("""
            SELECT p
            FROM Patient p
            JOIN p.appointments a
            GROUP BY p
            HAVING COUNT(a) >= :min
            ORDER BY COUNT(a) DESC
            """)
    List<Patient> findFrequentPatients(@Param("min") long min);
}
