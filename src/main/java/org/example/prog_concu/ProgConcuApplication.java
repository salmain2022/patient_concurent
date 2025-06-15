package org.example.prog_concu;

import org.example.prog_concu.Service.AlertService;
import org.example.prog_concu.Service.MonitoringManager;
import org.example.prog_concu.Service.PatientService;
import org.example.prog_concu.console.ConsoleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProgConcuApplication implements CommandLineRunner {

    @Autowired
    private PatientService patientService;
    
    @Autowired
    private MonitoringManager monitoringManager;
    
    @Autowired
    private AlertService alertService;

    public static void main(String[] args) {
        // Désactiver le banner Spring Boot pour une sortie console plus propre
        System.setProperty("spring.main.banner-mode", "off");
        System.setProperty("logging.level.root", "WARN");
        
        ConfigurableApplicationContext context = SpringApplication.run(ProgConcuApplication.class, args);
        
        // Garder l'application vivante
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🔴 Arrêt du système de monitoring médical...");
            context.close();
        }));
    }

    @Override
    public void run(String... args) throws Exception {
        // Affichage du banner personnalisé
        printBanner();
        
        // Initialisation du gestionnaire de console
        ConsoleManager consoleManager = new ConsoleManager(patientService, monitoringManager, alertService);
        
        // Démarrage de l'interface console
        consoleManager.start();
    }

    private void printBanner() {
        System.out.println("\n" +
                "╔═══════════════════════════════════════════════════════════╗\n" +
                "║              🏥 SYSTÈME DE MONITORING MÉDICAL             ║\n" +
                "║                    Version 1.0                           ║\n" +
                "╚═══════════════════════════════════════════════════════════╝\n");
    }
}