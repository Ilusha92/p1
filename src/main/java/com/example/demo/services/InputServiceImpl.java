package com.example.demo.services;

import com.example.demo.entities.Input;
import com.example.demo.repository.InputRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InputServiceImpl implements InputService {
    private final InputRepository inputRepository;
    //private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void createInput(Input input) {
        // Вызываем метод сохранения объекта в репозитории
        //bCryptPasswordEncoder.encode(password)
        inputRepository.save(input);
    }

    @Override
    public void editInput(Input input) {
        // Проверяем, существует ли объект Input с таким ID в репозитории
        // и сохраняем отредактированный объект
        if (inputRepository.existsById(input.getId())) {
            inputRepository.save(input);
        } else {
            throw new IllegalArgumentException("Input с указанным ID не найден");
        }
    }

    @Override
    public Input getInputById(Long id) {
        // Ищем объект Input по его ID в репозитории и возвращаем его
        return inputRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Input с указанным ID не найден"));
    }

    @Override
    public void saveInput(Input input) {
        // Вызываем метод создания Input, так как в данной реализации нет разделения на create и edit
        createInput(input);
    }

    @Override
    public List<Input> getUserInputs() {
        return null;
    }

}
