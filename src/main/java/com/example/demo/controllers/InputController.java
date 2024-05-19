package com.example.demo.controllers;

import com.example.demo.entities.*;
import com.example.demo.entities.forSupplies.Badge;
import com.example.demo.repository.DeviceRepository;
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
import java.util.List;

@Controller
public class InputController {

    private final InputServiceImpl inputService;
    private final DeviceRepository deviceRepository;

    @Autowired
    public InputController(InputServiceImpl inputService,DeviceRepository deviceRepository) {
        this.inputService = inputService;
        this.deviceRepository = deviceRepository;
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
        List<Device> softDevices = deviceRepository.findByIdIn(List.of(1L, 2L, 3L));
        List<Device> printerDevices = deviceRepository.findByIdIn(List.of(4L, 5L));

        List<InputBody> inputBodies = new ArrayList<>();
        for (int i = 0; i < header.getWorkDays(); i++) {
            InputBody body = new InputBody();
            body.setHeader(header);
            inputBodies.add(body);
        }

        HeaderBodies headerBodies = new HeaderBodies(inputBodies);
        model.addAttribute("softDevices", softDevices);
        model.addAttribute("printerDevices", printerDevices);
        model.addAttribute("headerId", headerId);
        model.addAttribute("headerBodies", headerBodies);
        model.addAttribute("sameEquipmentForAllDays", header.isSameEquipmentForAllDays());
        return "inputBody";
    }

    @PostMapping("/input/saveBodyInput")
    public String saveInputBody(@Valid @ModelAttribute HeaderBodies bodies,
                                @RequestParam("headerId") Long headerId,
                                @RequestParam("sameEquipmentForAllDays") boolean sameEquipmentForAllDays,
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

        List<InputBody> bodiesToSave = new ArrayList<>();
        if (sameEquipmentForAllDays) {
            InputBody templateBody = bodies.getBodies().get(0);
            for (int i = 0; i < header.getWorkDays(); i++) {
                InputBody body = new InputBody();
                body.setSoftDevice(templateBody.getSoftDevice());
                body.setPrinterDevice(templateBody.getPrinterDevice());
                body.setSDeviceCount(templateBody.getSDeviceCount());
                body.setSDevicePrice(templateBody.getSDevicePrice());
                body.setPDeviceCount(templateBody.getPDeviceCount());
                body.setPDevicePrice(templateBody.getPDevicePrice());
                body.setCameraDeviceCount(templateBody.getCameraDeviceCount());
                body.setCameraDevicePrice(templateBody.getCameraDevicePrice());
                body.setHeader(header);
                bodiesToSave.add(body);
            }
        } else {
            for (InputBody body : bodies.getBodies()) {
                body.setHeader(header);
                bodiesToSave.add(body);
            }
        }

        model.addAttribute("bodies", bodiesToSave);
        inputService.saveInputBody(bodiesToSave);

        return "redirect:/input/supplies?headerId=" + headerId;
    }

//    @GetMapping("/input/supplies")
//    public String showInputSupplies(@RequestParam("headerId") Long headerId, Model model) {
//        InputHeader header = inputService.getInputHeaderById(headerId);
//        if (header == null) {
//            return "errorPageHeader"; // Здесь errorPage - название вашего представления с сообщением об ошибке
//        }
//        List<Badge> inputBadges = new ArrayList<>();
//        Badge badge1 = new Badge();
//        Badge badge2 = new Badge();
//        inputBadges.add(badge1);
//        inputBadges.add(badge2);
//
//        Supplies sup = new Supplies();
//        sup.setBadges(inputBadges);
//        model.addAttribute("supplies", sup);
//        model.addAttribute("badges", inputBadges);
//        model.addAttribute("headerId", headerId);
//        System.out.println(sup);
//        return "inputSupplies";
//    }
//
//    @PostMapping("/input/saveSupplies")
//    public String saveInputSupplies(@Valid @ModelAttribute Supplies sup,
//                                @RequestParam("headerId")Long headerId,
//                                BindingResult bindingResult, Model model) {
//        if (bindingResult.hasErrors()) {
//            System.out.println("Binding result has errors:");
//            bindingResult.getAllErrors().forEach(error -> System.out.println(error));
//            return "errorBodyPage";
//        }
//
//        InputHeader header = inputService.getInputHeaderById(headerId);
//        if (header == null) {
//            return "errorPageHeader";
//        }
//        List<Badge> badgesToSave = new ArrayList<>();
//        Badge templateBadge = sup.getBadges().get(0);
//        for (int i = 0; i < header.getWorkDays(); i++) {
//            Badge badge = new Badge();
//            badge.setSize(templateBadge.getSize());
//            badge.setBadgeMaterial(templateBadge.getBadgeMaterial());
//            badge.setChroma(templateBadge.getChroma());
//            badge.setDensity(templateBadge.getDensity());
//            badge.setLamination(templateBadge.getLamination());
//            badge.setLaminationKind(templateBadge.getLaminationKind());
//            badge.setSup(sup);
//            badgesToSave.add(badge);
//        }
//
//        sup.setHeader(header);
//        sup.setBadges(badgesToSave);
//        //model.addAttribute("badges", badgesToSave);
//        System.out.println(sup);
//        inputService.saveInputSupplies(sup);
//
//        return "redirect:/staff?headerId=" + headerId;
//    }

//    @GetMapping("/input/supplies")
//    public String showInputSupplies(@RequestParam("headerId") Long headerId, Model model) {
//        InputHeader header = inputService.getInputHeaderById(headerId);
//        if (header == null) {
//            return "errorPageHeader"; // Название представления с сообщением об ошибке
//        }
//
//        // Создаем объект Supplies и сохраняем его
//        Supplies sup = new Supplies();
//        sup.setHeader(header);
//        List<Badge> inputBadges = new ArrayList<>();
//        sup.setBadges(inputBadges);
//        inputService.saveInputSupplies(sup);
//
//        // Создаем два объекта Badge и добавляем их в список
//
//        Badge badge1 = new Badge();
//        badge1.setSup(sup);
//        inputService.saveBadge(badge1); // Сначала сохраняем Badge
//        inputBadges.add(badge1);
//
//        Badge badge2 = new Badge();
//        badge2.setSup(sup);
//        inputService.saveBadge(badge2); // Сначала сохраняем Badge
//        inputBadges.add(badge2);
//
//
//
//        // Добавляем объекты в модель
//        model.addAttribute("supplies", sup);
//        model.addAttribute("headerId", headerId);
//        System.out.println(sup);
//
//        return "inputSupplies"; // Название представления
//    }

//

    @GetMapping("/input/supplies")
    public String showInputSupplies(@RequestParam("headerId") Long headerId, Model model) {
        InputHeader header = inputService.getInputHeaderById(headerId);
        if (header == null) {
            return "errorPageHeader";
        }

        Supplies sup = new Supplies();
        sup.setHeader(header);
        inputService.saveInputSupplies(sup);

        List<Badge> inputBadges = new ArrayList<>();
        Badge badge1 = new Badge();
        badge1.setSup(sup);
        inputService.saveBadge(badge1);
        inputBadges.add(badge1);

        Badge badge2 = new Badge();
        badge2.setSup(sup);
        inputService.saveBadge(badge2);
        inputBadges.add(badge2);

        sup.setBadges(inputBadges);

        model.addAttribute("supplies", sup);
        model.addAttribute("headerId", headerId);
        System.out.println(sup);

        return "inputSupplies";
    }


    @PostMapping("/input/saveSupplies")
    public String saveInputSupplies(@Valid @ModelAttribute Supplies sup,
                                    @RequestParam("headerId") Long headerId,
                                    BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            System.out.println("Binding result has errors:");
            bindingResult.getAllErrors().forEach(error -> System.out.println(error));
            return "errorBodyPage"; // Название представления с сообщением об ошибке
        }

        // Получаем InputHeader по headerId
        InputHeader header = inputService.getInputHeaderById(headerId);
        if (header == null) {
            return "errorPageHeader"; // Название представления с сообщением об ошибке
        }

        // Создаем новый список бейджей на основе данных из формы
        List<Badge> badgesToSave = new ArrayList<>();
        Badge templateBadge = sup.getBadges().get(0); // Используем первый бейдж как шаблон
        for (int i = 0; i < header.getWorkDays(); i++) {
            Badge badge = new Badge();
            badge.setSize(templateBadge.getSize());
            badge.setBadgeMaterial(templateBadge.getBadgeMaterial());
            badge.setChroma(templateBadge.getChroma());
            badge.setDensity(templateBadge.getDensity());
            badge.setLamination(templateBadge.getLamination());
            badge.setLaminationKind(templateBadge.getLaminationKind());
            badge.setSup(sup); // Устанавливаем связь с Supplies
            inputService.saveBadge(badge);
            badgesToSave.add(badge);
        }

        // Устанавливаем header и badges для Supplie
        sup.setBadges(badgesToSave);

        return "redirect:/staff?headerId=" + headerId; // Перенаправление после успешного сохранения
    }
}
