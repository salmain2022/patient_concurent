package org.example.prog_concu.Service;

import org.example.prog_concu.entities.Alert;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private final ConcurrentLinkedQueue<Alert> alertQueue = new ConcurrentLinkedQueue<>();
    private final AtomicLong idCounter = new AtomicLong(1); // Pour générer des IDs uniques

    public void addAlert(Alert alert) {
        // Vérifie si une alerte similaire existe déjà
        boolean alertExists = isAlertActive(alert.getPatientId(), alert.getSigneVital());
        
        if (!alertExists) {
            alert.setId(idCounter.getAndIncrement()); // Attribution d'un ID
            alert.setActive(true);
            alertQueue.offer(alert);
            System.out.println("🚨 Nouvelle alerte détectée : " + alert);
        }
    }

    public List<Alert> getActiveAlerts() {
        return alertQueue.stream()
                .filter(Alert::isActive)
                .collect(Collectors.toList());
    }

    public List<Alert> getAllAlerts() {
        return List.copyOf(alertQueue);
    }

    public boolean isAlertActive(Long patientId, String signeVital) {
        return alertQueue.stream()
                .anyMatch(alert ->
                        alert.isActive() &&
                        alert.getPatientId().equals(patientId) &&
                        alert.getSigneVital().equalsIgnoreCase(signeVital)
                );
    }

    public void acquitterAlerte(Long alertId) {
        alertQueue.stream()
                .filter(alert -> alert.getId().equals(alertId))
                .findFirst()
                .ifPresent(alert -> {
                    alert.setActive(false);
                    System.out.println("✅ Alerte acquittée: " + alert);
                });
    }

    public void acquitterAlertesByPatient(Long patientId) {
        alertQueue.stream()
                .filter(alert -> alert.isActive() && alert.getPatientId().equals(patientId))
                .forEach(alert -> {
                    alert.setActive(false);
                    System.out.println("✅ Alerte acquittée: " + alert);
                });
    }
}