package org.example.prog_concu.simulator;



//import com.healthmonitor.model.SensorData;
import org.example.prog_concu.entities.SensorData;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Random;

public class SensorSimulator implements Runnable {

    private final Long patientId;
    private final int intervalMillis;
    private volatile boolean running = true;
    private final Random random = new Random();
    private final Map<Long, SensorData> sharedSensorDataMap;

    public SensorSimulator(Long patientId, int intervalMillis, Map<Long, SensorData> sharedSensorDataMap) {
        this.patientId = patientId;
        this.intervalMillis = intervalMillis;
        this.sharedSensorDataMap = sharedSensorDataMap;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Générer et mettre à jour les données
                SensorData data = generateSensorData();
                sharedSensorDataMap.put(patientId, data);

                System.out.println("Données simulées mises à jour: " + data);

                // Attendre l'intervalle
                Thread.sleep(intervalMillis);

            } catch (InterruptedException e) {
                System.out.println("Simulation interrompue pour le patient " + patientId);
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Arrêt complet de la simulation pour le patient " + patientId);
    }

    public void stop() {
        running = false;
    }

    private SensorData generateSensorData() {
        return new SensorData(
                patientId,
                60 + random.nextInt(40),        // FC: 60-100 bpm
                36.0 + random.nextDouble() * 2.0, // Temp: 36.0-38.0 °C
                110 + random.nextInt(30),       // Systolique: 110-140 mmHg
                70 + random.nextInt(20),        // Diastolique: 70-90 mmHg
                LocalDateTime.now()
        );
    }
}