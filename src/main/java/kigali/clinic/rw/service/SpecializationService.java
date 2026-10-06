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
public class SpecializationService {

    @Autowired
    private SpecializationRepository specializationRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Transactional
    public String saveSpecialization(Specialization specialization) {
        if (specialization.getName() == null || specialization.getName().isBlank()) {
            return "Error: Specialization name is required";
        }
        if (specializationRepo.existsByName(specialization.getName())) {
            return "Error: Specialization with name '" + specialization.getName() + "' already exists";
        }

        List<Doctor> doctors = resolveDoctors(specialization.getDoctors());
        if (doctors == null) {
            return "Error: One or more doctors were not found";
        }
        specializationRepo.save(specialization);
        for (Doctor doctor : doctors) {
            doctor.getSpecializations().add(specialization);
            doctorRepo.save(doctor);
        }
        specialization.setDoctors(doctors);
        return "Specialization saved successfully";
    }

    public List<Specialization> getAllSpecializations() {
        return specializationRepo.findAll();
    }

    public Optional<Specialization> getSpecializationById(UUID id) {
        return specializationRepo.findById(id);
    }

    @Transactional
    public String updateSpecialization(UUID id, Specialization specializationDetails) {
        Optional<Specialization> existingSpecialization = specializationRepo.findById(id);
        if (existingSpecialization.isEmpty()) {
            return "We don't have specialization with id " + id;
        }
        if (specializationDetails.getName() == null || specializationDetails.getName().isBlank()) {
            return "Error: Specialization name is required";
        }

        for (Specialization sameName : specializationRepo.findByName(specializationDetails.getName())) {
            if (!sameName.getId().equals(id)) {
                return "Error: Specialization with name '" + specializationDetails.getName() + "' already exists";
            }
        }

        Specialization specialization = existingSpecialization.get();
        if (specializationDetails.getDoctors() != null) {
            List<Doctor> doctors = resolveDoctors(specializationDetails.getDoctors());
            if (doctors == null) {
                return "Error: One or more doctors were not found";
            }
            for (Doctor previousDoctor : specialization.getDoctors()) {
                if (doctors.stream().noneMatch(doctor -> doctor.getId().equals(previousDoctor.getId()))) {
                    previousDoctor.getSpecializations()
                            .removeIf(existing -> existing.getId().equals(specialization.getId()));
                    doctorRepo.save(previousDoctor);
                }
            }
            for (Doctor doctor : doctors) {
                if (doctor.getSpecializations().stream()
                        .noneMatch(existing -> existing.getId().equals(specialization.getId()))) {
                    doctor.getSpecializations().add(specialization);
                    doctorRepo.save(doctor);
                }
            }
            specialization.setDoctors(doctors);
        }
        specialization.setName(specializationDetails.getName());
        specializationRepo.save(specialization);
        return "Specialization with id " + id + " is updated successfully";
    }

    @Transactional
    public String deleteSpecialization(UUID id) {
        Optional<Specialization> specialization = specializationRepo.findById(id);
        if (specialization.isEmpty()) {
            return "We don't have specialization with id " + id;
        }
        for (Doctor doctor : specialization.get().getDoctors()) {
            doctor.getSpecializations().removeIf(existing -> existing.getId().equals(id));
            doctorRepo.save(doctor);
        }
        specializationRepo.delete(specialization.get());
        return "Specialization with id " + id + " is deleted successfully";
    }

    @Transactional(readOnly = true)
    public Optional<List<Doctor>> getDoctors(UUID specializationId) {
        return specializationRepo.findById(specializationId).map(specialization -> new ArrayList<>(specialization.getDoctors()));
    }

    private List<Doctor> resolveDoctors(List<Doctor> requestedDoctors) {
        List<Doctor> doctors = new ArrayList<>();
        if (requestedDoctors == null) {
            return doctors;
        }
        for (Doctor requestedDoctor : requestedDoctors) {
            if (requestedDoctor == null || requestedDoctor.getId() == null) {
                return null;
            }
            Optional<Doctor> doctor = doctorRepo.findById(requestedDoctor.getId());
            if (doctor.isEmpty()) {
                return null;
            }
            if (doctors.stream().noneMatch(existing -> existing.getId().equals(doctor.get().getId()))) {
                doctors.add(doctor.get());
            }
        }
        return doctors;
    }

    public List<Specialization> getUnusedSpecializations() {
        return specializationRepo.findUnusedSpecializations();
    }
}
