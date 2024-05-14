package com.example.demo.services;

import com.example.demo.entities.InputBody;
import com.example.demo.entities.InputHeader;
import com.example.demo.repository.InputBodyRepository;
import com.example.demo.repository.InputHeaderRepository;
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
    private final ExcelServiceImpl excelService;
    //private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Long saveInputHeader(InputHeader header, String username) {
        // Вызываем метод создания InputHeader, так как в данной реализации нет разделения на create и edit
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
    public InputHeader getInputHeaderById(Long id) {
        // Ищем объект InputHeader по его ID в репозитории и возвращаем его
        return inputHeaderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("InputHeader с указанным ID не найден"));
    }

    @Override
    public InputBody getInputBodyById(Long id) {
        // Ищем объект InputHeader по его ID в репозитории и возвращаем его
        return inputBodyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("InputBody с указанным ID не найден"));
    }





    @Override
    public void editInput(InputHeader header) {
        // Проверяем, существует ли объект InputHeader с таким ID в репозитории
        // и сохраняем отредактированный объект
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
