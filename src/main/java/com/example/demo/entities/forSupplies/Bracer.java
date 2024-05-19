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
//@Entity(name = "bracers")
//public class Bracer {
//
//    @Id
//    private Long id;
//
//    private String length; //длина
//    private String width; //ширина
//    private String application; //нанесение
//    private String material; //материал
//    private String type; //контрольный, многоразовый
//    private boolean rfid; //с RFID или без
//
//    @ManyToOne
//    @JoinColumn(name = "supplies_id", nullable = false)
//    private Supplies sup;
//}
