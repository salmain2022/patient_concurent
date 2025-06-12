package org.example.prog_concu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorDataDto {
    private Long patientId;
    private int heartRate;
    private double temperature;
    private int bloodPressureSystolic;
    private int bloodPressureDiastolic;
    private LocalDateTime timestamp;
}
