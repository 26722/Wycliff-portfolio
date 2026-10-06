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

import kigali.clinic.rw.model.Office;
import kigali.clinic.rw.dto.BusiestOfficeDto;
import kigali.clinic.rw.service.OfficeService;

@RestController 
@RequestMapping (value = {"/api/v1/office", "/api/v1/offices"})
public class OfficeController {
  
    @Autowired 
    private OfficeService officeService;

    @PostMapping(value="/save", consumes = MediaType.APPLICATION_JSON_VALUE, 
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> saveOffice(@RequestBody Office office) {
        String returnedMessage = officeService.saveOffice(office);
        if (returnedMessage.startsWith("Error:")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.CREATED);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Office>> getAllOffices() {
        List<Office> offices = officeService.getAllOffices();
        return new ResponseEntity<>(offices, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOfficeById(@PathVariable UUID id) {
        Optional<Office> office = officeService.getOfficeById(id);
        if (office.isPresent()) {
            return new ResponseEntity<>(office.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Office not found with id " + id, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateOffice(@PathVariable UUID id, @RequestBody Office office) {
        String returnedMessage = officeService.updateOffice(id, office);
        if (returnedMessage.startsWith("Error:")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
        if (returnedMessage.startsWith("we don't have office")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteOfficeById(@PathVariable UUID id) {
        String returnedMessage = officeService.deleteOfficeById(id);
        if (returnedMessage.startsWith("we don't have office")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/number/{officeNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteOneOffice(@PathVariable int officeNumber) {
        String returnedMessage = officeService.deleteOneOffice(officeNumber);
        if (returnedMessage.startsWith("we don't have office")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
    }

    @GetMapping(value = "/busiest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBusiestOffice() {
        Optional<BusiestOfficeDto> office = officeService.getBusiestOffice();
        if (office.isPresent()) {
            return new ResponseEntity<>(office.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("No appointments yet", HttpStatus.OK);
    }
}
