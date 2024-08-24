package com.example.demo.services;

import com.example.demo.entities.*;
import com.example.demo.entities.forSupplies.*;

import java.util.List;

public interface InputService {

    Long saveInputHeader(InputHeader header, String username);

    void saveInputBody(List<InputBody> bodies);

    void saveInputSupplies(Supplies sup);

    Badge saveBadge(Badge badge);

    Lanyard saveLanyard(Lanyard lanyard);

    Bracer saveBracer(Bracer bracer);

    Insert saveInsert(Insert insert);

    Pocket savePocket(Pocket pocket);

    Ribbon saveRibbon(Ribbon ribbon);

    Sticker saveSticker(Sticker sticker);

    Mounting saveMounting(Mounting mounting);

    Staff saveStaff(Staff staff);

    Logistic saveLogistic(Logistic logistic);

    Asup saveAsup(Asup asup);

    void editInput(InputHeader header);

    List<InputHeader> getUserInputs();

    InputBody getInputBodyById(Long id);

    Supplies getSuppliesById(Long id);

    void saveInputSuppliesBadges(List<Badge> badges);


    InputHeader getInputHeaderById(Long id);

}
