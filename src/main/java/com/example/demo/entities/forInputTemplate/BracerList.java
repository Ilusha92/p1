package com.example.demo.entities.forInputTemplate;

import com.example.demo.entities.forSupplies.Bracer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BracerList {

    private List<Bracer> bracers;
}
