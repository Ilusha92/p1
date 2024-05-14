package com.example.demo.controllers;

import com.example.demo.entities.HeaderBodies;
import com.example.demo.entities.InputHeader;
import com.example.demo.entities.InputBody;
import com.example.demo.services.InputService;
import com.example.demo.services.InputServiceImpl;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
public class InputController {

    private final InputServiceImpl inputService;

    @Autowired
    public InputController(InputServiceImpl inputService) {
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

        // Создаем список пустых объектов InputBody в количестве workDays
        List<InputBody> inputBodies = new ArrayList<>();
        for (int i = 0; i < header.getWorkDays(); i++) {
            inputBodies.add(new InputBody());
        }

        HeaderBodies headerBodies = new HeaderBodies(inputBodies);
        model.addAttribute("headerId", headerId); // Добавляем headerId в контекст модели
        model.addAttribute("workDays", header.getWorkDays());
        model.addAttribute("headerBodies", headerBodies);

        return "inputBody";
    }


    @PostMapping("/input/saveBodyInput")
    public String saveInputBody(@Valid @ModelAttribute HeaderBodies bodies,
                                @RequestParam("headerId") Long headerId,
                                BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            System.out.println("Binding result has errors:");
            bindingResult.getAllErrors().forEach(error -> System.out.println(error));
            return "errorBodyPage";
        }

        InputHeader header = inputService.getInputHeaderById(headerId);
        if (header == null) {
            return "errorPageHeader";
        }

        for (InputBody body : bodies.getBodies()) {
            body.setHeader(header);
        }
        model.addAttribute("bodies", bodies);
        inputService.saveInputBody(bodies.getBodies());

        return "redirect:/somepage?inputBodyId=" + headerId;
    }

}
