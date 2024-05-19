package com.example.demo.services;

import com.example.demo.entities.InputBody;
import com.example.demo.entities.InputHeader;
import com.example.demo.entities.Supplies;
import com.example.demo.entities.forSupplies.Badge;
import com.example.demo.repository.InputBodyRepository;
import com.example.demo.repository.InputHeaderRepository;
import com.example.demo.repository.SuppliesRepository;
import com.example.demo.repository.forSupplies.BadgeRepository;
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
    @Transactional
    public void saveInputSupplies(Supplies sup) {
        suppliesRepository.save(sup);
    }

    @Override

    public void saveInputSuppliesBadges(List<Badge> badges) {
        badgeRepository.saveAll(badges);
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
    @Transactional
    public Badge saveBadge(Badge badge) {
        return badgeRepository.save(badge);
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
}
