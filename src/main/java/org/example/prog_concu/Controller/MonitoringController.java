package org.example.prog_concu.Controller;

import java.util.HashMap;
import java.util.Map;

import org.example.prog_concu.Service.AlertService;
import org.example.prog_concu.Service.MonitoringManager;
import org.example.prog_concu.entities.SensorData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;



@Controller
@RequestMapping("/monitoring")
public class MonitoringController {

    private final MonitoringManager monitoringManager;
    private final AlertService alertService;

    public MonitoringController(MonitoringManager monitoringManager, AlertService alertService) {
        this.monitoringManager = monitoringManager;
        this.alertService = alertService;
    }

    

    @PostMapping("/start")
public String startMonitoring(@RequestParam Long patientId, RedirectAttributes redirectAttributes) {
    System.out.println("=== DEMARRAGE SURVEILLANCE ===");
    System.out.println("Patient ID reçu: " + patientId);
    
    try {
        monitoringManager.startMonitoring(patientId);
        System.out.println("Surveillance démarrée avec succès");
        redirectAttributes.addFlashAttribute("success", "Surveillance démarrée pour le patient " + patientId);
    } catch (Exception e) {
        System.err.println("ERREUR: " + e.getMessage());
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
    }
    return "redirect:/monitoring";
}

    @PostMapping("/stop")
    public String stopMonitoring(@RequestParam Long patientId, RedirectAttributes redirectAttributes) {
        try {
            monitoringManager.stopMonitoring(patientId);
            redirectAttributes.addFlashAttribute("success", "Surveillance arrêtée pour le patient " + patientId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/monitoring";
    }

    @PostMapping("/acknowledge")
    public String acknowledgeAlert(@RequestParam Long alertId, RedirectAttributes redirectAttributes) {
        try {
            alertService.acquitterAlerte(alertId);
            redirectAttributes.addFlashAttribute("success", "Alerte acquittée");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'acquittement: " + e.getMessage());
        }
        return "redirect:/monitoring";
    }
}