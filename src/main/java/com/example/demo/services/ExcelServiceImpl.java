package com.example.demo.services;

import com.example.demo.entities.InputBody;
import com.example.demo.entities.InputHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelServiceImpl implements ExcelService{
    private final ResourceLoader resourceLoader;
    private final String excelFilesDirectory = "files";

    public File createNewFile(InputHeader header) {

        File newFile = null;
        try {
            Resource resource = resourceLoader.getResource(chooseTemplate(header));
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

    public void updateFileWithBodyData(InputHeader header, List<InputBody> bodies) {
        try {
            String fileName = dateFormatterForFileName(header.getEventStartDate(), header.getEventEndDate())
                    + " " + header.getCustomer() + " СМЕТА РЕГИСТРАЦИИ Ver1";
            String filePath = excelFilesDirectory + File.separator + fileName + ".xlsx";
            File file = new File(filePath);

            if (!file.exists()) {
                throw new FileNotFoundException("Файл не найден: " + filePath);
            }

            Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
            Sheet sheet = workbook.getSheetAt(0);
            writeBody(header, bodies, sheet);

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll();

            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeBody(InputHeader header, List<InputBody> bodies, Sheet sheet) {
        if (header.isSameEquipmentForAllDays()) {
            int additionalRows = 0;
            Row row = sheet.getRow(9);
            Cell cell = row.getCell(0);
            cell.setCellValue(dateFormatterForDeviceCell(header.getEventStartDate(), header.getEventEndDate()));

            Row row1 = sheet.getRow(9);
            Cell cell1 = row1.getCell(1);
            cell1.setCellValue(bodies.get(0).getSoftDevice().getName());

            Row row15 = sheet.getRow(9);
            Cell cell15 = row15.getCell(3);
            cell15.setCellValue(header.getWorkDays());

            Row row2 = sheet.getRow(9);
            Cell cell2 = row2.getCell(4);
            cell2.setCellValue(bodies.get(0).getSDeviceCount());

            Row row3 = sheet.getRow(9);
            Cell cell3 = row3.getCell(5);
            cell3.setCellValue(bodies.get(0).getSDevicePrice());

            Row row4 = sheet.getRow(9);
            Cell cell4 = row4.getCell(7);
            cell4.setCellFormula("E10*F10*" + header.getWorkDays());


            Row row5 = sheet.getRow(10);
            Cell cell5 = row5.getCell(1);
            cell5.setCellValue(bodies.get(0).getPrinterDevice().getName());

            Row row16 = sheet.getRow(10);
            Cell cell16 = row16.getCell(3);
            cell16.setCellValue(header.getWorkDays());

            Row row6 = sheet.getRow(10);
            Cell cell6 = row6.getCell(4);
            cell6.setCellValue(bodies.get(0).getPDeviceCount());

            Row row7 = sheet.getRow(10);
            Cell cell7 = row7.getCell(5);
            cell7.setCellValue(bodies.get(0).getPDevicePrice());

            Row row8 = sheet.getRow(10);
            Cell cell8 = row8.getCell(7);
            cell8.setCellFormula("E11*F11*" + header.getWorkDays());


            Row row17 = sheet.getRow(11);
            Cell cell17 = row17.getCell(3);
            cell17.setCellValue(header.getWorkDays());

            Row row9 = sheet.getRow(11);
            Cell cell9 = row9.getCell(4);
            cell9.setCellValue(bodies.get(0).getSwitchingCount());

            Row row10 = sheet.getRow(11);
            Cell cell10 = row10.getCell(5);
            cell10.setCellValue(bodies.get(0).getSwitchingPrice());

            Row row11 = sheet.getRow(11);
            Cell cell11 = row11.getCell(7);
            cell11.setCellFormula("E12*F12*" + header.getWorkDays());


            Row row18 = sheet.getRow(12);
            Cell cell18 = row18.getCell(3);
            cell18.setCellValue(header.getWorkDays());

            Row row12 = sheet.getRow(12);
            Cell cell12 = row12.getCell(4);
            cell12.setCellValue(bodies.get(0).getNetworkCount());

            Row row13 = sheet.getRow(12);
            Cell cell13 = row13.getCell(5);
            cell13.setCellValue(bodies.get(0).getNetworkPrice());

            Row row14 = sheet.getRow(12);
            Cell cell14 = row14.getCell(7);
            cell14.setCellFormula("E13*F13*" + header.getWorkDays());

            Boolean cam = false;
            Boolean bar = false;
            Boolean rfi = false;
            Boolean tsd = false;
            if (bodies.get(0).getBarcodeDeviceCount() != null && !bodies.get(0).getBarcodeDeviceCount().isBlank() &&
                    bodies.get(0).getBarcodeDevicePrice() != null && !bodies.get(0).getBarcodeDevicePrice().isBlank()) {
                additionalRows++;
                bar = true;
            }
            if (bodies.get(0).getCameraDeviceCount() != null && !bodies.get(0).getCameraDevicePrice().isBlank() &&
                    bodies.get(0).getCameraDevicePrice() != null && !bodies.get(0).getCameraDeviceCount().isBlank()) {
                additionalRows++;
                cam = true;
            }
            if (bodies.get(0).getRfidReaderDeviceCount() != null && !bodies.get(0).getRfidReaderDevicePrice().isBlank() &&
                    bodies.get(0).getRfidReaderDevicePrice() != null && !bodies.get(0).getRfidReaderDeviceCount().isBlank()) {
                additionalRows++;
                rfi = true;
            }
            if (bodies.get(0).getTsdCount() != null && !bodies.get(0).getTsdPrice().isBlank() &&
                    bodies.get(0).getTsdPrice() != null && !bodies.get(0).getTsdCount().isBlank()) {
                additionalRows++;
                tsd = true;
            }
            if (additionalRows > 0) {
                insertRowsWithShift(sheet, 12, additionalRows);
            }
            mergeCells(sheet, 9, 9 + additionalRows + 3);

            int currentRow = 13;
            if (bar) {
                fillRow("2D-Сканер", sheet, currentRow++, bodies.get(0).getBarcodeDeviceCount(), bodies.get(0).getBarcodeDevicePrice(), header.getWorkDays());
            }
            if (cam) {
                fillRow("Web-камера", sheet, currentRow++, bodies.get(0).getCameraDeviceCount(), bodies.get(0).getCameraDevicePrice(), header.getWorkDays());
            }
            if (rfi) {
                fillRow("RFID-считыватель", sheet, currentRow++, bodies.get(0).getRfidReaderDeviceCount(), bodies.get(0).getRfidReaderDevicePrice(), header.getWorkDays());
            }
            if (tsd) {
                fillRow("ТСД", sheet, currentRow++, bodies.get(0).getTsdCount(), bodies.get(0).getTsdPrice(), header.getWorkDays());
            }

            Row rowForNetwork = sheet.getRow(12);
            Cell cellForNetwork = rowForNetwork.getCell(1);
            cellForNetwork.setCellValue("Сетевое оборудование");
        } else {
            int[] neededRows = new int[bodies.size()];
            for (int i = 0; i < bodies.size(); i++) {
                int rowsForEveryBody = 0;
                if (bodies.get(i).getSDeviceCount() != null && !bodies.get(i).getSDeviceCount().isBlank() &&
                        bodies.get(i).getSDevicePrice() != null && !bodies.get(i).getSDevicePrice().isBlank()) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getPDeviceCount() != null && !bodies.get(i).getPDeviceCount().isBlank() &&
                        bodies.get(i).getPDevicePrice() != null && !bodies.get(i).getPDevicePrice().isBlank()) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getBarcodeDeviceCount() != null && !bodies.get(i).getBarcodeDeviceCount().isBlank() &&
                        bodies.get(i).getBarcodeDevicePrice() != null && !bodies.get(i).getBarcodeDevicePrice().isBlank()) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getCameraDeviceCount() != null && !bodies.get(i).getCameraDevicePrice().isBlank() &&
                        bodies.get(i).getCameraDevicePrice() != null && !bodies.get(i).getCameraDeviceCount().isBlank()) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getRfidReaderDeviceCount() != null && !bodies.get(i).getRfidReaderDeviceCount().isBlank() &&
                        bodies.get(i).getRfidReaderDevicePrice() != null && !bodies.get(i).getRfidReaderDevicePrice().isBlank()) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getTsdCount() != null && !bodies.get(i).getTsdPrice().isBlank() &&
                        bodies.get(i).getTsdPrice() != null && !bodies.get(i).getTsdCount().isBlank()) {
                    rowsForEveryBody++;
                }
                neededRows[i] = rowsForEveryBody + 2;
                rowsForEveryBody = 0;
            }

            insertRowsWithShift(sheet, 12, neededRows[0] - 4);
            mergeCells(sheet, 9, 9 + neededRows[0] - 4 + 3);

            //до этого момента все ок
            int rowNum = findFirstRow(sheet, "ИТОГО:");

            for (int i = 1; i < neededRows.length; i++) {
                insertRowsWithShift(sheet, rowNum - 1, neededRows[i]);
            }

            Row rowForNetwork = sheet.getRow(9 + neededRows[0] - 4 + 3);
            Cell cellForNetwork = rowForNetwork.getCell(1);
            cellForNetwork.setCellValue("Сетевое оборудование");
            //создает все правильно, делает строк столько - сколько нужно
            //нужно замерджить ячейки под дни

        }
    }

    public static int findFirstRow(Sheet sheet, String cellContent) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains(cellContent)) {
                    return row.getRowNum(); // Возвращаем номер первой найденной строки
                }
            }
        }
        return -1; // Слово не найдено
    }

    private void fillRow(String deviceName, Sheet sheet, int rowIndex, String deviceCount, String devicePrice, int workDays) {
        Row row = sheet.getRow(rowIndex);

        Cell cellName = row.getCell(1);
        cellName.setCellValue(deviceName);

        Cell cellCount = row.getCell(4);
        cellCount.setCellValue(deviceCount);

        Cell cellPrice = row.getCell(5);
        cellPrice.setCellValue(devicePrice);

        Cell cellDays = row.getCell(3);
        cellDays.setCellValue(workDays);

        Cell cellSum = row.getCell(7);
        cellSum.setCellFormula("E" + (rowIndex + 1) + "*F" + (rowIndex + 1) + "*" + workDays);
    }

    private void insertRowsWithShift(Sheet sheet, int startRow, int numberOfRows) {
        for (int i = 0; i < numberOfRows; i++) {
            int currentRow = startRow + i;
            sheet.shiftRows(currentRow, sheet.getLastRowNum(), 1);

            Row sourceRow = sheet.getRow(currentRow - 1);
            Row newRow = sheet.createRow(currentRow);

            // Копирование стилей и значений ячеек
            copyRow(sourceRow, newRow);

            // Копирование объединенных ячеек
            copyMergedRegions(sheet, currentRow - 1, currentRow);
        }
    }

    private void copyRow(Row sourceRow, Row newRow) {
        for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
            Cell oldCell = sourceRow.getCell(j);
            Cell newCell = newRow.createCell(j);

            if (oldCell == null) {
                newCell = null;
                continue;
            }

            // Копирование стиля ячейки
            CellStyle newCellStyle = newRow.getSheet().getWorkbook().createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
            newCell.setCellStyle(newCellStyle);

            // Копирование значения ячейки
            switch (oldCell.getCellType()) {
                case STRING:
                    newCell.setCellValue(oldCell.getStringCellValue());
                    break;
                case NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());
                    break;
                case BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;
                case FORMULA:
                    newCell.setCellFormula(oldCell.getCellFormula());
                    break;
                default:
                    break;
            }
        }
    }

    private void copyMergedRegions(Sheet sheet, int sourceRowNum, int targetRowNum) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = sheet.getMergedRegion(i);
            if (cellRangeAddress.getFirstRow() == sourceRowNum) {
                CellRangeAddress newCellRangeAddress = new CellRangeAddress(
                        targetRowNum,
                        targetRowNum + (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow()),
                        cellRangeAddress.getFirstColumn(),
                        cellRangeAddress.getLastColumn()
                );
                sheet.addMergedRegion(newCellRangeAddress);
            }
        }
    }

    private void mergeCells(Sheet sheet, int firstRow, int lastRow) {
        // Удаление существующих объединений, которые пересекаются с новыми объединениями
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() >= firstRow && mergedRegion.getLastRow() <= lastRow && mergedRegion.getFirstColumn() == 0) {
                sheet.removeMergedRegion(i);
                i--; // Обновление индекса, поскольку список уменьшился
            }
        }

        // Объединение ячеек
        CellRangeAddress cellRangeAddress = new CellRangeAddress(firstRow, lastRow, 0, 0);
        sheet.addMergedRegion(cellRangeAddress);
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

    private String dateFormatterForDeviceCell(LocalDate start, LocalDate end){

        String prettyDate = null;
        Period period = Period.between(start, end);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
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

    private String chooseTemplate(InputHeader header){
        if(header.isSameEquipmentForAllDays()){
             return "classpath:smeta1.xlsx";
        } else {
            return "classpath:smeta.xlsx";
        }
    }

}
