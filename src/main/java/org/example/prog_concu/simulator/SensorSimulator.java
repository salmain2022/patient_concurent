package org.example.prog_concu.simulator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.example.prog_concu.entities.SensorData;

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
        while (running) {
            // Générer des données aléatoires
            SensorData data = generateSensorData();

            // Mettre à jour la Map partagée avec la nouvelle donnée pour ce patient
            sharedSensorDataMap.put(patientId, data);

            System.out.println("Données simulées mises à jour dans la Map: " + data);

            try {
                Thread.sleep(intervalMillis);  // intervalle en millisecondes (ex: 3000 ms = 3 sec)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Simulation interrompue");
            break;
        }
    }
    }



    public void stop() {
        running = false;
    }

    private SensorData generateSensorData() {
        int heartRate = 60 + random.nextInt(40); // 60-100 bpm
        double temperature = 36.0 + random.nextDouble() * 2.0; // 36.0-38.0 °C
        int systolic = 100 + random.nextInt(40); // 100-140 mmHg
        int diastolic = 60 + random.nextInt(20); // 60-80 mmHg
        LocalDateTime timestamp = LocalDateTime.now();

        return new SensorData(patientId, heartRate, temperature, systolic, diastolic, timestamp);
    }
}

