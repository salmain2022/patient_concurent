package org.example.prog_concu.Controller;

import org.example.prog_concu.Service.PatientService;
import org.example.prog_concu.entities.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public String listPatients(Model model) {
        model.addAttribute("patients", patientService.findAll());
        model.addAttribute("patient", new Patient()); // Pour le formulaire intégré
        return "patients/list";
    }

    @GetMapping("/add")
    public String showAddPatientForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "patients/add";
    }

    @PostMapping("/add")
    public String addPatient(@ModelAttribute Patient patient, RedirectAttributes redirectAttributes) {
        patientService.save(patient);
        redirectAttributes.addFlashAttribute("successMessage", "Patient ajouté avec succès!");
        return "redirect:/patients";
    }
    
    @PostMapping
    public String addPatientFromList(@ModelAttribute Patient patient, RedirectAttributes redirectAttributes) {
        return addPatient(patient, redirectAttributes);
    }
}