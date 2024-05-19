package com.example.demo.entities;

import com.example.demo.entities.forSupplies.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "supplies")
public class Supplies {

    public Supplies(List<Badge> badges, InputHeader header) {
        this.badges = badges;
        this.header = header;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "sup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Badge> badges;

    @OneToOne
    @JoinColumn(name = "header_id", nullable = false)
    private InputHeader header;

    @Override
    public String toString() {
        return "Supplies{" +
                "id=" + id +
                ", badges=" + badges +
                ", header=" + header +
                '}';
    }

    //    @OneToMany(mappedBy = "sup")
//    private List<Bracer> bracers;
//
//    @OneToMany(mappedBy = "sup")
//    private List<Insert> inserts;
//
//    @OneToMany(mappedBy = "sup")
//    private List<Lanyard> lanyards;
//
//    @OneToMany(mappedBy = "sup")
//    private List<Pocket> pockets;
//
//    @OneToMany(mappedBy = "sup")
//    private List<Sticker> stickers;
//
//    @OneToMany(mappedBy = "sup")
//    private List<Ribbon> ribbons;
}
