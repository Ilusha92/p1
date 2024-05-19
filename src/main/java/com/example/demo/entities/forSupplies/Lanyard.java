//package com.example.demo.entities.forSupplies;
//
//import com.example.demo.entities.Supplies;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import lombok.*;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor(access = AccessLevel.PUBLIC)
//@Entity(name = "lanyards")
//public class Lanyard {
//
//    @Id
//    private Long id;
//    private String size; //размер
//    private String application; //нанесение
//    private String bracing;//крепление
//
//    @ManyToOne
//    @JoinColumn(name = "supplies_id", nullable = false)
//    private Supplies sup;
//}
