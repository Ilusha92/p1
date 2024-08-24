package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "input_header")
public class InputHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customer;
    private String eventName;
    private String eventLocation;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private LocalTime eventWorkStartTime;
    private LocalTime eventWorkEndTime;
    private int visitorsCount;
    private String author;
    private Integer workDays;
    private boolean sameEquipmentForAllDays;

    @JsonManagedReference
    @OneToMany(mappedBy = "header")
    private List<InputBody> inputBodies;

    @JsonManagedReference
    @OneToOne(mappedBy = "header")
    private Supplies sup;

    @JsonManagedReference
    @OneToOne(mappedBy = "header")
    private Mounting mounting;

    @JsonManagedReference
    @OneToMany(mappedBy = "header")
    private List<Staff> staffs;

    @JsonManagedReference
    @OneToMany(mappedBy = "header")
    private List<Logistic> logistic;

    private LocalDate boolStart1;
    private LocalDate boolStart2;
    private LocalDate boolStart3;
    private LocalDate boolStart4;
    private LocalDate boolEnd1;
    private LocalDate boolEnd2;
    private LocalDate boolEnd3;
    private LocalDate boolEnd4;

    private Integer firstFormula;
    private Integer secondFormula;
    private Integer thirdFormula;
    private Integer fourthFormula;
    private Integer fifthFormula;
    private Integer sixthFormula;

}
