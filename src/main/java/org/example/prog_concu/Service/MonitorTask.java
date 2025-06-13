package org.example.prog_concu.Service;

import org.example.prog_concu.config.VitalSignsThresholds;
import org.example.prog_concu.entities.SensorData;  // ← Changé de SensorDataDto vers SensorData
import org.example.prog_concu.dto.AlertDto;
import org.example.prog_concu.repository.SensorDataRepository;
import org.example.prog_concu.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MonitorTask implements Runnable {
    
    // Champs publics pour permettre l'injection manuelle depuis MonitoringManager
    public AlertRepository alertRepository;
    public SensorDataRepository sensorDataRepository;
    
    // Map pour stocker les dernières données de chaque patient
    private final ConcurrentHashMap<Long, SensorData> latestSensorDataMap;  // ← Changé vers SensorData
    
    // Constructeur sans paramètre pour Spring
    public MonitorTask() {
        this.latestSensorDataMap = new ConcurrentHashMap<>();
    }
    
    // Constructeur avec paramètre pour injection manuelle
    public MonitorTask(ConcurrentHashMap<Long, SensorData> latestSensorDataMap) {
        this.latestSensorDataMap = latestSensorDataMap;
    }
    
    @Override
    public void run() {
        System.out.println("MonitorTask en cours d'exécution à " + LocalDateTime.now());
        
        // Parcourir toutes les dernières données de capteurs
        for (SensorData sensorData : latestSensorDataMap.values()) {
            checkVitalSigns(sensorData);
        }
    }
    
    private void checkVitalSigns(SensorData sensorData) {
        List<AlertDto> alerts = new ArrayList<>();
        
        // Vérifier la fréquence cardiaque
        if (sensorData.getHeartRate() < VitalSignsThresholds.HEART_RATE_MIN) {
            alerts.add(createAlert(sensorData.getPatientId(), "HEART_RATE", 
                sensorData.getHeartRate(), String.valueOf(VitalSignsThresholds.HEART_RATE_MIN), "BRADYCARDIE"));
        } else if (sensorData.getHeartRate() > VitalSignsThresholds.HEART_RATE_MAX) {
            alerts.add(createAlert(sensorData.getPatientId(), "HEART_RATE", 
                sensorData.getHeartRate(), String.valueOf(VitalSignsThresholds.HEART_RATE_MAX), "TACHYCARDIE"));
        }
        
        // Vérifier la température
        if (sensorData.getTemperature() < VitalSignsThresholds.TEMPERATURE_MIN) {
            alerts.add(createAlert(sensorData.getPatientId(), "TEMPERATURE", 
                sensorData.getTemperature(), String.valueOf(VitalSignsThresholds.TEMPERATURE_MIN), "HYPOTHERMIE"));
        } else if (sensorData.getTemperature() > VitalSignsThresholds.TEMPERATURE_MAX) {
            alerts.add(createAlert(sensorData.getPatientId(), "TEMPERATURE", 
                sensorData.getTemperature(), String.valueOf(VitalSignsThresholds.TEMPERATURE_MAX), "HYPERTHERMIE"));
        }
        
        // Vérifier la pression systolique
        if (sensorData.getBloodPressureSystolic() < VitalSignsThresholds.BLOOD_PRESSURE_SYSTOLIC_MIN) {
            alerts.add(createAlert(sensorData.getPatientId(), "BLOOD_PRESSURE_SYSTOLIC", 
                sensorData.getBloodPressureSystolic(), String.valueOf(VitalSignsThresholds.BLOOD_PRESSURE_SYSTOLIC_MIN), "HYPOTENSION_SYSTOLIQUE"));
        } else if (sensorData.getBloodPressureSystolic() > VitalSignsThresholds.BLOOD_PRESSURE_SYSTOLIC_MAX) {
            alerts.add(createAlert(sensorData.getPatientId(), "BLOOD_PRESSURE_SYSTOLIC", 
                sensorData.getBloodPressureSystolic(), String.valueOf(VitalSignsThresholds.BLOOD_PRESSURE_SYSTOLIC_MAX), "HYPERTENSION_SYSTOLIQUE"));
        }
        
        // Vérifier la pression diastolique
        if (sensorData.getBloodPressureDiastolic() < VitalSignsThresholds.BLOOD_PRESSURE_DIASTOLIC_MIN) {
            alerts.add(createAlert(sensorData.getPatientId(), "BLOOD_PRESSURE_DIASTOLIC", 
                sensorData.getBloodPressureDiastolic(), String.valueOf(VitalSignsThresholds.BLOOD_PRESSURE_DIASTOLIC_MIN), "HYPOTENSION_DIASTOLIQUE"));
        } else if (sensorData.getBloodPressureDiastolic() > VitalSignsThresholds.BLOOD_PRESSURE_DIASTOLIC_MAX) {
            alerts.add(createAlert(sensorData.getPatientId(), "BLOOD_PRESSURE_DIASTOLIC", 
                sensorData.getBloodPressureDiastolic(), String.valueOf(VitalSignsThresholds.BLOOD_PRESSURE_DIASTOLIC_MAX), "HYPERTENSION_DIASTOLIQUE"));
        }
        
        // Sauvegarder les alertes via le repository
        for (AlertDto alertDto : alerts) {
            // Convertir AlertDto en Alert entity avant sauvegarde
            org.example.prog_concu.entities.Alert alertEntity = convertToEntity(alertDto);
            alertRepository.save(alertEntity);
            
            // Log pour debug
            System.out.println("ALERTE GÉNÉRÉE: Patient " + alertDto.getPatientId() + 
                " - " + alertDto.getSigneVital() + " = " + alertDto.getValeurMesuree() + 
                " (Seuil: " + alertDto.getSeuilFranchi() + ")");
        }
    }
    
    private AlertDto createAlert(Long patientId, String signeVital, double valeurMesuree, 
                               String seuilFranchi, String typeAlerte) {
        return AlertDto.builder()
                .patientId(patientId)
                .signeVital(signeVital + " - " + typeAlerte)
                .valeurMesuree(valeurMesuree)
                .seuilFranchi(seuilFranchi)
                .timestamp(LocalDateTime.now())
                .active(true)
                .build();
    }
    
    private org.example.prog_concu.entities.Alert convertToEntity(AlertDto alertDto) {
        org.example.prog_concu.entities.Alert alert = new org.example.prog_concu.entities.Alert();
        alert.setPatientId(alertDto.getPatientId());
        alert.setSigneVital(alertDto.getSigneVital());
        alert.setValeurMesuree(alertDto.getValeurMesuree());
        alert.setSeuilFranchi(alertDto.getSeuilFranchi());
        alert.setTimestamp(alertDto.getTimestamp());
        alert.setActive(alertDto.isActive());
        return alert;
    }
    
    // Méthode utilitaire pour obtenir les statistiques de monitoring
    public void printMonitoringStats() {
        System.out.println("=== STATISTIQUES DE MONITORING ===");
        System.out.println("Patients surveillés: " + latestSensorDataMap.size());
        if (alertRepository != null) {
            System.out.println("Total alertes générées: " + alertRepository.count());
            System.out.println("Alertes actives: " + alertRepository.findActiveAlerts().size());
        }
        System.out.println("===================================");
    }
}