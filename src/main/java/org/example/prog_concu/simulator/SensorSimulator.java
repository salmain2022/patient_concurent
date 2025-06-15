package org.example.prog_concu.simulator;

import org.example.prog_concu.entities.SensorData;
import org.example.prog_concu.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // Important: permet la création de nouvelles instances
public class SensorSimulator implements Runnable {

    private final Random random = new Random();
    
    // Propriétés injectées via constructeur ou setters
    private Long patientId;
    private Map<Long, SensorData> sharedSensorDataMap;
    private boolean consoleMode = true; // Mode silencieux par défaut
    
    @Autowired
    private SensorDataRepository sensorDataRepository;

    // Constructeur par défaut pour Spring
    public SensorSimulator() {
    }

    // Constructeur avec parametres
    public SensorSimulator(Long patientId, Map<Long, SensorData> sharedSensorDataMap, 
                          SensorDataRepository sensorDataRepository) {
        this.patientId = patientId;
        this.sharedSensorDataMap = sharedSensorDataMap;
        this.sensorDataRepository = sensorDataRepository;
    }

    @Override
    public void run() {
        try {
            
            SensorData data = generateSensorData();
            
            // Sauvegarder dans la map partagée
            sharedSensorDataMap.put(patientId, data);
            
            // Sauvegarder en base de données
            sensorDataRepository.save(data);

            // Log seulement si pas en mode console
            if (!consoleMode) {
                System.out.println("✅ Donnée simulée enregistrée pour le patient " + patientId + " : " + data);
            }

        } catch (Exception e) {
            if (!consoleMode) {
                System.out.println("❌ Erreur lors de la simulation pour le patient " + patientId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public SensorData generateSensorData() {
        // 30% de chance de générer des valeurs anormales
        boolean generateAbnormal = random.nextDouble() < 0.3;

        int heartRate;
        double temperature;
        int systolic;
        int diastolic;

        if (generateAbnormal) {
            // Valeurs anormales
            heartRate = random.nextBoolean() ?
                    random.nextInt(30) + 30 :  // Bradycardie (30-60)
                    random.nextInt(50) + 110;  // Tachycardie (110-160)

            temperature = random.nextBoolean() ?
                    35.0 + random.nextDouble() :           // Hypothermie (35-36)
                    37.6 + random.nextDouble() * 2;        // Fièvre (37.6-39.6)

            systolic = random.nextBoolean() ?
                    random.nextInt(20) + 70 :   // Hypotension (70-90)
                    random.nextInt(50) + 150;   // Hypertension (150-200)

            diastolic = random.nextBoolean() ?
                    random.nextInt(15) + 40 :   // Hypotension (40-55)
                    random.nextInt(30) + 95;    // Hypertension (95-125)

            if (!consoleMode) {
                System.out.println("⚠️ Valeurs ANORMALES générées pour patient " + patientId);
            }
        } else {
            // Valeurs normales
            heartRate = 60 + random.nextInt(40);     // 60-100 bpm
            temperature = 36.0 + random.nextDouble() * 1.5; // 36.0-37.5°C
            systolic = 90 + random.nextInt(50);      // 90-140 mmHg
            diastolic = 60 + random.nextInt(30);     // 60-90 mmHg
        }

        // Arrondir la température à 1 décimale
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

    // Setters pour l'injection
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public void setSharedSensorDataMap(Map<Long, SensorData> sharedSensorDataMap) {
        this.sharedSensorDataMap = sharedSensorDataMap;
    }

    public void setConsoleMode(boolean consoleMode) {
        this.consoleMode = consoleMode;
    }

    public Long getPatientId() {
        return patientId;
    }
}