package com.example.demo.entities.forSupplies;

import com.example.demo.entities.Supplies;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "bracers")
public class Bracer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String length; //длина
    private String width; //ширина
    private String application; //нанесение
    private String material; //материал
    private String color;
    private String type;
    private boolean rfid = false; //с RFID или без
    private Integer price;
    private Integer count;

    @ManyToOne
    @JoinColumn(name = "supplies_id", nullable = false)
    private Supplies supplies;
}
