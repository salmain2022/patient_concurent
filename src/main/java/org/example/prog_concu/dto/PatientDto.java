package org.example.prog_concu.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Data
public class PatientDto {

    private Long id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String serviceHospitalier;
}

