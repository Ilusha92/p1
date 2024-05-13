package com.example.demo.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "badges")
public class Badge {

    @Id
    private Long id;
    private String size;
    private String chroma;
    private String density;
    private String lamination;
    private String laminationKind;

}
