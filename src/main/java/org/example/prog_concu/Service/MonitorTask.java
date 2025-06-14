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

    public MonitorTask() {
        this.latestSensorDataMap = new ConcurrentHashMap<>();
    }

    public MonitorTask(ConcurrentHashMap<Long, SensorData> latestSensorDataMap) {
        this.latestSensorDataMap = latestSensorDataMap;
    }

    @Override
    public void run() {
        System.out.println("🩺 MonitorTask exécuté à " + LocalDateTime.now());

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
                Alert alert = new Alert(patientId, signeVital, valeur, true, LocalDateTime.now());
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

    // Setter utile si injection manuelle
    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }
}
