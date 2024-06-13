package com.example.demo.entities;

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
    private String localCount1;
    private String localPrice1;
    private String localCount2;
    private String localPrice2;
    private String localCount3;
    private String localPrice3;
    private String intercityCount1;
    private String intercityPrice1;
    private String intercityCount2;
    private String intercityPrice2;
    private String intercityCount3;
    private String intercityPrice3;
    private String habitationCount;
    private String habitationPrice;

    @OneToOne
    @JoinColumn(name = "header_id", nullable = false)
    private InputHeader header;

    @Override
    public String toString() {
        return "Logistic{" +
                "id=" + id +
                ", localCount1='" + localCount1 + '\'' +
                ", localPrice1='" + localPrice1 + '\'' +
                ", localCount2='" + localCount2 + '\'' +
                ", localPrice2='" + localPrice2 + '\'' +
                ", localCount3='" + localCount3 + '\'' +
                ", localPrice3='" + localPrice3 + '\'' +
                ", intercityCount1='" + intercityCount1 + '\'' +
                ", intercityPrice1='" + intercityPrice1 + '\'' +
                ", intercityCount2='" + intercityCount2 + '\'' +
                ", intercityPrice2='" + intercityPrice2 + '\'' +
                ", intercityCount3='" + intercityCount3 + '\'' +
                ", intercityPrice3='" + intercityPrice3 + '\'' +
                ", habitationCount='" + habitationCount + '\'' +
                ", habitationPrice='" + habitationPrice + '\'' +
                ", header=" + header +
                '}';
    }
}
