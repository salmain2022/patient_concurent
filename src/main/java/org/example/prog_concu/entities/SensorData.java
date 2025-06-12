package org.example.prog_concu.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
public class SensorData {
    private Long patientId;
    private int heartRate;
    private double temperature;
    private int bloodPressureSystolic;
    private int bloodPressureDiastolic;
    private LocalDateTime timestamp;

    // Constructeur
    public SensorData(Long patientId, int heartRate, double temperature,
                      int systolic, int diastolic, LocalDateTime timestamp) {
        this.patientId = patientId;
        this.heartRate = heartRate;
        this.temperature = temperature;
        this.bloodPressureSystolic = systolic;
        this.bloodPressureDiastolic = diastolic;
        this.timestamp = timestamp;
    }

    // Getters & Setters
    // (omis pour brièveté - générer avec IDE)

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

