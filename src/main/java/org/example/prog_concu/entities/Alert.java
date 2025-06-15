package org.example.prog_concu.entities;



import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long patientId;
    private String signeVital;
    private Double valeurMesuree;
    private Double seuilFranchi;
    private LocalDateTime timestamp;
    private boolean active = true;

     public String getMessage() {
        return String.format("%s anormal: %.1f (seuil: %.1f)", 
                signeVital, valeurMesuree, seuilFranchi);
    }
    @Override
    public String toString() {
        return "Alert{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", signeVital='" + signeVital + '\'' +
                ", valeurMesuree=" + valeurMesuree +
                ", seuilFranchi=" + seuilFranchi +
                ", timestamp=" + timestamp +
                ", active=" + active +
                '}';
    }
}