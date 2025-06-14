package org.example.prog_concu.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.prog_concu.entities.SensorData;
import org.example.prog_concu.repository.SensorDataRepository;
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

    private final ApplicationContext applicationContext;
    private final SensorDataRepository sensorDataRepository;
    private final AlertService alertService;

    private MonitorTask monitorTask;
    private ScheduledFuture<?> monitoringTask;

    @Autowired
    public MonitoringManager(ApplicationContext applicationContext,
                             SensorDataRepository sensorDataRepository,
                             AlertService alertService) {
        this.applicationContext = applicationContext;
        this.sensorDataRepository = sensorDataRepository;
        this.alertService = alertService;
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ MonitoringManager initialized");

        // Création propre de MonitorTask avec injection de la map + alertService
        monitorTask = new MonitorTask(sensorDataMap);
        monitorTask.setAlertService(alertService);  // Injection via setter (car instancié manuellement)

        // Lancer le monitoring périodique (toutes les 3 secondes)
        monitoringTask = executorService.scheduleAtFixedRate(
                () -> {
                    try {
                        monitorTask.run();
                    } catch (Exception e) {
                        System.err.println("❌ Erreur dans MonitorTask: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                5, // délai initial
                3, // intervalle
                TimeUnit.SECONDS
        );

        System.out.println("🔍 Système de monitoring automatique démarré !");
    }

    public void startMonitoring(Long patientId) {
        if (activeSimulations.containsKey(patientId)) {
            System.out.println("⚠️ Surveillance déjà active pour le patient: " + patientId);
            return;
        }

        SensorSimulator simulator = applicationContext.getBean(
                SensorSimulator.class,
                patientId,
                3000,
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

    public void printSystemStatus() {
        System.out.println("\n=== ÉTAT DU SYSTÈME DE MONITORING ===");
        System.out.println("Patients surveillés: " + sensorDataMap.size());
        System.out.println("Simulateurs actifs: " + activeSimulations.size());
        System.out.println("Total données capteur: " + sensorDataRepository.count());
        System.out.println("Alertes actives: " + alertService.getActiveAlerts().size());
        System.out.println("Total alertes: " + alertService.getAllAlerts().size());

        if (monitorTask != null) {
            monitorTask.printMonitoringStats();
        }

        System.out.println("=====================================\n");
    }

    @PreDestroy
    public void cleanUp() {
        if (monitoringTask != null) {
            monitoringTask.cancel(true);
        }

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
