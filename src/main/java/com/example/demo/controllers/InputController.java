package com.example.demo.controllers;

import com.example.demo.entities.*;
import com.example.demo.entities.forInputTemplate.*;
import com.example.demo.entities.forSupplies.*;
import com.example.demo.services.CustomerServiceImpl;
import com.example.demo.services.ExcelServiceImpl;
import com.example.demo.services.InputServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class InputController {

    private final InputServiceImpl inputService;
    private final CustomerServiceImpl customerService;
    private final ExcelServiceImpl excelService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(new Hibernate5Module());

    @Autowired
    public InputController(InputServiceImpl inputService, CustomerServiceImpl customerService,
                           ExcelServiceImpl excelService) {
        this.inputService = inputService;
        this.customerService = customerService;
        this.excelService = excelService;

    }

    @GetMapping("/input/header")
    public String showInputHeader(Model model) {
        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("inputHeader", new InputHeader());
        model.addAttribute("customers", customers);
        return "inputHeader";
    }

    @PostMapping("/input/saveHeaderInput")
    public String saveInput(@Valid @ModelAttribute("inputHeader") InputHeader header,
                            Authentication authentication) {
        String currentUsername = authentication.getName();
        Long headerId = inputService.saveInputHeader(header, currentUsername);
        excelService.createNewFile(header);
        if(header.isWithManyRegPoints()){
            return "redirect:/input/regPoints?headerId=" + headerId;
        } else {
            return "redirect:/input/body?headerId=" + headerId;
        }
    }

    @GetMapping("/input/regPoints")
    public String showInputRegPoints(@RequestParam("headerId") Long headerId, Model model) {
        InputHeader header = inputService.getInputHeaderById(headerId);
        LocalDate eventOver = header.getEventEndDate();
        List<RegPoint> regPoints = new ArrayList<>();
        for(int i = 0; i < 16; i++){
            RegPoint regPoint = new RegPoint();
            regPoint.setHeader(header);
            regPoints.add(regPoint);
        }
        HeaderRegPoints headerRegPoints = new HeaderRegPoints(regPoints);
        model.addAttribute("headerRegPoints",headerRegPoints);
        model.addAttribute("eventOver", eventOver);
        model.addAttribute("headerId", headerId);
        return "InputRegPoints";
    }

    @PostMapping("/input/saveInputRegPoints")
    public String saveInputRegPoints(@Valid @ModelAttribute HeaderRegPoints headerRegPoints,
                                     @RequestParam("headerId") Long headerId) {
        InputHeader header = inputService.getInputHeaderById(headerId);

        List<RegPoint> regPoints = headerRegPoints.getRegPoints();
        for(RegPoint regPoint : regPoints){
            regPoint.setHeader(header);
            if(isValidRegPoint(regPoint)){
                inputService.saveRegPoint(regPoint);
            }
        }
        return "redirect:/input/body?headerId=" + headerId;
    }

    private boolean isValidRegPoint(RegPoint regPoint) {
        return regPoint.getName() != null && !regPoint.getName().isBlank() &&
                regPoint.getDescription() != null && !regPoint.getDescription().isBlank() &&
                regPoint.getStartRPDate() != null &&
                regPoint.getEndRPDate() != null;
    }

    @GetMapping("/input/body")
    public String showInputBody(@RequestParam("headerId") Long headerId, Model model) {
        InputHeader header = inputService.getInputHeaderById(headerId);
        List<InputBody> inputBodies = new ArrayList<>();

        if (header.isWithManyRegPoints()) {
            for (int i = 0; i < header.getRegPoints().size(); i++) {
                InputBody body = new InputBody();
                body.setHeader(header);

                List<Device> devicesForInput = new ArrayList<>();
                for (int k = 0; k < 20; k++) {
                    Device device = new Device();
                    device.setBody(body);
                    devicesForInput.add(device);
                }
                body.setDevices(devicesForInput);
                inputBodies.add(body);
            }
        } else if (header.isSameEquipmentForAllDays()) {
            for (int i = 0; i < header.getPeriods(); i++) {
                InputBody body = new InputBody();
                body.setHeader(header);

                List<Device> devicesForInput = new ArrayList<>();
                for (int k = 0; k < 20; k++) {
                    Device device = new Device();
                    device.setBody(body);
                    devicesForInput.add(device);
                }
                body.setDevices(devicesForInput);
                inputBodies.add(body);
            }
        } else {
            for (int i = 0; i < header.getWorkDays(); i++) {
                InputBody body = new InputBody();
                body.setHeader(header);

                List<Device> devicesForInput = new ArrayList<>();
                for (int k = 0; k < 20; k++) {
                    Device device = new Device();
                    device.setBody(body);
                    devicesForInput.add(device);
                }
                body.setDevices(devicesForInput);
                inputBodies.add(body);
            }
        }

        HeaderBodies headerBodies = new HeaderBodies(inputBodies);

        model.addAttribute("workDates", inputService.getDatesOfWorkDays(header));
        model.addAttribute("regPointsStrings", inputService.getRegPointsStrings(header));
        model.addAttribute("stringPeriods", inputService.getPeriodsStrings(header));

        model.addAttribute("headerId", headerId);
        model.addAttribute("headerBodies", headerBodies);
        model.addAttribute("sameEquipmentForAllDays", header.isSameEquipmentForAllDays());
        model.addAttribute("withManyRegPoints", header.isWithManyRegPoints());
        model.addAttribute("periods", header.getPeriods());
        model.addAttribute("workDays", header.getWorkDays());
        model.addAttribute("regPoints", header.getRegPoints().size());
        return "inputBody";
    }

    @PostMapping("/input/saveBodyInput")
    public String saveBodyInput(@ModelAttribute HeaderBodies headerBodies,
                                @RequestParam("headerId") Long headerId) {
        InputHeader header = inputService.getInputHeaderById(headerId);

        List<InputBody> bodies = headerBodies.getBodies();

        for (InputBody body : bodies) {
            body.setHeader(header);

            List<Device> validDevices = body.getDevices().stream()
                    .filter(this::isValidDevice)
                    .peek(device -> device.setBody(body))
                    .collect(Collectors.toList());

            body.setDevices(validDevices);
            header.getInputBodies().add(body);
            inputService.saveInputBody(body);

            for (Device device : validDevices) {
                inputService.saveDevice(device);
            }

        }
        excelService.updateFileWithBodyData(header);
        return "redirect:/input/supplies?headerId=" + headerId;
    }

    private boolean isValidDevice(Device device) {
        return device.getName() != null && !device.getName().isBlank() &&
                device.getPriceFor1q() != null && device.getPriceFor1q() > 0 &&
                device.getCount() != null && device.getCount() > 0;
    }

    @GetMapping("/input/supplies")
    public String showInputSupplies(@RequestParam("headerId") Long headerId, Model model) {

        Supplies supplies = new Supplies();
        supplies.setHeader(inputService.getInputHeaderById(headerId));
        inputService.saveInputSupplies(supplies);
        Long suppliesId = supplies.getId();

        BadgeList badgeList = new BadgeList(supplies.getBadges());
        LanyardList lanyardList = new LanyardList(supplies.getLanyards());
        BracerList bracerList = new BracerList(supplies.getBracers());
        InsertList insertList = new InsertList(supplies.getInserts());
        PocketList pocketList = new PocketList(supplies.getPockets());
        RibbonList ribbonList = new RibbonList(supplies.getRibbons());
        StickerList stickerList = new StickerList(supplies.getStickers());
        AsupList asupList = new AsupList(supplies.getAsups());

        for (int i = 0; i < 2; i++) {
            Badge badge = new Badge();
            badge.setSupplies(inputService.getSuppliesById(suppliesId));
            badgeList.getBadges().add(badge);
        }

        for (int i = 0; i < 2; i++) {
            Lanyard lanyard = new Lanyard();
            lanyard.setSupplies(inputService.getSuppliesById(suppliesId));
            lanyardList.getLanyards().add(lanyard);
        }

        for (int i = 0; i < 2; i++) {
            Bracer bracer = new Bracer();
            bracer.setSupplies(inputService.getSuppliesById(suppliesId));
            bracerList.getBracers().add(bracer);
        }

        for (int i = 0; i < 2; i++) {
            Insert insert = new Insert();
            insert.setSupplies(inputService.getSuppliesById(suppliesId));
            insertList.getInserts().add(insert);
        }

        for (int i = 0; i < 2; i++) {
            Pocket pocket = new Pocket();
            pocket.setSupplies(inputService.getSuppliesById(suppliesId));
            pocketList.getPockets().add(pocket);
        }

        for (int i = 0; i < 2; i++) {
            Ribbon ribbon = new Ribbon();
            ribbon.setSupplies(inputService.getSuppliesById(suppliesId));
            ribbonList.getRibbons().add(ribbon);
        }

        for (int i = 0; i < 2; i++) {
            Sticker sticker = new Sticker();
            sticker.setSupplies(inputService.getSuppliesById(suppliesId));
            stickerList.getStickers().add(sticker);
        }

        for (int i = 0; i < 2; i++) {
            Asup asup = new Asup();
            asup.setSupplies(inputService.getSuppliesById(suppliesId));
            asupList.getAsups().add(asup);
        }

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

        List<Badge> validBadges = badgeList.getBadges().stream()
                .filter(badge -> badge.getPrice() != null && badge.getPrice() > 0 &&
                        badge.getCount() != null && badge.getCount() > 0)
                .peek(badge -> badge.setSupplies(inputService.getSuppliesById(suppliesId)))
                .collect(Collectors.toList());

        for (Badge badge : validBadges) {
            inputService.saveBadge(badge);
        }

        List<Lanyard> validLanyards = lanyardList.getLanyards().stream()
                .filter(lanyard -> lanyard.getPrice() != null && lanyard.getPrice() > 0 &&
                        lanyard.getCount() != null && lanyard.getCount() > 0)
                .peek(lanyard -> lanyard.setSupplies(inputService.getSuppliesById(suppliesId)))
                .collect(Collectors.toList());

        for (Lanyard lanyard : validLanyards) {
            inputService.saveLanyard(lanyard);
        }

        List<Bracer> validBracers = bracerList.getBracers().stream()
                .filter(bracer -> bracer.getPrice() != null && bracer.getPrice() > 0 &&
                        bracer.getCount() != null && bracer.getCount() > 0)
                .peek(bracer -> bracer.setSupplies(inputService.getSuppliesById(suppliesId)))
                .collect(Collectors.toList());

        for (Bracer bracer : validBracers) {
            inputService.saveBracer(bracer);
        }

        List<Insert> validInserts = insertList.getInserts().stream()
                .filter(insert -> insert.getPrice() != null && insert.getPrice() > 0 &&
                        insert.getCount() != null && insert.getCount() > 0)
                .peek(insert -> insert.setSupplies(inputService.getSuppliesById(suppliesId)))
                .collect(Collectors.toList());

        for (Insert insert : validInserts) {
            inputService.saveInsert(insert);
        }

        List<Pocket> validPockets = pocketList.getPockets().stream()
                .filter(pocket -> pocket.getPrice() != null && pocket.getPrice() > 0 &&
                        pocket.getCount() != null && pocket.getCount() > 0)
                .peek(pocket -> pocket.setSupplies(inputService.getSuppliesById(suppliesId)))
                .collect(Collectors.toList());

        for (Pocket pocket : validPockets) {
            inputService.savePocket(pocket);
        }

        List<Ribbon> validRibbons = ribbonList.getRibbons().stream()
                .filter(ribbon -> ribbon.getPrice() != null && ribbon.getPrice() > 0 &&
                        ribbon.getCount() != null && ribbon.getCount() > 0)
                .peek(ribbon -> ribbon.setSupplies(inputService.getSuppliesById(suppliesId)))
                .collect(Collectors.toList());

        for (Ribbon ribbon : validRibbons) {
            inputService.saveRibbon(ribbon);
        }

        List<Sticker> validStickers = stickerList.getStickers().stream()
                .filter(sticker -> sticker.getPrice() != null && sticker.getPrice() > 0 &&
                        sticker.getCount() != null && sticker.getCount() > 0)
                .peek(sticker -> sticker.setSupplies(inputService.getSuppliesById(suppliesId)))
                .collect(Collectors.toList());

        for (Sticker sticker : validStickers) {
            inputService.saveSticker(sticker);
        }

        List<Asup> validAsups = asupList.getAsups().stream()
                .filter(asup -> asup.getPrice() != null && asup.getPrice() > 0 &&
                        asup.getCount() != null && asup.getCount() > 0)
                .peek(asup -> asup.setSupplies(inputService.getSuppliesById(suppliesId)))
                .collect(Collectors.toList());

        for (Asup asup : validAsups) {
            inputService.saveAsup(asup);
        }

        InputHeader header = inputService.getInputHeaderById(headerId);
        Supplies supplies = inputService.getSuppliesById(suppliesId);
        excelService.updateFileWithSuppliesData(header, supplies);
        return "redirect:/input/staff?headerId=" + headerId;
    }

    @GetMapping("/input/staff")
    public String showInputStaff(@RequestParam("headerId") Long headerId, Model model) {
        InputHeader header = inputService.getInputHeaderById(headerId);
        List<InputStaff> inputStaffs = new ArrayList<>();
        MountingsList mountingsList = new MountingsList(new ArrayList<>());


        for(int i = 0; i < 3; i++){
            Mounting m = new Mounting();
            m.setHeader(header);
            mountingsList.getMountings().add(m);
        }
        System.out.println("mountingsize"+mountingsList.getMountings().size());
        if (header.isWithManyRegPoints()) {
            for (int i = 0; i < header.getRegPoints().size(); i++) {
                InputStaff inputStaff = new InputStaff();
                inputStaff.setHeader(header);

                List<Staff> staffsForInput = new ArrayList<>();
                for (int k = 0; k < 20; k++) {
                    Staff staff = new Staff();
                    staff.setStaff(inputStaff);
                    staffsForInput.add(staff);
                }
                inputStaff.setStaffs(staffsForInput);
                inputStaffs.add(inputStaff);
                //отладка
                System.out.println(inputStaffs.size());
            }
        } else if (header.isSameEquipmentForAllDays()) {
            for (int i = 0; i < header.getPeriods(); i++) {
                InputStaff inputStaff = new InputStaff();
                inputStaff.setHeader(header);

                List<Staff> staffsForInput = new ArrayList<>();
                for (int k = 0; k < 20; k++) {
                    Staff staff = new Staff();
                    staff.setStaff(inputStaff);
                    staffsForInput.add(staff);
                }
                inputStaff.setStaffs(staffsForInput);
                inputStaffs.add(inputStaff);
                //отладка
                System.out.println(inputStaffs.size());
            }
        } else {
            for (int i = 0; i < header.getWorkDays(); i++) {
                InputStaff inputStaff = new InputStaff();
                inputStaff.setHeader(header);

                List<Staff> staffsForInput = new ArrayList<>();
                for (int k = 0; k < 20; k++) {
                    Staff staff = new Staff();
                    staff.setStaff(inputStaff);
                    staffsForInput.add(staff);
                }
                inputStaff.setStaffs(staffsForInput);
                inputStaffs.add(inputStaff);
                //отладка
                System.out.println(inputStaffs.size());
            }
        }


        HeaderStaffs headerStaffs = new HeaderStaffs(inputStaffs);


        model.addAttribute("workDates", inputService.getDatesOfWorkDays(header));
        model.addAttribute("regPointsStrings", inputService.getRegPointsStrings(header));
        model.addAttribute("stringPeriods", inputService.getPeriodsStrings(header));

        model.addAttribute("headerId", headerId);
        model.addAttribute("headerStaffs", headerStaffs);
        model.addAttribute("mountingsList", mountingsList);
        model.addAttribute("sameEquipmentForAllDays", header.isSameEquipmentForAllDays());
        model.addAttribute("withManyRegPoints", header.isWithManyRegPoints());
        model.addAttribute("periods", header.getPeriods());
        model.addAttribute("workDays", header.getWorkDays());
        model.addAttribute("regPoints", header.getRegPoints().size());

        return "inputStaff";
    }

    @PostMapping("/input/saveStaffInput")
    public String saveStaffInput(@ModelAttribute MountingsList mountingsList,
                                 @ModelAttribute HeaderStaffs headerStaffs,
                                 @RequestParam("headerId") Long headerId) {
        InputHeader header = inputService.getInputHeaderById(headerId);

        List<Mounting> mos = mountingsList.getMountings();
        for(Mounting m: mos){
            m.setHeader(header);
            if(isValidMounting(m)){
                inputService.saveMounting(m);
            }
        }

        List<InputStaff> staffs = headerStaffs.getStaffs();
        for (InputStaff inputStaff : staffs) {
            inputStaff.setHeader(header);

            List<Staff> validStaffs = inputStaff.getStaffs().stream()
                    .filter(this::isValidStaff)
                    .peek(staff -> staff.setStaff(inputStaff))
                    .collect(Collectors.toList());

            inputStaff.setStaffs(validStaffs);
            header.getInputStaffs().add(inputStaff);
            inputService.saveInputStaff(inputStaff);

            for(Staff staff: validStaffs){
                inputService.saveStaff(staff);
            }
        }
        excelService.updateFileWithStaff(header);
        return "redirect:/input/logistic?headerId=" + headerId;
    }

    private boolean isValidStaff(Staff staff) {
        return staff.getKindOfStaff() != null && !staff.getKindOfStaff().isBlank() &&  // Проверяем наличие и непустоту вида персонала
                staff.getStartTime() != null &&  // Проверяем наличие времени начала
                staff.getEndTime() != null &&  // Проверяем наличие времени окончания
                staff.getStaffQuantity() != null && staff.getStaffQuantity() > 0 &&  // Проверяем количество персонала (должно быть больше 0)
                staff.getBetPerHour() != null && staff.getBetPerHour() > 0 &&  // Проверяем ставку в час (должно быть больше 0)
                !staff.getEndTime().isBefore(staff.getStartTime());  // Проверяем, что время окончания не раньше времени начала
    }

    private boolean isValidMounting(Mounting mounting) {
        return mounting.getKindOfService() != null && !mounting.getKindOfService().isBlank() &&  // Проверяем наличие и непустоту вида услуги
                mounting.getWorkHours() != null && mounting.getWorkHours() > 0 &&  // Проверяем часы работы (должны быть больше 0)
                mounting.getAgentCount() != null && mounting.getAgentCount() > 0 &&  // Проверяем количество агентов (должно быть больше 0)
                mounting.getPriceForHour() != null && mounting.getPriceForHour() > 0;  // Проверяем ставку в час (должна быть больше 0)
    }

    @GetMapping("/input/logistic")
    public String showInputLogisticAndASales(@RequestParam("headerId") Long headerId, Model model) {
        InputHeader header = inputService.getInputHeaderById(headerId);
        HeaderLogistics headerLogistics = new HeaderLogistics(new ArrayList<>());
        HeaderAsales headerAsales = new HeaderAsales(new ArrayList<>());

        for (int i = 0; i < 10; i++) {
            Logistic logistic = new Logistic();
            logistic.setHeader(header);
            headerLogistics.getLogistics().add(logistic);
        }

        for (int i = 0; i < 10; i++) {
            AdditionalSale sale = new AdditionalSale();
            sale.setHeader(header);
            headerAsales.getASales().add(sale);
        }

        model.addAttribute("headerLogistics", headerLogistics);
        model.addAttribute("headerAsales", headerAsales);
        model.addAttribute("headerId", headerId);
        return "inputLogisticAndASales";
    }

    @PostMapping("/input/saveLogisticAndASalesInput")
    public String saveLogisticAndASalesInput(@ModelAttribute HeaderLogistics headerLogistics,
                                             @ModelAttribute HeaderAsales headerAsales,
                                             @RequestParam("headerId") Long headerId) {
        InputHeader header = inputService.getInputHeaderById(headerId);

        List<Logistic> validLogistics = headerLogistics.getLogistics().stream()
                .filter(this::isValidLogistic)
                .peek(logistic -> logistic.setHeader(header))
                .collect(Collectors.toList());

        for (Logistic logistic : validLogistics) {
            inputService.saveLogistic(logistic);
        }

        List<AdditionalSale> validAsales = headerAsales.getASales().stream()
                .filter(this::isValidAsale)
                .peek(additionalSale -> additionalSale.setHeader(header))
                .collect(Collectors.toList());

        for (AdditionalSale additionalSale : validAsales) {
            inputService.saveAsale(additionalSale);
        }

        excelService.updateFileWithLogistics(header, validLogistics);

        return "redirect:/";
    }

    private boolean isValidLogistic(Logistic logistic) {
        return logistic.getDescription() != null && !logistic.getDescription().isBlank() &&
                logistic.getPrice() != null && logistic.getPrice() > 0 &&
                logistic.getCount() != null && logistic.getCount() > 0;
    }

    private boolean isValidAsale(AdditionalSale asale) {
        return asale.getDescription() != null && !asale.getDescription().isBlank() &&
                asale.getPrice() != null && asale.getPrice() > 0 &&
                asale.getCount() != null && asale.getCount() > 0;
    }


}
