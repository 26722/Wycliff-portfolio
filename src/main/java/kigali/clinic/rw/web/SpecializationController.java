package kigali.clinic.rw.web;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.model.Doctor;
import kigali.clinic.rw.model.Specialization;
import kigali.clinic.rw.service.SpecializationService;

@RestController
@RequestMapping(value = {"/api/v1/specialization", "/api/v1/specializations"})
public class SpecializationController {

    @Autowired
    private SpecializationService specializationService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> saveSpecialization(@RequestBody Specialization specialization) {
        String returnedMessage = specializationService.saveSpecialization(specialization);
        if (returnedMessage.startsWith("Error:")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.CREATED);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        return new ResponseEntity<>(specializationService.getAllSpecializations(), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSpecializationById(@PathVariable UUID id) {
        Optional<Specialization> specialization = specializationService.getSpecializationById(id);
        if (specialization.isPresent()) {
            return new ResponseEntity<>(specialization.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Specialization not found with id " + id, HttpStatus.NOT_FOUND);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateSpecialization(@PathVariable UUID id,
            @RequestBody Specialization specialization) {
        String returnedMessage = specializationService.updateSpecialization(id, specialization);
        if (returnedMessage.startsWith("Error:")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.BAD_REQUEST);
        }
        if (returnedMessage.startsWith("We don't have specialization")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteSpecialization(@PathVariable UUID id) {
        String returnedMessage = specializationService.deleteSpecialization(id);
        if (returnedMessage.startsWith("We don't have specialization")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}/doctors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDoctors(@PathVariable UUID id) {
        Optional<List<Doctor>> doctors = specializationService.getDoctors(id);
        if (doctors.isPresent()) {
            return new ResponseEntity<>(doctors.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Specialization not found with id " + id, HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/unused", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Specialization>> getUnusedSpecializations() {
        return new ResponseEntity<>(specializationService.getUnusedSpecializations(), HttpStatus.OK);
    }
}
