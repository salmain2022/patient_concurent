package org.example.prog_concu.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.prog_concu.entities.SensorData;
import org.example.prog_concu.repository.SensorDataRepository;
import org.example.prog_concu.simulator.SensorSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.HashSet;

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
    private boolean consoleMode = true; // Mode console par défaut

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
        if (!consoleMode) {
            System.out.println("✅ MonitoringManager initialized");
        }

        // Création propre de MonitorTask avec injection de la map + alertService
        monitorTask = new MonitorTask(sensorDataMap);
        monitorTask.setAlertService(alertService);
        monitorTask.disableDebugMode(); // Désactiver les messages de debug en mode console

        // Lancer le monitoring périodique (toutes les 3 secondes)
        monitoringTask = executorService.scheduleAtFixedRate(
                () -> {
                    try {
                        monitorTask.run();
                    } catch (Exception e) {
                        if (!consoleMode) {
                            System.err.println("❌ Erreur dans MonitorTask: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                },
                5, // délai initial
                3, // intervalle
                TimeUnit.SECONDS
        );

        if (!consoleMode) {
            System.out.println("🔍 Système de monitoring automatique démarré !");
        }
    }

    public void startMonitoring(Long patientId) {
        if (activeSimulations.containsKey(patientId)) {
            if (!consoleMode) {
                System.out.println("⚠️ Surveillance déjà active pour le patient: " + patientId);
            }
            return;
        }

        //  Créer le simulateur correctement
        SensorSimulator simulator = createSensorSimulator(patientId);

        // Programmer l'exécution toutes les 3 secondes
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(
                simulator,
                0,           // délai initial
                3,           // intervalle
                TimeUnit.SECONDS
        );

        activeSimulations.put(patientId, future);
        if (!consoleMode) {
            System.out.println("🚀 Surveillance démarrée pour le patient: " + patientId);
        }
    }

    //  Créer un simulateur correctement configuré
    private SensorSimulator createSensorSimulator(Long patientId) {
        try {
            // Obtenir une nouvelle instance du simulateur via Spring
            SensorSimulator simulator = applicationContext.getBean(SensorSimulator.class);
            
            // Configurer le simulateur
            simulator.setPatientId(patientId);
            simulator.setSharedSensorDataMap(sensorDataMap);
            simulator.setConsoleMode(consoleMode);
            
            return simulator;
        } catch (Exception e) {
            // Fallback: créer manuellement si problème avec Spring
            if (!consoleMode) {
                System.out.println("⚠️ Utilisation du fallback pour créer SensorSimulator");
            }
            return new SensorSimulator(patientId, sensorDataMap, sensorDataRepository);
        }
    }

    public void stopMonitoring(Long patientId) {
        ScheduledFuture<?> future = activeSimulations.remove(patientId);
        if (future != null) {
            future.cancel(true);
            sensorDataMap.remove(patientId);
            if (!consoleMode) {
                System.out.println("🛑 Surveillance arrêtée pour le patient: " + patientId);
            }
        }
    }

    /**
     * Vérifie si la surveillance est active pour un patient donné
     */
    public boolean isMonitoringActive(Long patientId) {
        ScheduledFuture<?> future = activeSimulations.get(patientId);
        return future != null && !future.isCancelled() && !future.isDone();
    }

    /**
     * Arrête toutes les surveillances actives
     */
    public void stopAllMonitoring() {
        // Annuler toutes les simulations actives
        activeSimulations.forEach((patientId, future) -> {
            future.cancel(true);
            if (!consoleMode) {
                System.out.println("🛑 Surveillance arrêtée pour le patient: " + patientId);
            }
        });
        
        // Vider les maps
        activeSimulations.clear();
        sensorDataMap.clear();
        
        if (!consoleMode) {
            System.out.println("🔴 Toutes les surveillances ont été arrêtées");
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

        // Afficher les patients actuellement surveillés
        if (!sensorDataMap.isEmpty()) {
            System.out.println("\nPatients avec données actives:");
            sensorDataMap.forEach((patientId, data) -> {
                System.out.printf("  - Patient %d: dernière mesure à %s%n", 
                    patientId, data.getTimestamp());
            });
        }

        if (monitorTask != null) {
            monitorTask.printMonitoringStats();
        }

        System.out.println("=====================================\n");
    }

    // Méthodes pour contrôler le mode console
    public void enableConsoleMode() {
        this.consoleMode = true;
        if (monitorTask != null) {
            monitorTask.disableDebugMode();
        }
        // Appliquer le mode console aux simulateurs actifs
        updateSimulatorsConsoleMode();
    }

    public void disableConsoleMode() {
        this.consoleMode = false;
        if (monitorTask != null) {
            monitorTask.enableDebugMode();
        }
        // Appliquer le mode console aux simulateurs actifs
        updateSimulatorsConsoleMode();
    }

    // Mettre à jour le mode console des simulateurs
    private void updateSimulatorsConsoleMode() {
        // Note: Cette méthode est pour référence future
        // En pratique, les simulateurs créés gardent leur mode console initial
        if (!consoleMode) {
            System.out.println("🔧 Mode console mis à jour pour les futurs simulateurs");
        }
    }

    public MonitorTask getMonitorTask() {
        return monitorTask;
    }

    public void pauseAllMonitoring() {
        activeSimulations.forEach((patientId, future) -> {
            future.cancel(false); // Cancel without interrupting if possible
            if (!consoleMode) {
                System.out.println("⏸️ Surveillance en pause pour le patient: " + patientId);
            }
        });
        activeSimulations.clear(); // Clear all active simulations
    }

    public void resumeAllMonitoring() {
        // Get all patient IDs that were being monitored (from sensorDataMap)
        Set<Long> patientIds = new HashSet<>(sensorDataMap.keySet());
        
        for (Long patientId : patientIds) {
            if (!activeSimulations.containsKey(patientId)) {
                startMonitoring(patientId); // Restart monitoring for each patient
            }
        }
        
        if (!consoleMode) {
            System.out.println("▶️ Surveillance reprise pour tous les patients");
        }
    }

    @PreDestroy
    public void cleanUp() {
        if (monitoringTask != null) {
            monitoringTask.cancel(true);
        }

        // Arrêter toutes les simulations
        stopAllMonitoring();

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (!consoleMode) {
            System.out.println("🔴 MonitoringManager arrêté");
        }
    }
}