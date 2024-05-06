package com.example.demo.services;

import com.example.demo.entities.Input;
import com.example.demo.repository.InputRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

@Service
public class ExcelServiceImpl {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private InputRepository inputRepository;

    public File updateExcelFile(Input input) {

        File tempFile = null;
        try {
            // Загрузка существующего файла из ресурсов
            Resource resource = resourceLoader.getResource("classpath:smeta.xlsx");
            InputStream inputStream = resource.getInputStream();

            // Создание копии файла
            tempFile = File.createTempFile("temp", ".xlsx");
            tempFile.deleteOnExit();
            OutputStream outputStream = new FileOutputStream(tempFile);

            // Копирование содержимого из существующего файла во временный файл
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            // Закрытие потоков
            inputStream.close();
            outputStream.close();

            // Открываем временный файл для редактирования
            Workbook workbook = new XSSFWorkbook(new FileInputStream(tempFile));
            Sheet sheet = workbook.getSheetAt(0); // Получаем первый лист

            // Записываем данные из объекта в ячейки Excel
            Row row = sheet.getRow(2);
            row.getCell(2).setCellValue(input.getEventName());


            // Сохраняем изменения во временный файл
            FileOutputStream fileOut = new FileOutputStream(tempFile);
            workbook.write(fileOut);
            fileOut.close();

            // Удаление временного файла (если нужно)
            //tempFile.delete();

            // Очистка ресурсов
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }
}
