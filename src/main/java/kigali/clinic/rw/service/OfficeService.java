package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.model.Office;
import kigali.clinic.rw.dto.BusiestOfficeDto;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.OfficeRepository;

@Service 
public class OfficeService {

    @Autowired 
    private OfficeRepository officeRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    public String saveOffice(Office office) {
        if (officeRepo.existsByOfficeNumber(office.getOfficeNumber())) {
            return "Error: Office with number " + office.getOfficeNumber() + " already exists";
        }
        if (office.getName() != null && officeRepo.existsByName(office.getName())) {
            return "Error: Office with name '" + office.getName() + "' already exists";
        }

        officeRepo.save(office);
        return "saved successfully";
    }

    public List<Office> getAllOffices() {
        return officeRepo.findAll();
    }

    public Optional<Office> getOfficeById(UUID id) {
        return officeRepo.findById(id);
    }

    public String updateOffice(UUID id, Office officeDetails) {
        Optional<Office> existingOffice = officeRepo.findById(id);
        if (existingOffice.isPresent()) {
            Optional<Office> officeWithSameNumber = officeRepo.findByOfficeNumber(officeDetails.getOfficeNumber());
            if (officeWithSameNumber.isPresent() && !officeWithSameNumber.get().getId().equals(id)) {
                return "Error: Office with number " + officeDetails.getOfficeNumber() + " already exists";
            }

            if (officeDetails.getName() != null) {
                List<Office> officesWithSameName = officeRepo.findByName(officeDetails.getName());
                for (Office o : officesWithSameName) {
                    if (!o.getId().equals(id)) {
                        return "Error: Office with name '" + officeDetails.getName() + "' already exists";
                    }
                }
            }

            Office office = existingOffice.get();
            office.setName(officeDetails.getName());
            office.setOfficeNumber(officeDetails.getOfficeNumber());
            officeRepo.save(office);
            return "Office with id " + id + " updated successfully";
        } else {
            return "we don't have office with id " + id;
        }
    }

    public String deleteOneOffice(int officeNumber) {
        Optional<Office> getOneOffice = officeRepo.findByOfficeNumber(officeNumber);

        if (getOneOffice.isPresent()) {
            officeRepo.deleteById(getOneOffice.get().getId());
            return "Office with this number " + officeNumber + " is deleted successfully";
        } else {
            return "we don't have office with that office Number " + officeNumber;
        }
    }

    public String deleteOfficeById(UUID id) {
        Optional<Office> getOneOffice = officeRepo.findById(id);

        if (getOneOffice.isPresent()) {
            officeRepo.deleteById(id);
            return "Office with id " + id + " is deleted successfully";
        } else {
            return "we don't have office with id " + id;
        }
    }

    // Empty Optional means there are no appointments yet.
    public Optional<BusiestOfficeDto> getBusiestOffice() {
        return appointmentRepo.findBusiestOffices(Limit.of(1)).stream().findFirst();
    }
}
