package org.example.prog_concu;

import org.example.prog_concu.entities.Patient;
import org.example.prog_concu.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class ProgConcuApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProgConcuApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(PatientRepository repository) {
        return args -> {
            repository.save(
                Patient.builder()
                    .nom("Belhad")
                    .prenom("Houda")
                    .dateNaissance(LocalDate.of(1999, 3, 14))
                    .serviceHospitalier("Cardiologie")
                    .build()
            );

            System.out.println("📋 Liste des patients :");
            repository.findAll().forEach(System.out::println);
        };
    }
}
