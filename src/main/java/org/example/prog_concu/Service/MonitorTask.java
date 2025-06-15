package org.example.prog_concu.Service;

import org.example.prog_concu.config.VitalSignsThresholds;
import org.example.prog_concu.entities.Alert;
import org.example.prog_concu.entities.SensorData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MonitorTask implements Runnable {

    @Autowired
    private AlertService alertService;

    private final ConcurrentHashMap<Long, SensorData> latestSensorDataMap;
    private boolean debugMode = false; // Pour contrôler l'affichage des messages

    public MonitorTask() {
        this.latestSensorDataMap = new ConcurrentHashMap<>();
    }

    public MonitorTask(ConcurrentHashMap<Long, SensorData> latestSensorDataMap) {
        this.latestSensorDataMap = latestSensorDataMap;
    }

    @Override
    public void run() {
        // Enlever le message de debug qui pollue la console
        if (debugMode) {
            System.out.println("🩺 MonitorTask exécuté à " + LocalDateTime.now());
        }

        for (SensorData data : latestSensorDataMap.values()) {
            checkVitalSigns(data);
        }
    }

    private void checkVitalSigns(SensorData data) {
        Long patientId = data.getPatientId();

        checkAndTriggerAlert(patientId, "TEMPÉRATURE", data.getTemperature(),
                VitalSignsThresholds.TEMPERATURE_MIN, VitalSignsThresholds.TEMPERATURE_MAX);

        checkAndTriggerAlert(patientId, "FRÉQUENCE CARDIAQUE", (double) data.getHeartRate(),
                VitalSignsThresholds.HEART_RATE_MIN, VitalSignsThresholds.HEART_RATE_MAX);

        checkAndTriggerAlert(patientId, "PRESSION SYSTOLIQUE", data.getBloodPressureSystolic(),
                VitalSignsThresholds.BLOOD_PRESSURE_SYSTOLIC_MIN, VitalSignsThresholds.BLOOD_PRESSURE_SYSTOLIC_MAX);

        checkAndTriggerAlert(patientId, "PRESSION DIASTOLIQUE", data.getBloodPressureDiastolic(),
                VitalSignsThresholds.BLOOD_PRESSURE_DIASTOLIC_MIN, VitalSignsThresholds.BLOOD_PRESSURE_DIASTOLIC_MAX);
    }

    public void checkAndTriggerAlert(Long patientId, String signeVital, double valeur, double min, double max) {
        if (valeur < min || valeur > max) {
            if (!alertService.isAlertActive(patientId, signeVital)) {
                // Déterminer le seuil franchi
                double seuilFranchi = (valeur < min) ? min : max;
                
                Alert alert = Alert.builder()
                        .patientId(patientId)
                        .signeVital(signeVital)
                        .valeurMesuree(valeur)
                        .seuilFranchi(seuilFranchi)
                        .timestamp(LocalDateTime.now())
                        .active(true)
                        .build();
                        
                alertService.addAlert(alert);
            }
        }
    }

    public void printMonitoringStats() {
        System.out.println("=== STATISTIQUES DE MONITORING ===");
        System.out.println("Patients surveillés : " + latestSensorDataMap.size());
        System.out.println("Alertes actives : " + alertService.getActiveAlerts().size());
        System.out.println("Toutes alertes : " + alertService.getAllAlerts().size());
        System.out.println("===================================");
    }

    // Méthodes pour contrôler le mode debug
    public void enableDebugMode() {
        this.debugMode = true;
    }

    public void disableDebugMode() {
        this.debugMode = false;
    }

    // Setter utile si injection manuelle
    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }
}