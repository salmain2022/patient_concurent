package org.example.prog_concu.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.prog_concu.entities.SensorData;
import org.example.prog_concu.simulator.SensorSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class MonitoringManager {
    private final ConcurrentHashMap<Long, SensorData> sensorDataMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> activeSimulations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    // Injectez le contexte Spring pour créer des instances de SensorSimulator
    private final ApplicationContext applicationContext;

    @Autowired
    public MonitoringManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ MonitoringManager initialized");
    }

    public void startMonitoring(Long patientId) {
        if (activeSimulations.containsKey(patientId)) {
            System.out.println("⚠️ Surveillance déjà active pour le patient: " + patientId);
            return;
        }

        // Créez le simulateur via Spring pour une meilleure gestion des dépendances
        SensorSimulator simulator = applicationContext.getBean(
                SensorSimulator.class,
                patientId,
                3000,  // Intervalle de 3 secondes
                sensorDataMap
        );

        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(
                simulator,
                0,
                3,
                TimeUnit.SECONDS
        );

        activeSimulations.put(patientId, future);
        System.out.println("🚀 Surveillance démarrée pour le patient: " + patientId);
    }

    public void stopMonitoring(Long patientId) {
        ScheduledFuture<?> future = activeSimulations.remove(patientId);
        if (future != null) {
            future.cancel(true);
            sensorDataMap.remove(patientId);
            System.out.println("🛑 Surveillance arrêtée pour le patient: " + patientId);
        }
    }

    public SensorData getLatestSensorData(Long patientId) {
        return sensorDataMap.get(patientId);
    }

    public ConcurrentHashMap<Long, SensorData> getAllSensorData() {
        return new ConcurrentHashMap<>(sensorDataMap);
    }

    @PreDestroy
    public void cleanUp() {
        // Arrêt propre de tous les threads
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("🔴 MonitoringManager arrêté");
    }
}