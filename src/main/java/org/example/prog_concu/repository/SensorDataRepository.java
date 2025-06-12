package org.example.prog_concu.repository;

import org.example.prog_concu.entities.SensorData;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SensorDataRepository {
    private final Map<UUID, SensorData> sensorDataStore = new ConcurrentHashMap<>();

    public void save(SensorData data) {
        sensorDataStore.put(UUID.randomUUID(), data);
    }

    public List<SensorData> findAll() {
        return new ArrayList<>(sensorDataStore.values());
    }

    public List<SensorData> findByPatientId(Long patientId) {
        return sensorDataStore.values().stream()
                .filter(data -> data.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<SensorData> findRecentByPatientId(Long patientId, int limit) {
        return sensorDataStore.values().stream()
                .filter(data -> data.getPatientId().equals(patientId))
                .sorted(Comparator.comparing(SensorData::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<SensorData> findBetweenDates(LocalDateTime start, LocalDateTime end) {
        return sensorDataStore.values().stream()
                .filter(data -> !data.getTimestamp().isBefore(start) && !data.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    public void clear() {
        sensorDataStore.clear();
    }

    public int count() {
        return sensorDataStore.size();
    }
}
