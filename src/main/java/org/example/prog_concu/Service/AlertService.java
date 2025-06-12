package org.example.prog_concu.Service;



import org.example.prog_concu.entities.Alert;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
public class AlertService {
    // Stockage thread-safe des alertes
    private final ConcurrentLinkedQueue<Alert> alertQueue = new ConcurrentLinkedQueue<>();

    /**
     * Ajoute une nouvelle alerte à la file
     * @param alert Alerte à ajouter
     */
    public void addAlert(Alert alert) {
        alertQueue.offer(alert);
        System.out.println("🚨 Alerte ajoutée: " + alert);
    }

    /**
     * Récupère toutes les alertes actives
     * @return Liste des alertes actives
     */
    public List<Alert> getActiveAlerts() {
        return alertQueue.stream()
                .filter(Alert::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les alertes (pour débogage)
     * @return Liste complète des alertes
     */
    public List<Alert> getAllAlerts() {
        return List.copyOf(alertQueue);
    }
}