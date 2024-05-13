package com.example.demo.services;

import com.example.demo.entities.InputBody;
import com.example.demo.entities.InputHeader;

import java.util.List;

public interface InputService {

    Long createInputHeader(InputHeader header);

    Long saveInputHeader(InputHeader header, String username);

    InputHeader getInputHeaderById(Long id);

    void saveInputBody(List<InputBody> bodies);

    InputBody getInputBodyById(Long id);





    // Редактирование существующей записи InputHeader в базе данных
    void editInput(InputHeader header);



    List<InputHeader> getUserInputs();

}
