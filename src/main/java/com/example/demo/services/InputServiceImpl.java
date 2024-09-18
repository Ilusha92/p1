package com.example.demo.services;

import com.example.demo.entities.*;
import com.example.demo.entities.forSupplies.*;
import com.example.demo.repository.*;
import com.example.demo.repository.forSupplies.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InputServiceImpl implements InputService {

    private final InputHeaderRepository inputHeaderRepository;
    private final RegPointRepository regPointRepository;
    private final InputBodyRepository inputBodyRepository;
    private final SuppliesRepository suppliesRepository;
    private final BadgeRepository badgeRepository;
    private final LanyardRepository lanyardRepository;
    private final BracerRepository bracerRepository;
    private final InsertRepository insertRepository;
    private final PocketRepository pocketRepository;
    private final RibbonRepository ribbonRepository;
    private final StickerRepository stickerRepository;
    private final StaffRepository staffRepository;
    private final LogisticRepository logisticRepository;
    private final AsupRepository asupRepository;
    private final MountingRepository mountingRepository;
    private final DeviceRepository deviceRepository;
    private final InputStaffRepository inputStaffRepository;
    private final AdditionalSalesRepository additionalSalesRepository;

    private final ExcelServiceImpl excelService;

    //private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Long saveInputHeader(InputHeader header, String username) {

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
        header.setAuthor(username);
        countWorkDaysAndPeriods(header);
        InputHeader savedHeader = inputHeaderRepository.save(header);
        return savedHeader.getId();
    }

    private void countWorkDaysAndPeriods(InputHeader header) {
        int days = 0;
        int periods = 0;
        if(header.isSameEquipmentForAllDays()){
            if (header.getPrePrintStart() != null && header.getPrePrintEnd() != null) {
                periods++;
            }
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
        } else {
            if (header.getPrePrintStart() != null && header.getPrePrintEnd() != null) {
                days += ChronoUnit.DAYS.between(header.getPrePrintStart(), header.getPrePrintEnd()) + 1;
            }

            days = (int) (days + (ChronoUnit.DAYS.between(header.getEventStartDate(), header.getEventEndDate()) + 1));
            header.setWorkDays(days);
        }
        header.setWorkDays(days);
        header.setPeriods(periods);
    }

    @Override
    public RegPoint saveRegPoint(RegPoint regPoint) {
        return regPointRepository.save(regPoint);
    }

    @Override
    public Device saveDevice(Device device) {
        return deviceRepository.save(device);
    }

    @Override
    public InputBody saveInputBody(InputBody body) {
        return inputBodyRepository.save(body);
    }

    @Override
    public void saveInputSupplies(Supplies sup) {
        suppliesRepository.save(sup);
    }

    @Override
    public void saveInputSuppliesBadges(List<Badge> badges) {
        badgeRepository.saveAll(badges);
    }

    @Override
    public Logistic saveLogistic(Logistic logistic){
        return logisticRepository.save(logistic);
    }

    @Override
    public InputHeader getInputHeaderById(Long id) {
        return inputHeaderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("InputHeader с указанным ID не найден"));
    }

    @Override
    public InputBody getInputBodyById(Long id) {
        return inputBodyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("InputBody с указанным ID не найден"));
    }

    @Override
    public Supplies getSuppliesById(Long id) {
        return suppliesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("InputBody с указанным ID не найден"));
    }

    @Override
    public Badge saveBadge(Badge badge) {
        return badgeRepository.save(badge);
    }

    @Override
    public Lanyard saveLanyard(Lanyard lanyard) {
        return lanyardRepository.save(lanyard);
    }

    @Override
    public Insert saveInsert(Insert insert) {
        return insertRepository.save(insert);
    }

    @Override
    public Pocket savePocket(Pocket pocket) {
        return pocketRepository.save(pocket);
    }

    @Override
    public Ribbon saveRibbon(Ribbon ribbon){
        return ribbonRepository.save(ribbon);
    }

    @Override
    public Sticker saveSticker(Sticker sticker) {
        return stickerRepository.save(sticker);
    }

    @Override
    public Asup saveAsup(Asup asup) {
        return asupRepository.save(asup);
    }

    @Override
    public AdditionalSale saveAsale(AdditionalSale additionalSale){
        return additionalSalesRepository.save(additionalSale);
    }

    @Override
    public Mounting saveMounting(Mounting mounting){
        return mountingRepository.save(mounting);
    }

    @Override
    public InputStaff saveInputStaff(InputStaff inputStaff) {
        return inputStaffRepository.save(inputStaff);
    }

    @Override
    public Staff saveStaff(Staff staff){
        return staffRepository.save(staff);
    }

    @Override
    public void editInput(InputHeader header) {
        if (inputHeaderRepository.existsById(header.getId())) {
            inputHeaderRepository.save(header);
        } else {
            throw new IllegalArgumentException("InputHeader с указанным ID не найден");
        }
    }

    @Override
    public List<InputHeader> getUserInputs() {
        return null;
    }


    @Override
    public Bracer saveBracer(Bracer bracer){
        return bracerRepository.save(bracer);
    }

    public List<String> getDatesOfWorkDays(InputHeader header) {
        List<String> dates = new ArrayList<>();

        // Получаем значения дат из header
        LocalDate prePrintStart = header.getPrePrintStart();
        LocalDate prePrintEnd = header.getPrePrintEnd();
        LocalDate eventStartDate = header.getEventStartDate();
        LocalDate eventEndDate = header.getEventEndDate();

        // Добавляем даты для предрегистрации (PrePrint)
        if (prePrintStart != null && prePrintEnd != null) {
            for (LocalDate date = prePrintStart; !date.isAfter(prePrintEnd); date = date.plusDays(1)) {
                dates.add(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            }
        }

        // Добавляем даты для основного события (Event)
        if (eventStartDate != null && eventEndDate != null) {
            for (LocalDate date = eventStartDate; !date.isAfter(eventEndDate); date = date.plusDays(1)) {
                dates.add(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            }
        }

        return dates;
    }


    public List<String> getRegPointsStrings(InputHeader header) {
        List<String> stringsRegPoints = new ArrayList<>();
        List<RegPoint> regPointsForSort = sortRegPoints(header.getRegPoints());

        for (RegPoint regPoint : regPointsForSort) {
            // Проверяем, равны ли даты
            String dateRange;
            if (regPoint.getStartRPDate().equals(regPoint.getEndRPDate())) {
                // Если даты одинаковы, выводим одну дату
                dateRange = regPoint.getStartRPDate().toString();
            } else {
                // Если даты разные, выводим диапазон
                dateRange = regPoint.getStartRPDate().toString() + " - " + regPoint.getEndRPDate().toString();
            }

            // Формируем строку с описанием и датами
            String variants = "Устройства для " + regPoint.getDescription() +
                    " (" + regPoint.getName() + "). " + dateRange;

            stringsRegPoints.add(variants);
        }

        return stringsRegPoints;
    }

    public List<RegPoint> sortRegPoints(List<RegPoint> regPoints) {
        // Задаём фиксированный порядок сортировки для поля "description"
        List<String> descriptionOrder = Arrays.asList("Предрегистрация", "Аккредитация", "Контроль доступа");

        // Сортируем список точек аккредитации по двум критериям
        return regPoints.stream()
                .sorted(Comparator
                        .comparing(RegPoint::getStartRPDate) // Сначала сортировка по дате начала
                        .thenComparing(rp -> descriptionOrder.indexOf(rp.getDescription())) // Затем по порядку значений description
                )
                .toList();
    }

    public List<String> getPeriodsStrings(InputHeader header) {
        List<String> periodsStrings = new ArrayList<>();

        // Добавляем период prePrint с меткой "Предрегистрация"
        if (header.getPrePrintStart() != null && header.getPrePrintEnd() != null) {
            String prePrintPeriod = "Предрегистрация: " + header.getPrePrintStart() + " - " + header.getPrePrintEnd();
            periodsStrings.add(prePrintPeriod);
        }

        // Добавляем периоды boolStart - boolEnd с метками "Период 1", "Период 2" и т.д.
        int periodCount = 1;
        periodCount = addPeriodToList(periodsStrings, header.getBoolStart1(), header.getBoolEnd1(), periodCount);
        periodCount = addPeriodToList(periodsStrings, header.getBoolStart2(), header.getBoolEnd2(), periodCount);
        periodCount = addPeriodToList(periodsStrings, header.getBoolStart3(), header.getBoolEnd3(), periodCount);
        periodCount = addPeriodToList(periodsStrings, header.getBoolStart4(), header.getBoolEnd4(), periodCount);

        return periodsStrings;
    }

    private int addPeriodToList(List<String> periodsStrings, LocalDate start, LocalDate end, int periodCount) {
        if (start != null && end != null) {
            String period = "Период " + periodCount + ": " + start + " - " + end;
            periodsStrings.add(period);
            periodCount++;
        }
        return periodCount;
    }



}
