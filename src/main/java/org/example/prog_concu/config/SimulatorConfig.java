package org.example.prog_concu.config;

import java.util.concurrent.ConcurrentHashMap;

import org.example.prog_concu.entities.SensorData;
import org.example.prog_concu.repository.SensorDataRepository;
import org.example.prog_concu.simulator.SensorSimulator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimulatorConfig {

    @Bean
    public ConcurrentHashMap<Long, SensorData> sharedSensorDataMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public SensorSimulator sensorSimulator(SensorDataRepository sensorDataRepository,
                                           ConcurrentHashMap<Long, SensorData> sharedSensorDataMap) {
        Long patientId = 1L;       // Exemple d'ID patient fixe ou paramétré autrement
        int intervalMillis = 3000; // Exemple d'intervalle fixe

        return new SensorSimulator(patientId, intervalMillis, sharedSensorDataMap, sensorDataRepository);
    }
}
