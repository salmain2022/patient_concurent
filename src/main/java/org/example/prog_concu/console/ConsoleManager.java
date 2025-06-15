package org.example.prog_concu.console;

import org.example.prog_concu.Service.AlertService;
import org.example.prog_concu.Service.MonitoringManager;
import org.example.prog_concu.Service.PatientService;
import org.example.prog_concu.entities.Alert;
import org.example.prog_concu.entities.Patient;
import org.example.prog_concu.entities.SensorData;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ConsoleManager {

    private final PatientService patientService;
    private final MonitoringManager monitoringManager;
    private final AlertService alertService;
    private final Scanner scanner;
    private final ScheduledExecutorService displayExecutor;
    private boolean running = true;
    private boolean debugMode = false; 

    public ConsoleManager(PatientService patientService, 
                         MonitoringManager monitoringManager, 
                         AlertService alertService) {
        this.patientService = patientService;
        this.monitoringManager = monitoringManager;
        this.alertService = alertService;
        this.scanner = new Scanner(System.in);
        this.displayExecutor = Executors.newSingleThreadScheduledExecutor();
        
        monitoringManager.enableConsoleMode();
    }

    public void start() {
        System.out.println("🚀 Interface console démarrée");
        System.out.println("💡 Tapez 'help' pour voir les commandes disponibles");
        System.out.println("🔇 Mode silencieux activé par défaut (utilisez 'debug on' pour voir les détails)\n");

        // Thread séparé pour la saisie utilisateur
        Thread inputThread = new Thread(() -> {
            while (running) {
                try {
                    System.out.print("medical-monitor> ");
                    System.out.flush();
                    String input = scanner.nextLine().trim();
                    
                    if (input.isEmpty()) continue;
                    
                    processCommand(input);
                } catch (Exception e) {
                    if (running) {
                        System.err.println("Erreur de saisie: " + e.getMessage());
                    }
                }
            }
        });
        
        inputThread.setDaemon(false); // Thread principal
        inputThread.start();
        
        try {
            inputThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        cleanup();
    }

    private void processCommand(String input) {
        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();

        try {
            switch (command) {
                case "help":
                    showHelp();
                    break;
                case "list-patients":
                    listPatients();
                    break;
                case "add-patient":
                    addPatient(parts);
                    break;
                case "start-monitoring":
                    startMonitoring(parts);
                    break;
                case "stop-monitoring":
                    stopMonitoring(parts);
                    break;
                case "show-data":
                    showSensorData(parts);
                    break;
                case "show-alerts":
                    showAlerts();
                    break;
                case "ack-alert":
                    acknowledgeAlert(parts);
                    break;
                case "ack-patient-alerts":
                    acknowledgePatientAlerts(parts);
                    break;
                case "status":
                    showSystemStatus();
                    break;
                case "monitor-live":
                    startLiveMonitoring(parts);
                    break;
                case "clear":
                    clearScreen();
                    break;
                case "debug":
                    toggleDebugMode(parts);
                    break;
                case "quiet":
                    toggleQuietMode();
                    break;
                case "pause-all":
                    pauseAllMonitoring();
                    break;
                case "resume-all":
                    resumeAllMonitoring();
                    break;
                case "exit":
                    exit();
                    break;
                default:
                    System.out.println("❌ Commande inconnue: " + command + ". Tapez 'help' pour voir les commandes disponibles.");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'exécution de la commande: " + e.getMessage());
            if (debugMode) {
                e.printStackTrace();
            }
        }
    }

    private void showHelp() {
        System.out.println("\n📋 COMMANDES DISPONIBLES:");
        System.out.println("─────────────────────────────────────────────────────");
        System.out.println("👥 GESTION DES PATIENTS:");
        System.out.println("  list-patients              - Afficher tous les patients");
        System.out.println("  add-patient <nom> <prénom> - Ajouter un nouveau patient");
        System.out.println();
        System.out.println("🔍 SURVEILLANCE:");
        System.out.println("  start-monitoring <id>      - Démarrer la surveillance d'un patient");
        System.out.println("  stop-monitoring <id>       - Arrêter la surveillance d'un patient");
        System.out.println("  show-data <id>             - Afficher les dernières données d'un patient");
        System.out.println("  monitor-live <id>          - Surveillance en temps réel (Ctrl+C pour arrêter)");
        System.out.println("  pause-all                  - Mettre en pause toutes les surveillances");
        System.out.println("  resume-all                 - Reprendre toutes les surveillances");
        System.out.println();
        System.out.println("🚨 ALERTES:");
        System.out.println("  show-alerts                - Afficher toutes les alertes");
        System.out.println("  ack-alert <id>             - Acquitter une alerte par son ID");
        System.out.println("  ack-patient-alerts <id>    - Acquitter toutes les alertes d'un patient");
        System.out.println();
        System.out.println("📊 SYSTÈME:");
        System.out.println("  status                     - Afficher l'état du système");
        System.out.println("  debug on/off               - Activer/désactiver les messages détaillés");
        System.out.println("  quiet                      - Basculer mode silencieux/verbeux");
        System.out.println("  clear                      - Effacer l'écran");
        System.out.println("  exit                       - Quitter l'application");
        System.out.println("─────────────────────────────────────────────────────\n");
    }

    private void listPatients() {
        List<Patient> patients = patientService.findAll();
        
        if (patients.isEmpty()) {
            System.out.println("📋 Aucun patient enregistré.");
            return;
        }

        System.out.println("\n👥 LISTE DES PATIENTS:");
        System.out.println("─────────────────────────────────────────");
        for (Patient patient : patients) {
            System.out.printf("ID: %d | %s %s%n", 
                patient.getId(), 
                patient.getPrenom(), 
                patient.getNom());
        }
        System.out.println();
    }

    private void addPatient(String[] parts) {
        if (parts.length != 3) {
            System.out.println("❌ Usage: add-patient <nom> <prénom>");
            return;
        }

        String nom = parts[1];
        String prenom = parts[2];

        Patient patient = new Patient();
        patient.setNom(nom);
        patient.setPrenom(prenom);

        Patient saved = patientService.save(patient);
        System.out.printf("✅ Patient ajouté avec succès: %s %s (ID: %d)%n", 
            saved.getPrenom(), saved.getNom(), saved.getId());
    }

    private void startMonitoring(String[] parts) {
        if (parts.length != 2) {
            System.out.println("❌ Usage: start-monitoring <patient_id>");
            return;
        }
        
        try {
            Long patientId = Long.parseLong(parts[1]);
            Optional<Patient> patient = patientService.findById(patientId);
            
            if (patient.isEmpty()) {
                System.out.println("❌ Patient non trouvé avec l'ID: " + patientId);
                return;
            }
            
            // Démarrer la surveillance en arrière-plan
            boolean started = startMonitoringInBackground(patientId);
            
            if (started) {
                System.out.printf("🔍 Surveillance démarrée pour %s %s (ID: %d)%n", 
                    patient.get().getPrenom(), patient.get().getNom(), patientId);
                System.out.println("💡 Surveillance active en arrière-plan. Console disponible pour autres commandes.");
            } else {
                System.out.println("⚠️ La surveillance est déjà active pour ce patient.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ ID patient invalide: " + parts[1]);
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du démarrage de la surveillance: " + e.getMessage());
            if (debugMode) {
                e.printStackTrace();
            }
        }
    }

    // démarrer la surveillance en arrière-plan
    private boolean startMonitoringInBackground(Long patientId) {
        try {
            // Vérifier si la surveillance n'est pas déjà active
            if (monitoringManager.isMonitoringActive(patientId)) {
                return false;
            }
            
            // Créer un thread daemon pour la surveillance
            Thread monitoringThread = new Thread(() -> {
                try {
                    if (debugMode) {
                        System.out.println("🔧 [DEBUG] Démarrage du thread de surveillance pour patient " + patientId);
                    }
                    monitoringManager.startMonitoring(patientId);
                } catch (Exception e) {
                    System.out.println("❌ Erreur dans le thread de surveillance: " + e.getMessage());
                    if (debugMode) {
                        e.printStackTrace();
                    }
                }
            }, "MonitoringThread-Patient-" + patientId);
            
            monitoringThread.setDaemon(true);
            monitoringThread.start();
            
            // Attendre un court moment pour s'assurer que la surveillance démarre
            Thread.sleep(300);
            
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("❌ Interruption lors du démarrage de la surveillance");
            return false;
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du démarrage de la surveillance: " + e.getMessage());
            if (debugMode) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private void stopMonitoring(String[] parts) {
        if (parts.length != 2) {
            System.out.println("❌ Usage: stop-monitoring <patient_id>");
            return;
        }

        try {
            Long patientId = Long.parseLong(parts[1]);
            
            if (!monitoringManager.isMonitoringActive(patientId)) {
                System.out.println("⚠️ Aucune surveillance active pour le patient ID: " + patientId);
                return;
            }
            
            monitoringManager.stopMonitoring(patientId);
            System.out.println("🛑 Surveillance arrêtée pour le patient ID: " + patientId);
            
        } catch (NumberFormatException e) {
            System.out.println("❌ ID patient invalide: " + parts[1]);
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'arrêt de la surveillance: " + e.getMessage());
        }
    }

    private void showSensorData(String[] parts) {
        if (parts.length != 2) {
            System.out.println("❌ Usage: show-data <patient_id>");
            return;
        }

        try {
            Long patientId = Long.parseLong(parts[1]);
            
            Optional<Patient> patient = patientService.findById(patientId);
            if (patient.isEmpty()) {
                System.out.println("❌ Patient non trouvé avec l'ID: " + patientId);
                return;
            }

            SensorData data = monitoringManager.getLatestSensorData(patientId);
            if (data == null) {
                System.out.println("❌ Aucune donnée disponible pour le patient ID: " + patientId);
                if (!monitoringManager.isMonitoringActive(patientId)) {
                    System.out.println("💡 Démarrez la surveillance avec: start-monitoring " + patientId);
                }
                return;
            }

            displaySensorData(patient.get(), data);
            
        } catch (NumberFormatException e) {
            System.out.println("❌ ID patient invalide: " + parts[1]);
        }
    }

    private void displaySensorData(Patient patient, SensorData data) {
        System.out.println("\n📊 DONNÉES CAPTEUR - " + patient.getPrenom() + " " + patient.getNom());
        System.out.println("─────────────────────────────────────────────────────");
        System.out.printf("🌡️  Température: %.1f°C%n", data.getTemperature());
        System.out.printf("❤️  Fréquence cardiaque: %d bpm%n", data.getHeartRate());
        System.out.printf("🩸 Pression artérielle: %.0f/%.0f mmHg%n", 
            data.getBloodPressureSystolic(), data.getBloodPressureDiastolic());
        System.out.printf("⏰ Timestamp: %s%n", 
            data.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println();
    }

    private void showAlerts() {
        List<Alert> activeAlerts = alertService.getActiveAlerts();
        List<Alert> allAlerts = alertService.getAllAlerts();

        System.out.println("\n🚨 ALERTES ACTIVES (" + activeAlerts.size() + "):");
        System.out.println("─────────────────────────────────────────────────────");
        
        if (activeAlerts.isEmpty()) {
            System.out.println("✅ Aucune alerte active");
        } else {
            for (Alert alert : activeAlerts) {
                displayAlert(alert);
            }
        }

        System.out.println("\n📋 HISTORIQUE DES ALERTES (" + allAlerts.size() + " total):");
        System.out.println("─────────────────────────────────────────────────────");
        
        for (Alert alert : allAlerts) {
            String status = alert.isActive() ? "🚨 ACTIVE" : "✅ ACQUITTÉE";
            System.out.printf("[%s] ID:%d | Patient:%d | %s: %.1f (seuil: %.1f) | %s%n",
                status,
                alert.getId(),
                alert.getPatientId(),
                alert.getSigneVital(),
                alert.getValeurMesuree(),
                alert.getSeuilFranchi(),
                alert.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss"))
            );
        }
        System.out.println();
    }

    private void displayAlert(Alert alert) {
        System.out.printf("🚨 [ID:%d] Patient %d - %s: %.1f (seuil franchi: %.1f) - %s%n",
            alert.getId(),
            alert.getPatientId(),
            alert.getSigneVital(),
            alert.getValeurMesuree(),
            alert.getSeuilFranchi(),
            alert.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss"))
        );
    }

    private void acknowledgeAlert(String[] parts) {
        if (parts.length != 2) {
            System.out.println("❌ Usage: ack-alert <alert_id>");
            return;
        }

        try {
            Long alertId = Long.parseLong(parts[1]);
            alertService.acquitterAlerte(alertId);
            System.out.println("✅ Alerte ID " + alertId + " acquittée");
        } catch (NumberFormatException e) {
            System.out.println("❌ ID alerte invalide: " + parts[1]);
        }
    }

    private void acknowledgePatientAlerts(String[] parts) {
        if (parts.length != 2) {
            System.out.println("❌ Usage: ack-patient-alerts <patient_id>");
            return;
        }

        try {
            Long patientId = Long.parseLong(parts[1]);
            alertService.acquitterAlertesByPatient(patientId);
            System.out.println("✅ Toutes les alertes du patient ID " + patientId + " ont été acquittées");
        } catch (NumberFormatException e) {
            System.out.println("❌ ID patient invalide: " + parts[1]);
        }
    }

    private void showSystemStatus() {
        System.out.println("\n📊 ÉTAT DU SYSTÈME:");
        System.out.println("─────────────────────────────────────────");
        System.out.println("🔧 Mode debug: " + (debugMode ? "ACTIVÉ" : "DÉSACTIVÉ"));
        System.out.println();
        monitoringManager.printSystemStatus();
    }

    private void startLiveMonitoring(String[] parts) {
        if (parts.length != 2) {
            System.out.println("❌ Usage: monitor-live <patient_id>");
            return;
        }

        try {
            Long patientId = Long.parseLong(parts[1]);
            
            Optional<Patient> patient = patientService.findById(patientId);
            if (patient.isEmpty()) {
                System.out.println("❌ Patient non trouvé avec l'ID: " + patientId);
                return;
            }

            if (!monitoringManager.isMonitoringActive(patientId)) {
                System.out.println("⚠️ Surveillance non active pour ce patient. Démarrage automatique...");
                startMonitoringInBackground(patientId);
                Thread.sleep(1000); // Laisser le temps à la surveillance de démarrer
            }

            System.out.println("🔴 SURVEILLANCE EN TEMPS RÉEL - " + patient.get().getPrenom() + " " + patient.get().getNom());
            System.out.println("Appuyez sur ENTRÉE pour arrêter...\n");

            // Temporairement activer le mode verbeux pour le live monitoring
            boolean originalDebugMode = debugMode;
            debugMode = false; // Masquer les messages debug pendant le live

            // Créer un nouvel executor pour le live monitoring
            ScheduledExecutorService liveExecutor = Executors.newSingleThreadScheduledExecutor();
            
            // Démarrer l'affichage en temps réel
            liveExecutor.scheduleAtFixedRate(() -> {
                SensorData data = monitoringManager.getLatestSensorData(patientId);
                if (data != null) {
                    clearScreen();
                    System.out.println("🔴 SURVEILLANCE EN TEMPS RÉEL - " + patient.get().getPrenom() + " " + patient.get().getNom());
                    System.out.println("Appuyez sur ENTRÉE pour arrêter...\n");
                    displaySensorData(patient.get(), data);
                    
                    // Afficher les alertes actives pour ce patient
                    List<Alert> alerts = alertService.getActiveAlerts();
                    alerts.stream()
                        .filter(a -> a.getPatientId().equals(patientId))
                        .forEach(alert -> {
                            System.out.println("🚨 ALERTE ACTIVE:");
                            displayAlert(alert);
                        });
                }
            }, 0, 2, TimeUnit.SECONDS);

            // Attendre que l'utilisateur appuie sur ENTRÉE
            scanner.nextLine();
            
            // Arrêter l'affichage en temps réel
            liveExecutor.shutdown();
            try {
                if (!liveExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    liveExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                liveExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            clearScreen();
            System.out.println("🛑 Surveillance en temps réel arrêtée\n");

            // Restaurer le mode debug précédent
            debugMode = originalDebugMode;
            
        } catch (NumberFormatException e) {
            System.out.println("❌ ID patient invalide: " + parts[1]);
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la surveillance live: " + e.getMessage());
            if (debugMode) {
                e.printStackTrace();
            }
        }
    }

    private void pauseAllMonitoring() {
        monitoringManager.pauseAllMonitoring();
        System.out.println("⏸️ Toutes les surveillances ont été mises en pause");
    }
    
    private void resumeAllMonitoring() {
        monitoringManager.resumeAllMonitoring();
        System.out.println("▶️ Toutes les surveillances ont été reprises");
    }

    private void clearScreen() {
        // Effacer l'écran (compatible avec la plupart des terminaux)
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[2J\033[H");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback si le clear ne fonctionne pas
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    private void exit() {
        System.out.println("🛑 Arrêt de toutes les surveillances...");
        monitoringManager.stopAllMonitoring();
        System.out.println("👋 Au revoir!");
        running = false;
    }

    private void toggleDebugMode(String[] parts) {
        if (parts.length != 2) {
            System.out.println("❌ Usage: debug <on|off>");
            return;
        }

        String mode = parts[1].toLowerCase();
        if ("on".equals(mode)) {
            debugMode = true;
            monitoringManager.disableConsoleMode();
            System.out.println("🔧 Mode debug ACTIVÉ - messages détaillés visibles");
        } else if ("off".equals(mode)) {
            debugMode = false;
            monitoringManager.enableConsoleMode();
            System.out.println("🔇 Mode debug DÉSACTIVÉ - messages masqués");
        } else {
            System.out.println("❌ Mode invalide. Utilisez 'on' ou 'off'");
        }
    }

    private void toggleQuietMode() {
        debugMode = !debugMode;
        if (debugMode) {
            monitoringManager.disableConsoleMode();
            System.out.println("🔊 Mode verbeux activé - messages détaillés visibles");
        } else {
            monitoringManager.enableConsoleMode();
            System.out.println("🔇 Mode silencieux activé - messages masqués");
        }
    }

    private void cleanup() {
        try {
            if (!displayExecutor.isShutdown()) {
                displayExecutor.shutdown();
                if (!displayExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    displayExecutor.shutdownNow();
                }
            }
        } catch (InterruptedException e) {
            displayExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
}