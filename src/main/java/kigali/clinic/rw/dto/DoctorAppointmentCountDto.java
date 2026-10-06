package kigali.clinic.rw.dto;

import java.util.UUID;

public record DoctorAppointmentCountDto(UUID doctorId, String doctorName, Long appointmentCount) {
}
