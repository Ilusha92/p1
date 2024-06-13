package com.example.demo.entities.forInputTemplate;

import com.example.demo.entities.forSupplies.Insert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class InsertList {
    List<Insert> inserts;
}
