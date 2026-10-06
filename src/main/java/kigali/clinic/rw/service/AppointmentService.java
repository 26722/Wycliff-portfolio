package kigali.clinic.rw.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.model.Appointment;
import kigali.clinic.rw.model.AppointmentStatus;
import kigali.clinic.rw.model.Doctor;
import kigali.clinic.rw.model.Patient;
import kigali.clinic.rw.dto.DoctorAppointmentCountDto;
import kigali.clinic.rw.dto.OverdueAppointmentDto;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class AppointmentService {

    public static final String DOCTOR_ALREADY_BOOKED = "Doctor is already booked on that date";

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    public String saveAppointment(Appointment appointment) {
        if (appointment.getAppointmentDate() == null) {
            return "Error: Appointment date is required";
        }

        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            return "Error: Patient is required";
        }
        Optional<Patient> patientOpt = patientRepo.findById(appointment.getPatient().getId());
        if (patientOpt.isEmpty()) {
            return "Error: Patient not found with id " + appointment.getPatient().getId();
        }
        appointment.setPatient(patientOpt.get());

        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return "Error: Doctor is required";
        }
        Optional<Doctor> doctorOpt = doctorRepo.findById(appointment.getDoctor().getId());
        if (doctorOpt.isEmpty()) {
            return "Error: Doctor not found with id " + appointment.getDoctor().getId();
        }
        appointment.setDoctor(doctorOpt.get());

        if (appointmentRepo.existsByDoctorIdAndAppointmentDateAndStatusNot(
                doctorOpt.get().getId(), appointment.getAppointmentDate(), AppointmentStatus.CANCELLED)) {
            return DOCTOR_ALREADY_BOOKED;
        }

        // Check for existing duplicate appointment
        if (appointmentRepo.existsByPatientIdAndDoctorIdAndAppointmentDate(
                patientOpt.get().getId(), doctorOpt.get().getId(), appointment.getAppointmentDate())) {
            return "Error: Appointment already exists for this patient and doctor on " + appointment.getAppointmentDate();
        }

        if (appointment.getStatus() == null) {
            appointment.setStatus(AppointmentStatus.SCHEDULED);
        }

        appointmentRepo.save(appointment);
        return "Appointment saved successfully";
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    public Optional<Appointment> getAppointmentById(UUID id) {
        return appointmentRepo.findById(id);
    }

    public List<Appointment> getAppointmentsByDoctorId(UUID doctorId) {
        return appointmentRepo.findByDoctorId(doctorId);
    }

    public List<Appointment> getAppointmentsByPatientId(UUID patientId) {
        return appointmentRepo.findByPatientId(patientId);
    }
    public String updateAppointment(UUID id, Appointment appointmentDetails) {
        Optional<Appointment> existingAppointment = appointmentRepo.findById(id);

        if (existingAppointment.isPresent()) {
            Appointment appointment = existingAppointment.get();

            if (appointmentDetails.getAppointmentDate() != null) {
                appointment.setAppointmentDate(appointmentDetails.getAppointmentDate());
            }

            if (appointmentDetails.getReason() != null) {
                appointment.setReason(appointmentDetails.getReason());
            }

            if (appointmentDetails.getStatus() != null) {
                appointment.setStatus(appointmentDetails.getStatus());
            }

            if (appointmentDetails.getDoctor() != null && appointmentDetails.getDoctor().getId() != null) {
                Optional<Doctor> doc = doctorRepo.findById(appointmentDetails.getDoctor().getId());
                if (doc.isEmpty()) {
                    return "Error: Doctor not found with id " + appointmentDetails.getDoctor().getId();
                }
                appointment.setDoctor(doc.get());
            }

            if (appointmentDetails.getPatient() != null && appointmentDetails.getPatient().getId() != null) {
                Optional<Patient> pat = patientRepo.findById(appointmentDetails.getPatient().getId());
                if (pat.isEmpty()) {
                    return "Error: Patient not found with id " + appointmentDetails.getPatient().getId();
                }
                appointment.setPatient(pat.get());
            }

            appointmentRepo.save(appointment);
            return "Appointment with id " + id + " is updated successfully";
        } else {
            return "We don't have appointment with id " + id;
        }
    }

    public String cancelAppointment(UUID id) {
        Optional<Appointment> existingAppointment = appointmentRepo.findById(id);

        if (existingAppointment.isPresent()) {
            Appointment appointment = existingAppointment.get();
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepo.save(appointment);
            return "Appointment with id " + id + " has been cancelled";
        } else {
            return "We don't have appointment with id " + id;
        }
    }

    public String deleteAppointment(UUID id) {
        Optional<Appointment> existingAppointment = appointmentRepo.findById(id);

        if (existingAppointment.isPresent()) {
            appointmentRepo.deleteById(id);
            return "Appointment with id " + id + " is deleted successfully";
        } else {
            return "We don't have appointment with that id " + id;
        }
    }

    public List<Appointment> getPendingAppointmentsOfDoctor(UUID doctorId) {
        return appointmentRepo.findByDoctorIdAndStatusOrderByAppointmentDateAsc(doctorId, AppointmentStatus.PENDING);
    }

    public List<OverdueAppointmentDto> getOverduePendingAppointments() {
        return appointmentRepo.findOverdueByStatus(AppointmentStatus.PENDING);
    }

    public List<DoctorAppointmentCountDto> countAppointmentsPerDoctor() {
        return appointmentRepo.countAppointmentsPerDoctor();
    }

    public List<Appointment> getAppointmentsBySpecialization(String specializationName) {
        return appointmentRepo.findByDoctorSpecializationName(specializationName);
    }

    // pageNumber is 1-based here; Spring Data's PageRequest is 0-based.
    public Page<Appointment> getDoctorAppointmentsPage(UUID doctorId, int pageNumber, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize, Sort.by("appointmentDate").ascending());
        return appointmentRepo.findByDoctorId(doctorId, pageRequest);
    }

    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepo.findByStatusOrderByAppointmentDateAsc(status);
    }

    public List<Appointment> getAppointmentsBetween(LocalDate start, LocalDate end) {
        return appointmentRepo.findByAppointmentDateBetweenOrderByAppointmentDateAsc(Date.valueOf(start), Date.valueOf(end));
    }

    public List<Object[]> countAppointmentsByStatus() {
        return appointmentRepo.countByStatus();
    }

    @Transactional
    public String cancelDoctorDay(UUID doctorId, LocalDate date) {
        int cancelled = appointmentRepo.cancelDoctorDay(doctorId, Date.valueOf(date),
                AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED);
        return cancelled + (cancelled == 1 ? " appointment" : " appointments") + " cancelled";
    }
}
