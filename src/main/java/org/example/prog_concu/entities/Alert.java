package org.example.prog_concu.entities;



import java.time.LocalDateTime;

public class Alert {

    private Long patientId;
    private String signeVital;
    private double valeur;
    private boolean active;
    private LocalDateTime dateCreation;

    // Constructeur complet
    public Alert(Long patientId, String signeVital, double valeur, boolean active, LocalDateTime dateCreation) {
        this.patientId = patientId;
        this.signeVital = signeVital;
        this.valeur = valeur;
        this.active = active;
        this.dateCreation = dateCreation;
    }

    // getters et setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getSigneVital() {
        return signeVital;
    }

    public void setSigneVital(String signeVital) {
        this.signeVital = signeVital;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}

//    @Override
//    public String toString() {
//        return "Alert{" +
//                "patientId=" + patientId +
//                ", signeVital='" + signeVital + '\'' +
//                ", valeurMesuree=" + valeurMesuree +
//                ", seuilFranchi='" + seuilFranchi + '\'' +
//                ", timestamp=" + timestamp +
//                ", active=" + active +
//                '}';
//    }
//}
