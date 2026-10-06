package kigali.clinic.rw.web;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.model.Appointment;
import kigali.clinic.rw.dto.DoctorAppointmentCountDto;
import kigali.clinic.rw.dto.OverdueAppointmentDto;
import kigali.clinic.rw.service.AppointmentService;

// Temporary endpoints for Exercise 4: call each query and read the generated SQL in the console.
@RestController
@RequestMapping(value = "/api/v1/query/appointment")
public class AppointmentQueryController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping(value = "/doctor/{doctorId}/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Appointment>> getPendingAppointmentsOfDoctor(@PathVariable UUID doctorId) {
        return new ResponseEntity<>(appointmentService.getPendingAppointmentsOfDoctor(doctorId), HttpStatus.OK);
    }

    @GetMapping(value = "/overdue", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OverdueAppointmentDto>> getOverduePendingAppointments() {
        return new ResponseEntity<>(appointmentService.getOverduePendingAppointments(), HttpStatus.OK);
    }

    @GetMapping(value = "/count-per-doctor", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DoctorAppointmentCountDto>> countAppointmentsPerDoctor() {
        return new ResponseEntity<>(appointmentService.countAppointmentsPerDoctor(), HttpStatus.OK);
    }

    @GetMapping(value = "/specialization/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Appointment>> getAppointmentsBySpecialization(@PathVariable String name) {
        return new ResponseEntity<>(appointmentService.getAppointmentsBySpecialization(name), HttpStatus.OK);
    }

    @GetMapping(value = "/doctor/{doctorId}/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDoctorAppointmentsPage(@PathVariable UUID doctorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 1 || size < 1) {
            return new ResponseEntity<>("Error: page and size must be at least 1", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new PagedModel<>(appointmentService.getDoctorAppointmentsPage(doctorId, page, size)),
                HttpStatus.OK);
    }
}
