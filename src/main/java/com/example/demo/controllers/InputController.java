package com.example.demo.controllers;

import com.example.demo.entities.*;
import com.example.demo.entities.forInputTemplate.*;
import com.example.demo.entities.forSupplies.*;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.InputHeaderRepository;
import com.example.demo.services.ExcelServiceImpl;
import com.example.demo.services.InputServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class InputController {

    private final InputServiceImpl inputService;
    private final DeviceRepository deviceRepository;
    private final ExcelServiceImpl excelService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(new Hibernate5Module());

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
        LocalDate lastPeriodEnd = header.getEventStartDate().minusDays(1);

        if (header.getBoolStart1() != null && header.getBoolEnd1() != null) {
            lastPeriodEnd = header.getBoolEnd1();
        }

        if (header.getBoolStart2() != null && header.getBoolEnd2() != null) {
            lastPeriodEnd = header.getBoolEnd2();
        }

        if (header.getBoolStart3() != null && header.getBoolEnd3() != null) {
            lastPeriodEnd = header.getBoolEnd3();
        }

        if (lastPeriodEnd.isBefore(header.getEventEndDate())) {
            header.setBoolStart4(lastPeriodEnd.plusDays(1));
            header.setBoolEnd4(header.getEventEndDate());
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
//        List<Device> softDevices = deviceRepository.findByIdIn(List.of(1L, 2L, 3L));
//        List<Device> printerDevices = deviceRepository.findByIdIn(List.of(4L, 5L));

        List<InputBody> inputBodies = new ArrayList<>();
        for (int i = 0; i < header.getWorkDays(); i++) {
            InputBody body = new InputBody();
            body.setHeader(header);
            inputBodies.add(body);
        }

        int periods = 0;
        LocalDate lastPeriodEnd = header.getEventStartDate().minusDays(1);
        if (header.getBoolStart1() != null && header.getBoolEnd1() != null) {
            periods++;
            lastPeriodEnd = header.getBoolEnd1();
        }
        if (header.getBoolStart2() != null && header.getBoolEnd2() != null) {
            periods++;
            lastPeriodEnd = header.getBoolEnd2();
        } else if (header.getBoolStart1() != null && header.getBoolEnd1() != null &&
                header.getBoolEnd1().isBefore(header.getEventEndDate()) &&
                header.getBoolEnd1().isAfter(lastPeriodEnd)) {
            periods++;
            lastPeriodEnd = header.getBoolEnd1();
        }
        if (header.getBoolStart3() != null && header.getBoolEnd3() != null) {
            periods++;
            lastPeriodEnd = header.getBoolEnd3();
        } else if (header.getBoolStart2() != null && header.getBoolEnd2() != null &&
                header.getBoolEnd2().isBefore(header.getEventEndDate()) &&
                header.getBoolEnd2().isAfter(lastPeriodEnd)) {
            periods++;
            lastPeriodEnd = header.getBoolEnd2();
        }
        if (lastPeriodEnd.isBefore(header.getEventEndDate())) {
            periods++;
        }

        System.out.println("Количество валидных периодов: " + periods);

        List<String> periodDates = new ArrayList<>();
        if (header.getBoolStart1() != null && header.getBoolEnd1() != null) {
            periodDates.add(header.getBoolStart1().toString());
            periodDates.add(header.getBoolEnd1().toString());
        }
        if (header.getBoolStart2() != null && header.getBoolEnd2() != null) {
            periodDates.add(header.getBoolStart2().toString());
            periodDates.add(header.getBoolEnd2().toString());
        }
        if (header.getBoolStart3() != null && header.getBoolEnd3() != null) {
            periodDates.add(header.getBoolStart3().toString());
            periodDates.add(header.getBoolEnd3().toString());
        }
        if (header.getBoolStart4() != null && header.getBoolEnd4() != null) {
            periodDates.add(header.getBoolStart4().toString());
            periodDates.add(header.getBoolEnd4().toString());
        }

        List<String> daysWithoutPeriod = new ArrayList<>();

        LocalDate startDate = header.getEventStartDate();
        LocalDate endDate = header.getEventEndDate();

        while (!startDate.isAfter(endDate)) {
            daysWithoutPeriod.add(startDate.toString());
            startDate = startDate.plusDays(1);
        }



        HeaderBodies headerBodies = new HeaderBodies(inputBodies);
//        model.addAttribute("softDevices", softDevices);
//        model.addAttribute("printerDevices", printerDevices);
        model.addAttribute("headerId", headerId);
        model.addAttribute("headerBodies", headerBodies);
        model.addAttribute("sameEquipmentForAllDays", header.isSameEquipmentForAllDays());
        model.addAttribute("periods", periods);
        model.addAttribute("periodDates", periodDates);
        model.addAttribute("daysWithoutPeriod", daysWithoutPeriod);
        return "inputBody";
    }

    @PostMapping("/input/saveBodyInput")
    public String saveInputBody(@Valid @ModelAttribute HeaderBodies bodies,
                                @RequestParam("headerId") Long headerId,
                                @RequestParam("sameEquipmentForAllDays") boolean sameEquipmentForAllDays,
                                @RequestParam("periods") int periods,
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

        if (sameEquipmentForAllDays && periods > 0) {
            for (int i = 0; i < periods; i++) {
                InputBody templateBody = bodies.getBodies().get(i);
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
        } else if (sameEquipmentForAllDays) {
            InputBody templateBody = bodies.getBodies().get(0);
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
        } else {
            for (InputBody body : bodies.getBodies()) {
                body.setHeader(header);
                bodiesToSave.add(body);
            }
        }

        model.addAttribute("bodies", bodiesToSave);
        inputService.saveInputBody(bodiesToSave);
        excelService.updateFileWithBodyData(header, bodiesToSave, periods);
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

        Asup asup1 = new Asup();
        Asup asup2 = new Asup();
        asup1.setSupplies(inputService.getSuppliesById(suppliesId));
        asup2.setSupplies(inputService.getSuppliesById(suppliesId));
        supplies.getAsups().add(asup1);
        supplies.getAsups().add(asup2);

        BadgeList badgeList = new BadgeList(supplies.getBadges());
        LanyardList lanyardList = new LanyardList(supplies.getLanyards());
        BracerList bracerList = new BracerList(supplies.getBracers());
        InsertList insertList = new InsertList(supplies.getInserts());
        PocketList pocketList = new PocketList(supplies.getPockets());
        RibbonList ribbonList = new RibbonList(supplies.getRibbons());
        StickerList stickerList = new StickerList(supplies.getStickers());
        AsupList asupList = new AsupList(supplies.getAsups());

        model.addAttribute("headerId", headerId);
        model.addAttribute("suppliesId", suppliesId);
        model.addAttribute("badgeList", badgeList);
        model.addAttribute("lanyardList", lanyardList);
        model.addAttribute("bracerList", bracerList);
        model.addAttribute("insertList", insertList);
        model.addAttribute("pocketList", pocketList);
        model.addAttribute("ribbonList", ribbonList);
        model.addAttribute("stickerList", stickerList);
        model.addAttribute("asupList", asupList);
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
                               @ModelAttribute("asupList") AsupList asupList,
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

        asupList.getAsups().get(0).setSupplies(inputService.getSuppliesById(suppliesId));
        asupList.getAsups().get(1).setSupplies(inputService.getSuppliesById(suppliesId));
        inputService.saveAsup(asupList.getAsups().get(0));
        inputService.saveAsup(asupList.getAsups().get(1));

        InputHeader header = inputService.getInputHeaderById(headerId);
        Supplies supplies = inputService.getSuppliesById(suppliesId);
        excelService.updateFileWithSuppliesData(header, supplies);
        return "redirect:/input/staff?headerId=" + headerId;
    }

    @GetMapping("/input/staff")
    public String showInputStaff(@RequestParam("headerId") Long headerId, Model model) {
        InputHeader header = inputService.getInputHeaderById(headerId);
        Integer daysWithoutPeriod = header.getWorkDays();
        Mounting m = new Mounting();
        m.setHeader(inputService.getInputHeaderById(headerId));

        List<Staff> staffs = new ArrayList<>();
        for (int i = 1; i < 37; i++) {//было i < 36
            Staff staff = new Staff();
            staff.setHeader(header);
            staffs.add(staff);
        }

        int periods = 0;
        LocalDate lastPeriodEnd = header.getEventStartDate().minusDays(1);
        if (header.getBoolStart1() != null && header.getBoolEnd1() != null) {
            periods++;
            lastPeriodEnd = header.getBoolEnd1();
        }
        if (header.getBoolStart2() != null && header.getBoolEnd2() != null) {
            periods++;
            lastPeriodEnd = header.getBoolEnd2();
        } else if (header.getBoolStart1() != null && header.getBoolEnd1() != null &&
                header.getBoolEnd1().isBefore(header.getEventEndDate()) &&
                header.getBoolEnd1().isAfter(lastPeriodEnd)) {
            periods++;
            lastPeriodEnd = header.getBoolEnd1();
        }
        if (header.getBoolStart3() != null && header.getBoolEnd3() != null) {
            periods++;
            lastPeriodEnd = header.getBoolEnd3();
        } else if (header.getBoolStart2() != null && header.getBoolEnd2() != null &&
                header.getBoolEnd2().isBefore(header.getEventEndDate()) &&
                header.getBoolEnd2().isAfter(lastPeriodEnd)) {
            periods++;
            lastPeriodEnd = header.getBoolEnd2();
        }
        if (lastPeriodEnd.isBefore(header.getEventEndDate())) {
            periods++;
        }

        List<String> periodDates = new ArrayList<>();
        if (header.getBoolStart1() != null && header.getBoolEnd1() != null) {
            periodDates.add(header.getBoolStart1().toString());
            periodDates.add(header.getBoolEnd1().toString());
        }
        if (header.getBoolStart2() != null && header.getBoolEnd2() != null) {
            periodDates.add(header.getBoolStart2().toString());
            periodDates.add(header.getBoolEnd2().toString());
        }
        if (header.getBoolStart3() != null && header.getBoolEnd3() != null) {
            periodDates.add(header.getBoolStart3().toString());
            periodDates.add(header.getBoolEnd3().toString());
        }
        if (header.getBoolStart4() != null && header.getBoolEnd4() != null) {
            periodDates.add(header.getBoolStart4().toString());
            periodDates.add(header.getBoolEnd4().toString());
        }

        List<String> datesWithoutPeriod = new ArrayList<>();

        LocalDate startDate = header.getEventStartDate();
        LocalDate endDate = header.getEventEndDate();

        while (!startDate.isAfter(endDate)) {
            datesWithoutPeriod.add(startDate.toString());
            startDate = startDate.plusDays(1);
        }



        String staffsJson = "";
        try {
            staffsJson = objectMapper.writeValueAsString(staffs);
        } catch (Exception e) {
            e.printStackTrace();
            // Логирование ошибки или обработка
        }

        System.out.println("Количество валидных периодов: " + periods);
        System.out.println("Periods: " + periods);
        System.out.println("Period Dates: " + periodDates);


        HeaderStaffs headerStaffs = new HeaderStaffs(staffs);
        model.addAttribute("m", m);
        model.addAttribute("headerId", headerId);
        model.addAttribute("headerStaffs", headerStaffs);
        model.addAttribute("periods", periods);
        model.addAttribute("staffsJson", staffsJson);
        model.addAttribute("sameEquipmentForAllDays", header.isSameEquipmentForAllDays());
        model.addAttribute("periodDates", periodDates);
        model.addAttribute("daysWithoutPeriod", daysWithoutPeriod);

        return "inputStaff";
    }

    @PostMapping("/input/saveStaffInput")
    public String saveStaffInput(@ModelAttribute Mounting m,
                                 @ModelAttribute HeaderStaffs headerStaffs,
                                 @RequestParam("headerId") Long headerId,
                                 @RequestParam("periods") int periods) {
        InputHeader header = inputService.getInputHeaderById(headerId);
        m.setHeader(header);
        inputService.saveMounting(m);
        // Получаем список сотрудников из объекта HeaderStaffs
        List<Staff> staffs = headerStaffs.getStaffs();

        // Обработка и сохранение каждого сотрудника
        for (Staff staff : staffs) {
            // Привязываем header к каждому сотруднику
            staff.setHeader(header);
            if (staff.getBetPerHour() == null) {
                staff.setBetPerHour(0);
            }
            if (staff.getStaffQuantity() == null) {
                staff.setStaffQuantity(0);
            }
            inputService.saveStaff(staff);
        }
        excelService.updateFileWithStaff(header, staffs, m, periods);
        return "redirect:/input/logistic?headerId=" + headerId;
    }

    @GetMapping("/input/logistic")
    public String showInputLogistic(@RequestParam("headerId") Long headerId,
                                    Model model) {
        InputHeader header = inputService.getInputHeaderById(headerId);

        List<Logistic> logistics = new ArrayList<>();
        for (int i = 0; i < 10; i++) {//10
            Logistic logistic = new Logistic();
            logistic.setHeader(header);
            logistics.add(logistic);
            System.out.println(logistic);
        }

        HeaderLogistics headerLogistics = new HeaderLogistics(logistics);
        model.addAttribute("headerId", headerId);
        model.addAttribute("headerLogistics", headerLogistics);
        return "inputLogistic";

    }

    @PostMapping("/input/saveLogisticInput")
    public String saveLogisticInput(@ModelAttribute HeaderLogistics headerLogistics,
                                    @RequestParam("headerId") Long headerId) {
        InputHeader header = inputService.getInputHeaderById(headerId);
        List<Logistic> logistics = headerLogistics.getLogistics();

        // Обработка и сохранение каждого сотрудника
        for (Logistic logistic : logistics) {
            System.out.println(logistic);
            // Привязываем header к каждому сотруднику
            logistic.setHeader(header);
            System.out.println(logistic);
            if (logistic.getPrice() == null) {
                logistic.setPrice(0);
            }
            if (logistic.getCount() == null) {
                logistic.setCount(0);
            }
            if (logistic.getHeader() == null) {
                throw new IllegalStateException("Header не должен быть null");
            }

            inputService.saveLogistic(logistic);
        }
        excelService.updateFileWithLogistics(header, logistics);

        return "redirect:/"; // Перенаправление на страницу успешного сохранения
    }
}
