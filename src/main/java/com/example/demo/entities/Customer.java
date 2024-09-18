package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "customers")
public class Customer {

    @Id
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String inn;
    private String ogrn;
    private String kpp;
    private String registrationDate;
    private String address;
    private String generalManager;

    @JsonBackReference
    @OneToMany(mappedBy = "customer")
    private List<Manager> managers;

    @JsonBackReference
    @OneToMany(mappedBy = "customer")
    private List<InputHeader> headers;
}
