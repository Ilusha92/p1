package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "device")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer priceFor1q;
    private Integer count;

    @ManyToOne
    @JoinColumn(name = "input_body_id")
    private InputBody body;

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", priceFor1q=" + priceFor1q +
                ", count=" + count +
                ", body=" + body +
                '}';
    }
}