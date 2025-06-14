package org.example.prog_concu.Service;

import org.example.prog_concu.entities.Alert;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private final ConcurrentLinkedQueue<Alert> alertQueue = new ConcurrentLinkedQueue<>();

    public void addAlert(Alert alert) {
        alertQueue.offer(alert);
        System.out.println("🚨 Nouvelle alerte détectée : " + alert);
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

    public void acquitterAlerte(Alert alert) {
        alert.setActive(false);
        System.out.println("✅ Alerte acquittée: " + alert);
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
