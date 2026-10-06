package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.model.Patient;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    public String savePatient(Patient patient) {
        patientRepo.save(patient);
        return "Patient saved successfully";
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    public Optional<Patient> getPatientById(UUID id) {
        return patientRepo.findById(id);
    }

    public Optional<Patient> getBySsNumber(String socialSecurityNumber) {
        return patientRepo.findBySocialSecurityNumber(socialSecurityNumber);
    }

    public String updatePatient(UUID id, Patient patientDetails) {
        Optional<Patient> existingPatient = patientRepo.findById(id);

        if (existingPatient.isPresent()) {
            Patient patient = existingPatient.get();
            patient.setFirstName(patientDetails.getFirstName());
            patient.setLastName(patientDetails.getLastName());
            patient.setDateOfBirth(patientDetails.getDateOfBirth());
            if (patientDetails.getSocialSecurityNumber() != null) {
                patient.setSocialSecurityNumber(patientDetails.getSocialSecurityNumber());
            }
            patientRepo.save(patient);
            return "Patient with id " + id + " is updated successfully";
        } else {
            return "We don't have patient with id " + id;
        }
    }

    public String deletePatient(UUID id) {
        Optional<Patient> existingPatient = patientRepo.findById(id);

        if (existingPatient.isPresent()) {
            patientRepo.deleteById(id);
            return "Patient with id " + id + " is deleted successfully";
        } else {
            return "We don't have patient with that id " + id;
        }
    }

    public List<Patient> getPatientsByLastName(String lastName) {
        return patientRepo.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    // Empty Optional means the doctor does not exist.
    public Optional<List<Patient>> getPatientsOfDoctor(UUID doctorId) {
        if (!doctorRepo.existsById(doctorId)) {
            return Optional.empty();
        }
        return Optional.of(patientRepo.findPatientsOfDoctor(doctorId));
    }

    public List<Patient> getFrequentPatients(long min) {
        return patientRepo.findFrequentPatients(min);
    }
}
