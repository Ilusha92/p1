package com.example.demo.entities;

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
    private String customer;
    private String eventName;
    private String eventLocation;
    private LocalDate eventStartDate; // Поле для начальной даты события
    private LocalDate eventEndDate; // Поле для конечной даты события
    private LocalTime eventWorkStartTime;
    private LocalTime eventWorkEndTime;
    private int visitorsCount;
    private String author;
    private int workDays;
    private boolean sameEquipmentForAllDays;
    @OneToMany(mappedBy = "header")
    private List<InputBody> inputBodies;
    @OneToOne(mappedBy = "header")
    private Supplies sup;
    @OneToOne(mappedBy = "header")
    private Staff staff;
    @OneToOne(mappedBy = "header")
    private Logistic logistic;


}
