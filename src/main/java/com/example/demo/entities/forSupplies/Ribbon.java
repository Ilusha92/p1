package com.example.demo.entities.forSupplies;

import com.example.demo.entities.Supplies;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "ribbons")
public class Ribbon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String kind;
    private String price;
    private String count;

    @ManyToOne
    @JoinColumn(name = "supplies_id", nullable = false)
    private Supplies supplies;
}
