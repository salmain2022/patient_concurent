package org.example.prog_concu.repository;

import org.springframework.stereotype.Repository;
import org.example.prog_concu.entities.Alert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class AlertRepository {
    private final Map<UUID, Alert> alerts = new ConcurrentHashMap<>();

    public List<Alert> findAll() {
        return new ArrayList<>(alerts.values());
    }

    public List<Alert> findByPatientId(Long patientId) {
        return alerts.values().stream()
                .filter(alert -> alert.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<Alert> findActiveAlerts() {
        return alerts.values().stream()
                .filter(Alert::isActive)
                .collect(Collectors.toList());
    }

    public void save(Alert alert) {
        // Utilisation d'un UUID pour clé unique
        alerts.put(UUID.randomUUID(), alert);
    }

    public void clear() {
        alerts.clear();
    }

    public int count() {
        return alerts.size();
    }
}