package org.example.prog_concu.monitoring;

import org.example.prog_concu.Service.MonitoringManager;
import org.example.prog_concu.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MonitoringTest implements CommandLineRunner {
    
    @Autowired
    private MonitoringManager monitoringManager;
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("🧪 === DÉMARRAGE DES TESTS DE MONITORING ===");
        
        // Démarrer la surveillance pour quelques patients de test
        System.out.println("🚀 Démarrage de la surveillance pour les patients 1, 2 et 3...");
        
        monitoringManager.startMonitoring(1L);
        monitoringManager.startMonitoring(2L);
        monitoringManager.startMonitoring(3L);
        
        // Attendre pour voir les données et alertes se générer
        System.out.println("⏳ Attente de 15 secondes pour voir les données et alertes...");
        Thread.sleep(15000); // 15 secondes
        
        // Afficher le statut du système
        monitoringManager.printSystemStatus();
        
        // Afficher quelques alertes générées
        System.out.println("🚨 Alertes générées:");
        alertRepository.findActiveAlerts().stream()
            .limit(10) // Limiter à 10 alertes pour éviter le spam
            .forEach(alert -> 
                System.out.println("   → Patient " + alert.getPatientId() + 
                    " - " + alert.getSigneVital() + " = " + alert.getValeur() +
                    " (Seuil: " + alert.getSeuilFranchi() + ")")
            );
        
        // Tester l'arrêt d'une surveillance
        System.out.println("🛑 Arrêt de la surveillance du patient 2...");
        monitoringManager.stopMonitoring(2L);
        
        // Attendre encore un peu
        System.out.println("⏳ Attente de 10 secondes supplémentaires...");
        Thread.sleep(10000);
        
        // Afficher le statut final
        System.out.println("📊 Statut final du système:");
        monitoringManager.printSystemStatus();
        
        System.out.println("✅ === TESTS DE MONITORING TERMINÉS ===");
    }
}