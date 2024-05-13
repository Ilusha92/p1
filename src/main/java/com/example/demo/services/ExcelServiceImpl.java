package com.example.demo.services;


import com.example.demo.entities.InputHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelServiceImpl implements ExcelService{
    private final ResourceLoader resourceLoader;
    private final String excelFilesDirectory = "files";

    public File createNewFile(InputHeader header) {

        File newFile = null;
        try {
            Resource resource = resourceLoader.getResource("classpath:smeta.xlsx");
            InputStream inputStream = resource.getInputStream();

            newFile = File.createTempFile("temp", ".xlsx");
            newFile.deleteOnExit();
            OutputStream outputStream = new FileOutputStream(newFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            Workbook workbook = new XSSFWorkbook(new FileInputStream(newFile));
            Sheet sheet = workbook.getSheetAt(0);
            writeHeader(header,sheet);

            String fileName = dateFormatterForFileName(header.getEventStartDate(), header.getEventEndDate())
                    + " " + header.getCustomer() + " СМЕТА РЕГИСТРАЦИИ Ver1";
            String filePath = excelFilesDirectory + File.separator + fileName + ".xlsx";
            File outputFile = new File(filePath);

            FileOutputStream fileOut = new FileOutputStream(outputFile);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }

    private void writeHeader(InputHeader header, Sheet sheet) {
        // Название мероприятия
        Row row = sheet.getRow(2);
        Cell cell = row.getCell(3);
        cell.setCellValue(header.getEventName());

        // Место проведения(адрес)
        Row row2 = sheet.getRow(3);
        Cell cell2 = row2.getCell(3);
        cell2.setCellValue(header.getEventLocation());

        // Даты проведения(адрес)
        Row row3 = sheet.getRow(5);
        Cell cell3 = row3.getCell(3);
        // Логика с датами
        cell3.setCellValue(dateFormatterForCell(header.getEventStartDate(), header.getEventEndDate()));

        // Время работы
        Row row4 = sheet.getRow(6);
        Cell cell4 = row4.getCell(3);
        cell4.setCellValue(header.getEventWorkStartTime().toString() + "-" + header.getEventWorkEndTime().toString());

        // Количество посетителей
        Row row5 = sheet.getRow(7);
        Cell cell5 = row5.getCell(3);
        cell5.setCellValue(header.getVisitorsCount());
    }

    private String dateFormatterForCell(LocalDate start, LocalDate end){

        String prettyDate = null;
        Period period = Period.between(start, end);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd");
        if (period.getDays() == 0) {
            prettyDate = start.format(formatter);
        } else if(period.getDays() > 0) {
            prettyDate = start.format(formatter2) + " - " + end.format(formatter);
        }
        return prettyDate;
    }

    private String dateFormatterForFileName(LocalDate start, LocalDate end){

        String prettyName = null;
        Period period = Period.between(start, end);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd");
        if (period.getDays() == 0) {
            prettyName = start.format(formatter);
        } else if(period.getDays() > 0) {
            prettyName = start.format(formatter) + " - " + end.format(formatter2);
        }
        return prettyName;
    }

}
