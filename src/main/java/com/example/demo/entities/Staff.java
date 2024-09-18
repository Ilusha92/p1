package com.example.demo.entities;

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

    @ManyToOne
    @JoinColumn(name = "input_staff_id")
    private InputStaff staff;

}

