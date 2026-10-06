package kigali.clinic.rw.dto;

import java.sql.Date;

public record OverdueAppointmentDto(String patientName, String doctorName, Date appointmentDate) {
}
