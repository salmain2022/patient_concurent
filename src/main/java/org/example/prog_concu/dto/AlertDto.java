package org.example.prog_concu.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Data
public class AlertDto {
    private Long patientId;
    private String signeVital;
    private double valeurMesuree;
    private String seuilFranchi;
    private LocalDateTime timestamp;
    private boolean active;



    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    
}
