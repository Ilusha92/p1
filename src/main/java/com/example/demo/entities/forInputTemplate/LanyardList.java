package com.example.demo.entities.forInputTemplate;

import com.example.demo.entities.forSupplies.Lanyard;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LanyardList {

    private List<Lanyard> lanyards;
}
