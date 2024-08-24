package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "logistic")
public class Logistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private Integer price;
    private Integer count;

    @JsonBackReference
    @ManyToOne
    private InputHeader header;

    @Override
    public String toString() {
        return "Logistic{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", count=" + count +
                ", header=" + header +
                '}';
    }
}
