package org.example.prog_concu.config;

import java.util.concurrent.ConcurrentHashMap;

import org.example.prog_concu.entities.SensorData;
import org.example.prog_concu.repository.SensorDataRepository;
import org.example.prog_concu.simulator.SensorSimulator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SimulatorConfig {
    @Bean
    @Scope("prototype")
    public SensorSimulator sensorSimulator(Long patientId, int intervalMillis,
                                    ConcurrentHashMap<Long, SensorData> sharedMap,
                                    SensorDataRepository sensorDataRepository) {
        return new SensorSimulator(patientId, intervalMillis, sharedMap, sensorDataRepository);
    }
}
