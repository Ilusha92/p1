package com.example.demo.entities.forSupplies;

import com.example.demo.entities.Supplies;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "stickers")
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stick;
    private String tape;
    private boolean transparent = false;
    private String price;
    private String count;

    @ManyToOne
    @JoinColumn(name = "supplies_id", nullable = false)
    private Supplies supplies;
}
