package org.example.prog_concu.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.prog_concu.entities.SensorData;
import org.example.prog_concu.repository.SensorDataRepository;
import org.example.prog_concu.repository.AlertRepository;
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

    // Ajout pour MonitorTask
    private MonitorTask monitorTask;
    private ScheduledFuture<?> monitoringTask;

    private final ApplicationContext applicationContext;
    private final SensorDataRepository sensorDataRepository;
    private final AlertRepository alertRepository;

    @Autowired
    public MonitoringManager(ApplicationContext applicationContext, 
                           SensorDataRepository sensorDataRepository,
                           AlertRepository alertRepository) {
        this.applicationContext = applicationContext;
        this.sensorDataRepository = sensorDataRepository;
        this.alertRepository = alertRepository;
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ MonitoringManager initialized");
        
        // Initialiser MonitorTask avec la map partagée
        monitorTask = new MonitorTask(sensorDataMap);
        
        // Injecter manuellement AlertRepository (car MonitorTask est créé manuellement)
        monitorTask.alertRepository = alertRepository;
        
        // Démarrer la surveillance automatique - MonitorTask s'exécute toutes les 3 secondes
        monitoringTask = executorService.scheduleAtFixedRate(
            () -> {
                try {
                    monitorTask.run();
                } catch (Exception e) {
                    System.err.println("❌ Erreur dans MonitorTask: " + e.getMessage());
                    e.printStackTrace();
                }
            }, 
            5, // délai initial de 5 secondes
            3, // intervalle de 3 secondes
            TimeUnit.SECONDS
        );
        
        System.out.println("🔍 Système de monitoring automatique démarré!");
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
    
    // Nouvelles méthodes utilitaires
    public void printSystemStatus() {
        System.out.println("\n=== ÉTAT DU SYSTÈME DE MONITORING ===");
        System.out.println("Patients surveillés: " + sensorDataMap.size());
        System.out.println("Simulateurs actifs: " + activeSimulations.size());
        System.out.println("Total données capteur: " + sensorDataRepository.count());
        System.out.println("Total alertes: " + alertRepository.count());
        System.out.println("Alertes actives: " + alertRepository.findActiveAlerts().size());
        
        if (monitorTask != null) {
            monitorTask.printMonitoringStats();
        }
        
        System.out.println("=====================================\n");
    }

    @PreDestroy
    public void cleanUp() {
        // Arrêter le MonitorTask
        if (monitoringTask != null) {
            monitoringTask.cancel(true);
        }
        
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