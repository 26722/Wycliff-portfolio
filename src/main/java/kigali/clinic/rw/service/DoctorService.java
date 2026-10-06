package kigali.clinic.rw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.model.Doctor;
import kigali.clinic.rw.model.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class DoctorService {

    
    @Autowired
    private DoctorRepository doctorRepo;
    @Autowired
    private SpecializationRepository specializationRepo;

    @Transactional
    public String saveDoctor(Doctor doctor) {
        List<Specialization> specializations = resolveSpecializations(doctor.getSpecializations());
        if (specializations == null) {
            return "Error: One or more specializations were not found";
        }
        doctor.setSpecializations(new ArrayList<>());
        doctorRepo.save(doctor);
        doctor.setSpecializations(specializations);
        doctorRepo.save(doctor);
        return "Doctor saved successfully";
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    public Optional<Doctor> getDoctorById(UUID id) {
        return doctorRepo.findById(id);
    }

    @Transactional
    public String updateDoctor(UUID id, Doctor doctorDetails) {
        Optional<Doctor> existingDoctor = doctorRepo.findById(id);

        if (existingDoctor.isPresent()) {
            List<Specialization> specializations = null;
            if (doctorDetails.getSpecializations() != null) {
                specializations = resolveSpecializations(doctorDetails.getSpecializations());
                if (specializations == null) {
                    return "Error: One or more specializations were not found";
                }
            }

            Doctor doctor = existingDoctor.get();
            doctor.setFirstName(doctorDetails.getFirstName());
            doctor.setLastName(doctorDetails.getLastName());
            doctor.setDateOfBirth(doctorDetails.getDateOfBirth());
            if (doctorDetails.getOffice() != null) {
                doctor.setOffice(doctorDetails.getOffice());
            }
            if (specializations != null) {
                doctor.setSpecializations(specializations);
            }
            doctorRepo.save(doctor);
            return "Doctor with id " + id + " is updated successfully";
        } else {
            return "We don't have doctor with id " + id;
        }
    }

    public String deleteDoctor(UUID id) {
        Optional<Doctor> existingDoctor = doctorRepo.findById(id);

        if (existingDoctor.isPresent()) {
            doctorRepo.deleteById(id);
            return "Doctor with id " + id + " is deleted successfully";
        } else {
            return "We don't have doctor with that id " + id;
        }
    }

    @Transactional
    public String addSpecialization(UUID doctorId, UUID specializationId) {
        Optional<Doctor> doctor = doctorRepo.findById(doctorId);
        if (doctor.isEmpty()) {
            return "We don't have doctor with id " + doctorId;
        }
        Optional<Specialization> specialization = specializationRepo.findById(specializationId);
        if (specialization.isEmpty()) {
            return "We don't have specialization with id " + specializationId;
        }
        List<Specialization> specializations = doctor.get().getSpecializations();
        if (specializations.stream().anyMatch(existing -> existing.getId().equals(specializationId))) {
            return "Error: Doctor already has specialization " + specialization.get().getName();
        }
        specializations.add(specialization.get());
        doctorRepo.save(doctor.get());
        return "Specialization " + specialization.get().getName() + " added to doctor with id " + doctorId;
    }

    @Transactional
    public String removeSpecialization(UUID doctorId, UUID specializationId) {
        Optional<Doctor> doctor = doctorRepo.findById(doctorId);
        if (doctor.isEmpty()) {
            return "We don't have doctor with id " + doctorId;
        }
        boolean removed = doctor.get().getSpecializations()
                .removeIf(existing -> existing.getId().equals(specializationId));
        if (!removed) {
            return "We don't have specialization with id " + specializationId + " on doctor with id " + doctorId;
        }
        doctorRepo.save(doctor.get());
        return "Specialization with id " + specializationId + " removed from doctor with id " + doctorId;
    }

    @Transactional(readOnly = true)
    public Optional<List<Specialization>> getSpecializations(UUID doctorId) {
        return doctorRepo.findById(doctorId).map(doctor -> new ArrayList<>(doctor.getSpecializations()));
    }

    private List<Specialization> resolveSpecializations(List<Specialization> requestedSpecializations) {
        List<Specialization> specializations = new ArrayList<>();
        if (requestedSpecializations == null) {
            return specializations;
        }
        for (Specialization requested : requestedSpecializations) {
            if (requested == null || requested.getId() == null) {
                return null;
            }
            Optional<Specialization> specialization = specializationRepo.findById(requested.getId());
            if (specialization.isEmpty()) {
                return null;
            }
            if (specializations.stream()
                    .noneMatch(existing -> existing.getId().equals(specialization.get().getId()))) {
                specializations.add(specialization.get());
            }
        }
        return specializations;
    }

    public List<Doctor> getDoctorsBySpecializationName(String name) {
        return doctorRepo.findBySpecializationName(name);
    }

    public List<Doctor> getDoctorsWithoutOffice() {
        return doctorRepo.findDoctorsWithoutOffice();
    }
}
