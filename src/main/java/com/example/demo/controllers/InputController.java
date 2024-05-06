package com.example.demo.controllers;

import com.example.demo.entities.Input;
import com.example.demo.services.InputService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class InputController {

    private final InputService inputService;

    @Autowired
    public InputController(InputService inputService) {
        this.inputService = inputService;
    }

    @GetMapping("/input/form")
    public String showInputForm(Model model) {
        model.addAttribute("input", new Input());
        return "inputForm";
    }

    @PostMapping("/input/saveInput")
    public String saveInput(@Valid @ModelAttribute("input") Input input, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Если есть ошибки валидации, возвращаем обратно на форму ввода с сообщениями об ошибках
            return "inputForm";
        }

        inputService.saveInput(input);
        return "redirect:/input/form";
    }

//    @GetMapping("/lk")
//    public String showInputs(Model model) {
//        // Получаем имеющиеся у пользователя input'ы из сервиса
//        List<Input> userInputs = inputService.getUserInputs(); // Предполагается, что есть метод в InputService для получения input'ов пользователя
//        model.addAttribute("userInputs", userInputs);
//        return "userInputsPage"; // Предполагается, что у вас есть страница для отображения input'ов пользователя
//    }
}
