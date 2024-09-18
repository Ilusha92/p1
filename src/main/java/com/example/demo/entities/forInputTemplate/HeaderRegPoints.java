package com.example.demo.entities.forInputTemplate;

import com.example.demo.entities.RegPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class HeaderRegPoints {

    List<RegPoint> regPoints;
}
