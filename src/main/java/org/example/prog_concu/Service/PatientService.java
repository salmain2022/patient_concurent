package org.example.prog_concu.Service;

import org.example.prog_concu.entities.Patient;
import org.example.prog_concu.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    // Injection via constructeur
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // Sauvegarder un patient
    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }

    // Récupérer tous les patients
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    // Trouver un patient par son id
    public Optional<Patient> findById(Long id) {
        return patientRepository.findById(id);
    }
}
