package org.example.prog_concu.simulator;

import org.example.prog_concu.entities.SensorData;
import org.example.prog_concu.repository.SensorDataRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

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
                sensorDataRepository.save(data);

                System.out.println("✅ Donnée simulée enregistrée pour le patient " + patientId + " : " + data);

                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                System.out.println("⛔ Simulation interrompue pour le patient " + patientId);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.out.println("❌ Erreur lors de la simulation pour le patient " + patientId);
                e.printStackTrace();
            }
        }
        System.out.println("🛑 Arrêt complet de la simulation pour le patient " + patientId);
    }

    public void stop() {
        running = false;
    }

    private SensorData generateSensorData() {
        boolean generateAbnormal = random.nextDouble() < 0.3;

        int heartRate;
        double temperature;
        int systolic;
        int diastolic;

        if (generateAbnormal) {
            heartRate = random.nextBoolean() ?
                    random.nextInt(30) + 30 :
                    random.nextInt(50) + 110;

            temperature = random.nextBoolean() ?
                    35.0 + random.nextDouble() :
                    37.6 + random.nextDouble() * 2;

            systolic = random.nextBoolean() ?
                    random.nextInt(20) + 70 :
                    random.nextInt(50) + 150;

            diastolic = random.nextBoolean() ?
                    random.nextInt(15) + 40 :
                    random.nextInt(30) + 95;

            System.out.println("⚠️ Valeurs ANORMALES générées pour patient " + patientId);
        } else {
            heartRate = 60 + random.nextInt(40);
            temperature = 36.0 + random.nextDouble() * 1.5;
            systolic = 90 + random.nextInt(50);
            diastolic = 60 + random.nextInt(30);
        }

        temperature = Math.round(temperature * 10.0) / 10.0;

        return new SensorData(
                patientId,
                heartRate,
                temperature,
                systolic,
                diastolic,
                LocalDateTime.now()
        );
    }
}
