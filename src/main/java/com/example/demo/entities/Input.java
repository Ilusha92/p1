package com.example.demo.entities;

import lombok.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "input")
public class Input {
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
}
