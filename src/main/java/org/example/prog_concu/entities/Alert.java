package org.example.prog_concu.entities;



import java.time.LocalDateTime;

public class Alert {
    private Long patientId;
    private String signeVital;
    private double valeurMesuree;
    private String seuilFranchi;
    private LocalDateTime timestamp;
    private boolean active;

    // Constructeur par défaut
    public Alert() {}

    // Constructeur paramétré
    public Alert(Long patientId, String signeVital, double valeurMesuree, String seuilFranchi, boolean active) {
        this.patientId = patientId;
        this.signeVital = signeVital;
        this.valeurMesuree = valeurMesuree;
        this.seuilFranchi = seuilFranchi;
        this.timestamp = LocalDateTime.now();
        this.active = active;
    }

    // Getters et Setters
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getSigneVital() { return signeVital; }
    public void setSigneVital(String signeVital) { this.signeVital = signeVital; }

    public double getValeurMesuree() { return valeurMesuree; }
    public void setValeurMesuree(double valeurMesuree) { this.valeurMesuree = valeurMesuree; }

    public String getSeuilFranchi() { return seuilFranchi; }
    public void setSeuilFranchi(String seuilFranchi) { this.seuilFranchi = seuilFranchi; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "Alert{" +
                "patientId=" + patientId +
                ", signeVital='" + signeVital + '\'' +
                ", valeurMesuree=" + valeurMesuree +
                ", seuilFranchi='" + seuilFranchi + '\'' +
                ", timestamp=" + timestamp +
                ", active=" + active +
                '}';
    }
}