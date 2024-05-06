package com.example.demo.services;

import com.example.demo.entities.Input; // Правильный импорт

import java.util.List;

public interface InputService {
    // Создание новой записи Input в базе данных
    void createInput(Input input);

    // Редактирование существующей записи Input в базе данных
    void editInput(Input input);

    Input getInputById(Long id);

    // Сохранение записи Input в базе данных
    void saveInput(Input input);

    List<Input> getUserInputs();

}
