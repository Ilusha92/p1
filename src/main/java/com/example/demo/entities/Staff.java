package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kindOfStaff;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer staffQuantity;
    private Integer betPerHour;

    @JsonBackReference
    @ManyToOne
    private InputHeader header;

}

