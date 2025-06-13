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
        // 30% de chance de générer des valeurs anormales pour tester les alertes
        boolean generateAbnormal = random.nextDouble() < 0.3;
        
        int heartRate;
        double temperature;
        int systolic;
        int diastolic;
        
        if (generateAbnormal) {
            // Générer des valeurs anormales
            heartRate = random.nextBoolean() ? 
                random.nextInt(30) + 30 :  // Bradycardie (30-60)
                random.nextInt(50) + 110;  // Tachycardie (110-160)
            
            temperature = random.nextBoolean() ? 
                35.0 + random.nextDouble() :      // Hypothermie (35.0-36.0)
                37.6 + random.nextDouble() * 2;  // Hyperthermie (37.6-39.6)
            
            systolic = random.nextBoolean() ? 
                random.nextInt(20) + 70 :   // Hypotension (70-90)
                random.nextInt(50) + 150;   // Hypertension (150-200)
            
            diastolic = random.nextBoolean() ? 
                random.nextInt(15) + 40 :   // Hypotension (40-55)
                random.nextInt(30) + 95;    // Hypertension (95-125)
                
            System.out.println("⚠️ Valeurs ANORMALES générées pour patient " + patientId);
        } else {
            // Valeurs normales
            heartRate = 60 + random.nextInt(40);              // 60-100 bpm
            temperature = 36.0 + random.nextDouble() * 1.5;   // 36.0-37.5°C
            systolic = 90 + random.nextInt(50);               // 90-140 mmHg
            diastolic = 60 + random.nextInt(30);              // 60-90 mmHg
        }
        
        return new SensorData(
                patientId,
                heartRate,
                Math.round(temperature * 10.0) / 10.0, // Arrondir à 1 décimale
                systolic,
                diastolic,
                LocalDateTime.now()
        );
    }
}