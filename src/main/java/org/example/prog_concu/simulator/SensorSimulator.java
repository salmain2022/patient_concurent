package org.example.prog_concu.simulator;

import org.example.prog_concu.entities.SensorData;
import org.example.prog_concu.repository.SensorDataRepository;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Random;

@Component
@Scope("prototype") // Très important pour que Spring crée une nouvelle instance à chaque fois
public class SensorSimulator implements Runnable {

    private final Long patientId;
    private final int intervalMillis;
    private volatile boolean running = true;
    private final Random random = new Random();
    private final Map<Long, SensorData> sharedSensorDataMap;
    private final SensorDataRepository sensorDataRepository;

    public SensorSimulator(Long patientId, int intervalMillis,
                           Map<Long, SensorData> sharedSensorDataMap,
                           SensorDataRepository sensorDataRepository) {
        this.patientId = patientId;
        this.intervalMillis = intervalMillis;
        this.sharedSensorDataMap = sharedSensorDataMap;
        this.sensorDataRepository = sensorDataRepository;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                SensorData data = generateSensorData();
                sharedSensorDataMap.put(patientId, data);
                sensorDataRepository.save(data); // 💾 Enregistrement BDD

                System.out.println("✅ Donnée simulée enregistrée pour le patient " + patientId + ": " + data);

                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                System.out.println("⛔ Simulation interrompue pour le patient " + patientId);
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("🛑 Arrêt complet de la simulation pour le patient " + patientId);
    }

    public void stop() {
        running = false;
    }

    private SensorData generateSensorData() {
        return new SensorData(
                patientId,
                60 + random.nextInt(40),             // FC: 60-100 bpm
                36.0 + random.nextDouble() * 2.0,     // Temp: 36.0-38.0 °C
                110 + random.nextInt(30),            // Systolique: 110-140
                70 + random.nextInt(20),             // Diastolique: 70-90
                LocalDateTime.now()
        );
    }
}
