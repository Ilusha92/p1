package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "mounting")
public class Mounting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kindOfService;
    private Integer workHours;
    private Integer agentCount;
    private Integer priceForHour;

    @JsonBackReference
    @ManyToOne
    private InputHeader header;
}
