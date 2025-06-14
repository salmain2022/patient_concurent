package org.example.prog_concu;

import org.example.prog_concu.entities.Patient;
import org.example.prog_concu.repository.PatientRepository;
import org.example.prog_concu.Service.MonitoringManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class ProgConcuApplication {

    private static final ConcurrentHashMap<Long, Patient> patientCache = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(ProgConcuApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(PatientRepository repository, MonitoringManager monitoringManager) {
        return args -> {
            // Création et sauvegarde du patient initial
            Patient savedPatient = repository.save(
                    Patient.builder()
                            .nom("Belhad")
                            .prenom("Houda")
                            .dateNaissance(LocalDate.of(1999, 3, 14))
                            .serviceHospitalier("Cardiologie")
                            .build()
            );

            patientCache.put(savedPatient.getId(), savedPatient);
            System.out.println("✅ Patient initial ajouté au cache (ID: " + savedPatient.getId() + ")");

            // Facultatif : démarrage automatique de la surveillance
            monitoringManager.startMonitoring(savedPatient.getId());

            System.out.println("📋 Liste des patients (base de données):");
            repository.findAll().forEach(System.out::println);

            // Simulation multi-thread (indépendante du monitoring des signes vitaux)
            Thread simulationThread = new Thread(() -> {
                try {
                    startSimulation();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            simulationThread.setDaemon(false);
            simulationThread.start();
        };
    }

    private void startSimulation() {
        System.out.println("\n🚀 Démarrage de la simulation concurrente");

        ExecutorService executor = Executors.newFixedThreadPool(4);

        executor.submit(() -> {
            for (int i = 1; i <= 5; i++) {
                Patient newPatient = Patient.builder()
                        .nom("Patient_Concurrent_" + i)
                        .prenom("Simulation")
                        .dateNaissance(LocalDate.now().minusYears(i))
                        .serviceHospitalier("Service_" + i)
                        .build();

                long newId = 100L + i;
                patientCache.put(newId, newPatient);

                System.out.println("[WRITE] Thread " + Thread.currentThread().getId()
                        + " a ajouté: " + newPatient.getNom() + " (ID: " + newId + ")");

                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        executor.submit(() -> {
            for (int i = 0; i < 3; i++) {
                System.out.println("[READ] Thread " + Thread.currentThread().getId()
                        + " - Contenu du cache (" + patientCache.size() + " patients):");

                patientCache.forEach((id, patient) ->
                        System.out.println("  • " + id + ": " + patient.getNom())
                );

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        executor.submit(() -> {
            for (int i = 1; i <= 3; i++) {
                int finalI = i;
                patientCache.computeIfPresent(1L, (id, patient) -> {
                    String newService = "Cardio_Updated_" + finalI;
                    patient.setServiceHospitalier(newService);

                    System.out.println("[UPDATE] Thread " + Thread.currentThread().getId()
                            + " a modifié le service du patient ID 1: " + newService);

                    return patient;
                });

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        executor.submit(() -> {
            try {
                Thread.sleep(500);

                for (long id = 101L; id <= 103L; id++) {
                    Patient removed = patientCache.remove(id);
                    if (removed != null) {
                        System.out.println("[DELETE] Thread " + Thread.currentThread().getId()
                                + " a supprimé: " + removed.getNom() + " (ID: " + id + ")");
                    }
                    Thread.sleep(250);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\n✅ Simulation terminée");
        System.out.println("📊 État final du cache (" + patientCache.size() + " patients):");
        patientCache.forEach((id, patient) ->
                System.out.println("  • " + id + ": " + patient.getNom() + " (" + patient.getServiceHospitalier() + ")")
        );
    }
}
