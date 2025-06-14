package org.example.prog_concu.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String serviceHospitalier;

    public Patient() {}

    public Patient(Long id, String nom, String prenom, LocalDate dateNaissance, String serviceHospitalier) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.serviceHospitalier = serviceHospitalier;
    }

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getServiceHospitalier() { return serviceHospitalier; }
    public void setServiceHospitalier(String serviceHospitalier) { this.serviceHospitalier = serviceHospitalier; }

    // Builder manuel (optionnel)
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String nom;
        private String prenom;
        private LocalDate dateNaissance;
        private String serviceHospitalier;

        public Builder nom(String nom) { this.nom = nom; return this; }
        public Builder prenom(String prenom) { this.prenom = prenom; return this; }
        public Builder dateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; return this; }
        public Builder serviceHospitalier(String serviceHospitalier) { this.serviceHospitalier = serviceHospitalier; return this; }

        public Patient build() {
            return new Patient(null, nom, prenom, dateNaissance, serviceHospitalier);
        }
    }
}
