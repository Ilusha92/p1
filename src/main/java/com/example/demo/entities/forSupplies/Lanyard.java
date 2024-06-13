package com.example.demo.entities.forSupplies;

import com.example.demo.entities.Supplies;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "lanyards")
public class Lanyard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String size; //размер
    private String application; //нанесение
    private String bracing;//крепление
    private String price;
    private String count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplies_id", nullable = false)
    private Supplies supplies;

}
