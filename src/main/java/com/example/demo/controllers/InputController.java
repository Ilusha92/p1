package com.example.demo.controllers;

import com.example.demo.entities.InputHeader;
import com.example.demo.entities.InputBody;
import com.example.demo.services.InputService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class InputController {

    private final InputService inputService;

    @Autowired
    public InputController(InputService inputService) {
        this.inputService = inputService;
    }

    @GetMapping("/input/header")
    public String showInputHeader(Model model) {
        model.addAttribute("input", new InputHeader());
        return "inputHeader";
    }

    @PostMapping("/input/saveHeaderInput")
    public String saveInput(@Valid @ModelAttribute("inputHeader") InputHeader header
            , BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "inputHeader";
        }
        String currentUsername = authentication.getName();
        Long headerId = inputService.saveInputHeader(header, currentUsername);
        return "redirect:/input/body?headerId=" + headerId;
    }

    @GetMapping("/input/body")
    public String showInputBody(@RequestParam("headerId") Long headerId, Model model) {
        InputHeader header = inputService.getInputHeaderById(headerId);
        if (header == null) {
            return "errorPageHeader"; // Здесь errorPage - название вашего представления с сообщением об ошибке
        }
        model.addAttribute("headerId", headerId); // Добавляем inputId в контекст модели
        model.addAttribute("workDays", header.getWorkDays());
        model.addAttribute("inputBody", new InputBody());

        return "inputBody";
    }

    @PostMapping("/input/saveBodyInput")
    public String saveInputBody(@Valid @ModelAttribute("inputBody") List<InputBody> bodies,
                                @RequestParam("headerId") Long headerId,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            System.out.println("Binding result has errors:");
            bindingResult.getAllErrors().forEach(error -> System.out.println(error));
            return "errorBodyPage";
        }

        // Получаем InputHeader по его идентификатору
        InputHeader header = inputService.getInputHeaderById(headerId);
        if (header == null) {
            return "errorPageHeader"; // Обработка случая, когда header не найден
        }

        // Привязываем каждый InputBody к InputHeader
        for (InputBody body : bodies) {
            body.setHeader(header);
        }

        inputService.saveInputBody(bodies);
        return "redirect:/somepage?inputBodyId=" + headerId;
    }
}
