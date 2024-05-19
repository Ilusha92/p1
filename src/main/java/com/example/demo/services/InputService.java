package com.example.demo.services;

import com.example.demo.entities.InputBody;
import com.example.demo.entities.InputHeader;
import com.example.demo.entities.Supplies;
import com.example.demo.entities.forSupplies.Badge;

import java.util.List;

public interface InputService {

    Long saveInputHeader(InputHeader header, String username);

    InputHeader getInputHeaderById(Long id);

    void saveInputBody(List<InputBody> bodies);

    InputBody getInputBodyById(Long id);

    void saveInputSupplies(Supplies sup);

    void saveInputSuppliesBadges(List<Badge> badges);

    Badge saveBadge(Badge badge);





    void editInput(InputHeader header);



    List<InputHeader> getUserInputs();

}
