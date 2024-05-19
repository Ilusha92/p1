//package com.example.demo.entities.forSupplies;
//
//import com.example.demo.entities.Supplies;
//import jakarta.persistence.*;
//import lombok.*;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor(access = AccessLevel.PUBLIC)
//@Entity(name = "inserts")
//public class Insert {
//
//    @Id
//    private Long id;
//    private String size; //размер
//    private String chroma; //цветность
//    private String density; //плотность
//    private String lamination; //показател ламинации
//
//    @Enumerated(EnumType.STRING)
//    private LaminationKind laminationKind; //тип ламинации
//
//    @ManyToOne
//    @JoinColumn(name = "supplies_id", nullable = false)
//    private Supplies sup;
//}
