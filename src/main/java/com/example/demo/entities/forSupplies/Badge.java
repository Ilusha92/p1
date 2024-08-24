package com.example.demo.entities.forSupplies;

import com.example.demo.entities.Supplies;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String badgeMaterial; //материал изготовления - тут всего два варианта ПЕЧАТНЫЙ ПЛАСТИКОВЫЙ
    private String size; //размер - варианты для списка или вписываем вручную(можно оставлять пустым)
    private String chroma; //цветность - варианты для списка или вписываем вручную(можно оставлять пустым)
    private String density; //плотность - варианты для списка или вписываем вручную(можно оставлять пустым)
    private String lamination; //показател ламинации - варианты для списка или вписываем вручную(можно оставлять пустым)
    private String laminationKind; //тип ламинации - тут всего два варианта МАТОВЫЙ ГЛЯНЦЕВЫЙ
    private boolean rfid = false;
    private Integer price;
    private Integer count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplies_id", nullable = false)
    private Supplies supplies;

}
