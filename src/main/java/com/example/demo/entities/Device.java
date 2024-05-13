package com.example.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "device")
public class Device {

    @Id
    private Long id;
    private String name;
    private int priceFor1q;
}