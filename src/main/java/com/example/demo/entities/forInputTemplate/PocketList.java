package com.example.demo.entities.forInputTemplate;

import com.example.demo.entities.forSupplies.Pocket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PocketList {

    List<Pocket> pockets;
}
