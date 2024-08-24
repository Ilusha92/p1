package com.example.demo.entities;

import com.example.demo.entities.forSupplies.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Builder
@AllArgsConstructor
@Entity(name = "supplies")
public class Supplies {

    public Supplies() {
        this.badges = new ArrayList<>();
        this.lanyards = new ArrayList<>();
        this.bracers = new ArrayList<>();
        this.inserts = new ArrayList<>();
        this.pockets = new ArrayList<>();
        this.ribbons = new ArrayList<>();
        this.stickers = new ArrayList<>();
        this.asups = new ArrayList<>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "supplies", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Badge> badges;

    @OneToMany(mappedBy = "supplies", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lanyard> lanyards;

    @OneToMany(mappedBy = "supplies",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bracer> bracers;

    @OneToMany(mappedBy = "supplies",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Insert> inserts;

    @OneToMany(mappedBy = "supplies",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pocket> pockets;

    @OneToMany(mappedBy = "supplies",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ribbon> ribbons;

    @OneToMany(mappedBy = "supplies",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sticker> stickers;

    @OneToMany(mappedBy = "supplies",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Asup> asups;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "header_id", nullable = false)
    private InputHeader header;

}
