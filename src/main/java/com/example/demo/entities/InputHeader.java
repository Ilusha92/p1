package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
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

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String eventName;
    private String eventLocation;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private LocalTime eventWorkStartTime;
    private LocalTime eventWorkEndTime;
    private Integer visitorsCount;
    private String author;
    private Integer workDays;
    private Integer periods;
    private boolean sameEquipmentForAllDays;
    private boolean withManyRegPoints;

    private LocalDate prePrintStart;
    private LocalDate prePrintEnd;

    private LocalDate boolStart1;
    private LocalDate boolStart2;
    private LocalDate boolStart3;
    private LocalDate boolStart4;
    private LocalDate boolEnd1;
    private LocalDate boolEnd2;
    private LocalDate boolEnd3;
    private LocalDate boolEnd4;

    @JsonManagedReference
    @OneToMany(mappedBy = "header")
    private List<InputBody> inputBodies;

    @JsonManagedReference
    @OneToOne(mappedBy = "header")
    private Supplies sup;

    @JsonManagedReference
    @OneToMany(mappedBy = "header")
    private List<Mounting> mountings;

    @JsonManagedReference
    @OneToMany(mappedBy = "header")
    private List<InputStaff> inputStaffs;

    @JsonManagedReference
    @OneToMany(mappedBy = "header")
    private List<Logistic> logistic;

    @JsonManagedReference
    @OneToMany(mappedBy = "header")
    private List<RegPoint> regPoints;

    @JsonManagedReference
    @OneToMany(mappedBy = "header")
    private List<AdditionalSale> aSales;



}
