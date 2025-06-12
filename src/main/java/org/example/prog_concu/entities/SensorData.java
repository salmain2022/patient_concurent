package org.example.prog_concu.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorData {
    private Long patientId;
    private int heartRate;
    private double temperature;
    private int bloodPressureSystolic;
    private int bloodPressureDiastolic;
    private LocalDateTime timestamp;
}

