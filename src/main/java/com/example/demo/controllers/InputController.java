package com.example.demo.controllers;

import com.example.demo.entities.*;
import com.example.demo.entities.forInputTemplate.*;
import com.example.demo.entities.forSupplies.*;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.services.ExcelServiceImpl;
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
    private final ExcelServiceImpl excelService;

    @Autowired
    public InputController(InputServiceImpl inputService,DeviceRepository deviceRepository,
                           ExcelServiceImpl excelService) {
        this.inputService = inputService;
        this.deviceRepository = deviceRepository;
        this.excelService = excelService;
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
        excelService.createNewFile(header);
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
                body.setNetworkCount(templateBody.getNetworkCount());
                body.setNetworkPrice(templateBody.getNetworkPrice());
                body.setSwitchingCount(templateBody.getSwitchingCount());
                body.setSwitchingPrice(templateBody.getSwitchingPrice());
                body.setCameraDeviceCount(templateBody.getCameraDeviceCount());
                body.setCameraDevicePrice(templateBody.getCameraDevicePrice());
                body.setBarcodeDeviceCount(templateBody.getBarcodeDeviceCount());
                body.setBarcodeDevicePrice(templateBody.getBarcodeDevicePrice());
                body.setRfidReaderDeviceCount(templateBody.getRfidReaderDeviceCount());
                body.setRfidReaderDevicePrice(templateBody.getRfidReaderDevicePrice());
                body.setTsdCount(templateBody.getTsdCount());
                body.setTsdPrice(templateBody.getTsdPrice());
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
        excelService.updateFileWithBodyData(header, bodiesToSave);
        return "redirect:/input/supplies?headerId=" + headerId;
    }


    @GetMapping("/input/supplies")
    public String showInputSupplies(@RequestParam("headerId") Long headerId, Model model) {
        Supplies supplies = new Supplies();
        supplies.setHeader(inputService.getInputHeaderById(headerId));
        inputService.saveInputSupplies(supplies); // Здесь Supplies готов, сохранен и связан с InputHeader
        Long suppliesId = supplies.getId();

        Badge badge1 = new Badge(); //
        Badge badge2 = new Badge(); //Здесь инициализированы два Badge, у них пока везде null
        badge1.setSupplies(inputService.getSuppliesById(suppliesId));
        badge2.setSupplies(inputService.getSuppliesById(suppliesId));// привязываем Supplies к Badge'ам
        supplies.getBadges().add(badge1);
        supplies.getBadges().add(badge2);//привязываем Badge'ы к Supplies

        Lanyard lanyard1 = new Lanyard();
        Lanyard lanyard2 = new Lanyard();
        lanyard1.setSupplies(inputService.getSuppliesById(suppliesId));
        lanyard2.setSupplies(inputService.getSuppliesById(suppliesId));
        supplies.getLanyards().add(lanyard1);
        supplies.getLanyards().add(lanyard2);

        Bracer bracer1 = new Bracer();
        Bracer bracer2 = new Bracer();
        bracer1.setSupplies(inputService.getSuppliesById(suppliesId));
        bracer2.setSupplies(inputService.getSuppliesById(suppliesId));
        supplies.getBracers().add(bracer1);
        supplies.getBracers().add(bracer2);

        Insert insert1 = new Insert();
        Insert insert2 = new Insert();
        insert1.setSupplies(inputService.getSuppliesById(suppliesId));
        insert2.setSupplies(inputService.getSuppliesById(suppliesId));
        supplies.getInserts().add(insert1);
        supplies.getInserts().add(insert2);

        Pocket pocket1 = new Pocket();
        Pocket pocket2 = new Pocket();
        pocket1.setSupplies(inputService.getSuppliesById(suppliesId));
        pocket2.setSupplies(inputService.getSuppliesById(suppliesId));
        supplies.getPockets().add(pocket1);
        supplies.getPockets().add(pocket2);

        Ribbon ribbon1 = new Ribbon();
        Ribbon ribbon2 = new Ribbon();
        ribbon2.setSupplies(inputService.getSuppliesById(suppliesId));
        ribbon1.setSupplies(inputService.getSuppliesById(suppliesId));
        supplies.getRibbons().add(ribbon1);
        supplies.getRibbons().add(ribbon2);

        Sticker sticker1 = new Sticker();
        Sticker sticker2 = new Sticker();
        sticker1.setSupplies(inputService.getSuppliesById(suppliesId));
        sticker2.setSupplies(inputService.getSuppliesById(suppliesId));
        supplies.getStickers().add(sticker1);
        supplies.getStickers().add(sticker2);

        BadgeList badgeList = new BadgeList(supplies.getBadges());
        LanyardList lanyardList = new LanyardList(supplies.getLanyards());
        BracerList bracerList = new BracerList(supplies.getBracers());
        InsertList insertList = new InsertList(supplies.getInserts());
        PocketList pocketList = new PocketList(supplies.getPockets());
        RibbonList ribbonList = new RibbonList(supplies.getRibbons());
        StickerList stickerList = new StickerList(supplies.getStickers());
        model.addAttribute("headerId", headerId);
        model.addAttribute("suppliesId", suppliesId);
        model.addAttribute("badgeList", badgeList);
        model.addAttribute("lanyardList", lanyardList);
        model.addAttribute("bracerList", bracerList);
        model.addAttribute("insertList", insertList);
        model.addAttribute("pocketList", pocketList);
        model.addAttribute("ribbonList", ribbonList);
        model.addAttribute("stickerList", stickerList);
        return "inputSupplies";
    }

    @PostMapping("/input/saveSupplies")
    public String saveSupplies(@RequestParam("headerId") Long headerId,
                               @ModelAttribute("badgeList") BadgeList badgeList,
                               @ModelAttribute("lanyardList") LanyardList lanyardList,
                               @ModelAttribute("bracerList") BracerList bracerList,
                               @ModelAttribute("insertList") InsertList insertList,
                               @ModelAttribute("pocketList") PocketList pocketList,
                               @ModelAttribute("ribbonList") RibbonList ribbonList,
                               @ModelAttribute("stickerList") StickerList stickerList,
                               @RequestParam("suppliesId") Long suppliesId,
                               Model model) {
        badgeList.getBadges().get(0).setSupplies(inputService.getSuppliesById(suppliesId));
        badgeList.getBadges().get(1).setSupplies(inputService.getSuppliesById(suppliesId));
        inputService.saveBadge(badgeList.getBadges().get(0));
        inputService.saveBadge(badgeList.getBadges().get(1));

        lanyardList.getLanyards().get(0).setSupplies(inputService.getSuppliesById(suppliesId));
        lanyardList.getLanyards().get(1).setSupplies(inputService.getSuppliesById(suppliesId));
        inputService.saveLanyard(lanyardList.getLanyards().get(0));
        inputService.saveLanyard(lanyardList.getLanyards().get(1));

        bracerList.getBracers().get(0).setSupplies(inputService.getSuppliesById(suppliesId));
        bracerList.getBracers().get(1).setSupplies(inputService.getSuppliesById(suppliesId));
        inputService.saveBracer(bracerList.getBracers().get(0));
        inputService.saveBracer(bracerList.getBracers().get(1));

        insertList.getInserts().get(0).setSupplies(inputService.getSuppliesById(suppliesId));
        insertList.getInserts().get(1).setSupplies(inputService.getSuppliesById(suppliesId));
        inputService.saveInsert(insertList.getInserts().get(0));
        inputService.saveInsert(insertList.getInserts().get(1));

        pocketList.getPockets().get(0).setSupplies(inputService.getSuppliesById(suppliesId));
        pocketList.getPockets().get(1).setSupplies(inputService.getSuppliesById(suppliesId));
        inputService.savePocket(pocketList.getPockets().get(0));
        inputService.savePocket(pocketList.getPockets().get(1));

        ribbonList.getRibbons().get(0).setSupplies(inputService.getSuppliesById(suppliesId));
        ribbonList.getRibbons().get(1).setSupplies(inputService.getSuppliesById(suppliesId));
        inputService.saveRibbon(ribbonList.getRibbons().get(0));
        inputService.saveRibbon(ribbonList.getRibbons().get(1));

        stickerList.getStickers().get(0).setSupplies(inputService.getSuppliesById(suppliesId));
        stickerList.getStickers().get(1).setSupplies(inputService.getSuppliesById(suppliesId));
        inputService.saveSticker(stickerList.getStickers().get(0));
        inputService.saveSticker(stickerList.getStickers().get(1));

        //model.addAttribute("message", "Supplies and Badges saved successfully");
        return "redirect:/input/staff?headerId=" + headerId;
    }

    @GetMapping("/input/staff")
    public String showInputStaff(@RequestParam("headerId") Long headerId, Model model) {
        Staff staff = new Staff();
        System.out.println(staff);
        model.addAttribute("headerId", headerId);
        model.addAttribute("staff", staff);
        return "inputStaff";
    }

    @PostMapping("/input/saveStaffInput")
    public String saveStaffInput(@ModelAttribute Staff staff, @RequestParam("headerId") Long headerId) {

        staff.setHeader(inputService.getInputHeaderById(headerId));
        System.out.println(staff);
        inputService.saveStaff(staff);
        System.out.println(staff);
        return "redirect:/input/logistic?headerId=" + headerId; // Перенаправление на страницу успешного сохранения
    }

    @GetMapping("/input/logistic")
    public String showInputLogistic(@RequestParam("headerId") Long headerId, Model model) {
        Logistic logistic = new Logistic();
        model.addAttribute("headerId", headerId);
        model.addAttribute("logistic", logistic);
        return "inputLogistic";

    }

    @PostMapping("/input/saveLogisticInput")
    public String saveLogisticInput(@ModelAttribute Logistic logistic, @RequestParam("headerId") Long headerId) {

        logistic.setHeader(inputService.getInputHeaderById(headerId));

        inputService.saveLogistic(logistic);

        return "redirect:/"; // Перенаправление на страницу успешного сохранения
    }
}
