package kigali.clinic.rw.repository;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.model.Appointment;
import kigali.clinic.rw.model.AppointmentStatus;
import kigali.clinic.rw.model.Doctor;
import kigali.clinic.rw.model.Patient;
import kigali.clinic.rw.dto.BusiestOfficeDto;
import kigali.clinic.rw.dto.DoctorAppointmentCountDto;
import kigali.clinic.rw.dto.OverdueAppointmentDto;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByPatient(Patient patient);

    List<Appointment> findByPatientId(UUID patientId);

    List<Appointment> findByDoctor(Doctor doctor);

    List<Appointment> findByDoctorId(UUID doctorId);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByAppointmentDate(Date appointmentDate);

    List<Appointment> findByDoctorAndAppointmentDate(Doctor doctor, Date appointmentDate);

    boolean existsByDoctorIdAndAppointmentDate(UUID doctorId, Date appointmentDate);

    boolean existsByPatientIdAndDoctorIdAndAppointmentDate(UUID patientId, UUID doctorId, Date appointmentDate);

    // 1. Derived query: a doctor's appointments with a given status, oldest date first.
    // The date field is called appointmentDate, so the method says OrderByAppointmentDateAsc
    // (findByDoctorIdAndStatusOrderByDateAsc would fail at startup: Appointment has no "date" property).
    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateAsc(UUID doctorId, AppointmentStatus status);

    // 2. JPQL with a constructor expression: appointments still in the given status after their date has passed.
    @Query("""
            SELECT new kigali.clinic.rw.dto.OverdueAppointmentDto(
                CONCAT(p.firstName, ' ', p.lastName),
                CONCAT(d.firstName, ' ', d.lastName),
                a.appointmentDate)
            FROM Appointment a
            JOIN a.patient p
            JOIN a.doctor d
            WHERE a.status = :status
              AND a.appointmentDate < CURRENT_DATE
            ORDER BY a.appointmentDate
            """)
    List<OverdueAppointmentDto> findOverdueByStatus(@Param("status") AppointmentStatus status);

    // 3. Aggregate: appointments per doctor, busiest first. LEFT JOIN keeps doctors with zero appointments.
    @Query("""
            SELECT new kigali.clinic.rw.dto.DoctorAppointmentCountDto(
                d.id,
                CONCAT(d.firstName, ' ', d.lastName),
                COUNT(a))
            FROM Doctor d
            LEFT JOIN d.appointments a
            GROUP BY d.id, d.firstName, d.lastName
            ORDER BY COUNT(a) DESC
            """)
    List<DoctorAppointmentCountDto> countAppointmentsPerDoctor();

    // 4. JOIN across two relationships: Appointment -> doctor -> specializations.
    @Query("""
            SELECT DISTINCT a
            FROM Appointment a
            JOIN a.doctor d
            JOIN d.specializations s
            WHERE LOWER(s.name) = LOWER(:specializationName)
            ORDER BY a.appointmentDate
            """)
    List<Appointment> findByDoctorSpecializationName(@Param("specializationName") String specializationName);

    // 5. Pagination: Spring Data adds LIMIT/OFFSET and a separate COUNT query from the Pageable.
    Page<Appointment> findByDoctorId(UUID doctorId, Pageable pageable);

    // A2
    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    // A3: Between is inclusive on both ends.
    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(Date start, Date end);

    // A4: called with CANCELLED, so cancelled appointments don't block the date.
    boolean existsByDoctorIdAndAppointmentDateAndStatusNot(UUID doctorId, Date appointmentDate, AppointmentStatus status);

    // C1
    @Query("""
            SELECT a.status, COUNT(a)
            FROM Appointment a
            GROUP BY a.status
            """)
    List<Object[]> countByStatus();

    // C3: Appointment -> Doctor -> Office; the caller passes Limit.of(1) for the top row.
    @Query("""
            SELECT new kigali.clinic.rw.dto.BusiestOfficeDto(o.name, o.officeNumber, COUNT(a))
            FROM Appointment a
            JOIN a.doctor d
            JOIN d.office o
            GROUP BY o.id, o.name, o.officeNumber
            ORDER BY COUNT(a) DESC
            """)
    List<BusiestOfficeDto> findBusiestOffices(Limit limit);

    // C4: one UPDATE statement. Already COMPLETED or CANCELLED appointments are left out,
    // so the returned count is the number that actually changed.
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Appointment a
            SET a.status = :cancelled
            WHERE a.doctor.id = :doctorId
              AND a.appointmentDate = :date
              AND a.status NOT IN (:cancelled, :completed)
            """)
    int cancelDoctorDay(@Param("doctorId") UUID doctorId, @Param("date") Date date,
            @Param("cancelled") AppointmentStatus cancelled, @Param("completed") AppointmentStatus completed);
}
