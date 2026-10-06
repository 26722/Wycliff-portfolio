package kigali.clinic.rw.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.model.Appointment;
import kigali.clinic.rw.model.AppointmentStatus;
import kigali.clinic.rw.model.Doctor;
import kigali.clinic.rw.model.Patient;
import kigali.clinic.rw.model.Specialization;
import kigali.clinic.rw.dto.DoctorAppointmentCountDto;
import kigali.clinic.rw.dto.OverdueAppointmentDto;

// Runs against the real PostgreSQL database; @Transactional rolls every test back.
@SpringBootTest
@Transactional
class AppointmentRepositoryQueryTest {

    @Autowired
    private AppointmentRepository appointmentRepo;
    @Autowired
    private DoctorRepository doctorRepo;
    @Autowired
    private PatientRepository patientRepo;
    @Autowired
    private SpecializationRepository specializationRepo;

    private Doctor busyDoctor;
    private Doctor quietDoctor;
    private Patient patient;

    private static final LocalDate TODAY = LocalDate.now();

    @BeforeEach
    void seed() {
        Specialization cardiology = new Specialization();
        cardiology.setName("TestCardiology");
        specializationRepo.save(cardiology);

        busyDoctor = doctor("Busy", "Doctor");
        busyDoctor.getSpecializations().add(cardiology);
        doctorRepo.save(busyDoctor);
        quietDoctor = doctor("Quiet", "Doctor");

        patient = new Patient();
        patient.setFirstName("Test");
        patient.setLastName("Patient");
        patientRepo.save(patient);

        // busyDoctor: 2 overdue PENDING, 1 future PENDING, 9 SCHEDULED = 12 appointments
        appointment(busyDoctor, TODAY.minusDays(5), AppointmentStatus.PENDING);
        appointment(busyDoctor, TODAY.minusDays(10), AppointmentStatus.PENDING);
        appointment(busyDoctor, TODAY.plusDays(3), AppointmentStatus.PENDING);
        for (int i = 1; i <= 9; i++) {
            appointment(busyDoctor, TODAY.plusDays(10 + i), AppointmentStatus.SCHEDULED);
        }
        // quietDoctor: 1 appointment, past date but already completed, so not overdue
        appointment(quietDoctor, TODAY.minusDays(2), AppointmentStatus.COMPLETED);
    }

    @Test
    void derived_pendingAppointmentsOfDoctorOrderedByDate() {
        List<Appointment> pending = appointmentRepo
                .findByDoctorIdAndStatusOrderByAppointmentDateAsc(busyDoctor.getId(), AppointmentStatus.PENDING);

        assertEquals(3, pending.size());
        assertEquals(Date.valueOf(TODAY.minusDays(10)), pending.get(0).getAppointmentDate());
        assertEquals(Date.valueOf(TODAY.plusDays(3)), pending.get(2).getAppointmentDate());
    }

    @Test
    void jpql_overduePendingAppointmentsAsDto() {
        List<OverdueAppointmentDto> overdue = appointmentRepo.findOverdueByStatus(AppointmentStatus.PENDING).stream()
                .filter(dto -> dto.doctorName().equals("Busy Doctor"))
                .toList();

        assertEquals(2, overdue.size());
        assertEquals("Test Patient", overdue.get(0).patientName());
        assertEquals(Date.valueOf(TODAY.minusDays(10)), overdue.get(0).appointmentDate());
    }

    @Test
    void aggregate_appointmentsPerDoctorBusiestFirst() {
        List<DoctorAppointmentCountDto> counts = appointmentRepo.countAppointmentsPerDoctor();

        for (int i = 1; i < counts.size(); i++) {
            assertTrue(counts.get(i - 1).appointmentCount() >= counts.get(i).appointmentCount());
        }
        assertEquals(12L, countFor(counts, busyDoctor));
        assertEquals(1L, countFor(counts, quietDoctor));
    }

    @Test
    void join_appointmentsOfDoctorsWithSpecialization() {
        List<Appointment> appointments = appointmentRepo.findByDoctorSpecializationName("testcardiology");

        assertEquals(12, appointments.size());
        assertTrue(appointments.stream().allMatch(a -> a.getDoctor().getId().equals(busyDoctor.getId())));
    }

    @Test
    void pagination_firstPageOfDoctorAppointments() {
        Page<Appointment> page = appointmentRepo.findByDoctorId(busyDoctor.getId(),
                PageRequest.of(0, 10, Sort.by("appointmentDate").ascending()));

        assertEquals(10, page.getContent().size());
        assertEquals(12, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(Date.valueOf(TODAY.minusDays(10)), page.getContent().get(0).getAppointmentDate());
    }

    private Doctor doctor(String firstName, String lastName) {
        Doctor doctor = new Doctor();
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        return doctorRepo.save(doctor);
    }

    private void appointment(Doctor doctor, LocalDate date, AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(Date.valueOf(date));
        appointment.setStatus(status);
        appointment.setReason("Test");
        appointmentRepo.save(appointment);
    }

    private long countFor(List<DoctorAppointmentCountDto> counts, Doctor doctor) {
        return counts.stream()
                .filter(c -> c.doctorId().equals(doctor.getId()))
                .findFirst()
                .orElseThrow()
                .appointmentCount();
    }
}
