package com.example.demo.entities.forInputTemplate;

import com.example.demo.entities.forSupplies.Badge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BadgeList {

    private List<Badge> badges;

}
