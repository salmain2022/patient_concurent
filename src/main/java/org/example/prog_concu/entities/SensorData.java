package org.example.prog_concu.entities;

import java.time.LocalDateTime;

public class SensorData {
    private Long patientId;
    private int heartRate;
    private double temperature;
    private int bloodPressureSystolic;
    private int bloodPressureDiastolic;
    private LocalDateTime timestamp;

    // Constructeur
    public SensorData() {}

    public SensorData(Long patientId, int heartRate, double temperature,
                      int systolic, int diastolic, LocalDateTime timestamp) {
        this.patientId = patientId;
        this.heartRate = heartRate;
        this.temperature = temperature;
        this.bloodPressureSystolic = systolic;
        this.bloodPressureDiastolic = diastolic;
        this.timestamp = timestamp;
    }

    // Getters
    public Long getPatientId() {
        return patientId;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getBloodPressureSystolic() {
        return bloodPressureSystolic;
    }

    public int getBloodPressureDiastolic() {
        return bloodPressureDiastolic;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setBloodPressureSystolic(int bloodPressureSystolic) {
        this.bloodPressureSystolic = bloodPressureSystolic;
    }

    public void setBloodPressureDiastolic(int bloodPressureDiastolic) {
        this.bloodPressureDiastolic = bloodPressureDiastolic;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format(
                "Patient %d - FC: %d, Temp: %.1f, TA: %d/%d à %s",
                patientId, heartRate, temperature,
                bloodPressureSystolic, bloodPressureDiastolic,
                timestamp.toString()
        );
    }
    
}
