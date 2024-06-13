package com.example.demo.services;

import com.example.demo.entities.*;
import com.example.demo.entities.forSupplies.*;
import com.example.demo.repository.*;
import com.example.demo.repository.forSupplies.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InputServiceImpl implements InputService {

    private final InputHeaderRepository inputHeaderRepository;
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
    private final ExcelServiceImpl excelService;

    //private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Long saveInputHeader(InputHeader header, String username) {

        header.setAuthor(username);
        header.setWorkDays(countWorkDays(header.getEventStartDate(), header.getEventEndDate()));
        InputHeader savedHeader = inputHeaderRepository.save(header);
        return savedHeader.getId();
    }

    @Override
    public void saveInputBody(List<InputBody> bodies) {
        inputBodyRepository.saveAll(bodies);
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
    public Staff saveStaff(Staff staff) {
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

    private int countWorkDays(LocalDate start, LocalDate end) {
        return (int) (ChronoUnit.DAYS.between(start, end) + 1);
    }

    @Override
    public Bracer saveBracer(Bracer bracer){
        return bracerRepository.save(bracer);
    }
}
