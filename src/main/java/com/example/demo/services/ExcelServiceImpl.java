package com.example.demo.services;

import com.example.demo.entities.*;
import com.example.demo.repository.InputHeaderRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelServiceImpl implements ExcelService {
    private final ResourceLoader resourceLoader;
    private final String excelFilesDirectory = "files";
    HashMap<String, List<Integer>> formulaMap = new HashMap<>();

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
            writeHeader(header, sheet);
            String fileName = dateFormatterForFileName(header.getEventStartDate(), header.getEventEndDate())
                    + " " + header.getCustomer() + " СМЕТА РЕГИСТРАЦИИ Ver1";
            String filePath = excelFilesDirectory + File.separator + fileName + ".xlsx";
            File outputFile = new File(filePath);

            formulaMap.put(fileName, new ArrayList<>());

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

    public void updateFileWithBodyData(InputHeader header, List<InputBody> bodies, int periods) {
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

            CellStyle topRowStyle = workbook.createCellStyle();
            topRowStyle.setAlignment(HorizontalAlignment.CENTER);
            topRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            topRowStyle.setBorderTop(BorderStyle.DOUBLE);
            topRowStyle.setBorderLeft(BorderStyle.THIN);
            topRowStyle.setBorderRight(BorderStyle.THIN);

            CellStyle topRowStyle1 = workbook.createCellStyle();
            topRowStyle1.setAlignment(HorizontalAlignment.LEFT);
            topRowStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
            topRowStyle1.setBorderTop(BorderStyle.DOUBLE);
            topRowStyle1.setBorderLeft(BorderStyle.THIN);
            topRowStyle1.setBorderRight(BorderStyle.THIN);

            writeBody(header, bodies, sheet, periods, topRowStyle, topRowStyle1);


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

    private void writeBody(InputHeader header, List<InputBody> bodies, Sheet sheet, int periods,
                           CellStyle topRowCellsStyleAlign, CellStyle topRowCellsStyleAlign1) {
        if (header.isSameEquipmentForAllDays() && periods == 1) {
            int additionalRows = 0;

            Boolean sDevice = false;
            Boolean pDevice = false;
            Boolean switching = false;
            Boolean network = false;
            Boolean cam = false;
            Boolean bar = false;
            Boolean rfi = false;
            Boolean tsd = false;

            if (bodies.get(0).getSDeviceCount() != null && bodies.get(0).getSDevicePrice() != null) {
                additionalRows++;
                sDevice = true;
            }
            if (bodies.get(0).getPDeviceCount() != null && bodies.get(0).getPDevicePrice() != null) {
                additionalRows++;
                pDevice = true;
            }
            if (bodies.get(0).getSwitchingCount() != null && bodies.get(0).getSwitchingPrice() != null) {
                additionalRows++;
                switching = true;
            }
            if (bodies.get(0).getNetworkCount() != null && bodies.get(0).getNetworkPrice() != null) {
                additionalRows++;
                network = true;
            }
            if (bodies.get(0).getBarcodeDeviceCount() != null && bodies.get(0).getBarcodeDevicePrice() != null) {
                additionalRows++;
                bar = true;
            }
            if (bodies.get(0).getCameraDeviceCount() != null && bodies.get(0).getCameraDevicePrice() != null) {
                additionalRows++;
                cam = true;
            }
            if (bodies.get(0).getRfidReaderDeviceCount() != null && bodies.get(0).getRfidReaderDevicePrice() != null) {
                additionalRows++;
                rfi = true;
            }
            if (bodies.get(0).getTsdCount() != null && bodies.get(0).getTsdPrice() != null) {
                additionalRows++;
                tsd = true;
            }
            insertRowsWithShift(sheet, 9, additionalRows);
            if (additionalRows > 1) {
                mergeCells(sheet, 9, 9 + additionalRows - 1);
            }

            int currentRow = 9;
            if (sDevice) {
                fillRow(bodies.get(0).getSoftDevice().getName(), sheet, currentRow++, bodies.get(0).getSDeviceCount(), bodies.get(0).getSDevicePrice(), header.getWorkDays());
            }
            if (pDevice) {
                fillRow(bodies.get(0).getPrinterDevice().getName(), sheet, currentRow++, bodies.get(0).getPDeviceCount(), bodies.get(0).getPDevicePrice(), header.getWorkDays());
            }
            if (switching) {
                fillRow("Кабельная коммутация", sheet, currentRow++, bodies.get(0).getSwitchingCount(), bodies.get(0).getSwitchingPrice(), header.getWorkDays());
            }
            if (network) {
                fillRow("Cетевое оборудование", sheet, currentRow++, bodies.get(0).getNetworkCount(), bodies.get(0).getNetworkPrice(), header.getWorkDays());
            }
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

            Row periodsRow = sheet.getRow(9);
            Cell cell = periodsRow.getCell(0);
            cell.setCellValue(dateFormatterForDeviceCell(header.getEventStartDate(), header.getEventEndDate()));

            Row formulaRow = sheet.getRow(currentRow);
            Cell formulaCell = formulaRow.getCell(7);
            formulaCell.setCellFormula("SUM(H" + 10 + ":H" + (9 + additionalRows) +")");

        } else if (header.isSameEquipmentForAllDays() && periods > 1) {
            int[] neededRows = new int[bodies.size()];
            for (int i = 0; i < bodies.size(); i++) {

                int rowsForEveryBody = 0;
                if (bodies.get(i).getSDeviceCount() != null && bodies.get(i).getSDevicePrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getPDeviceCount() != null && bodies.get(i).getPDevicePrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getSwitchingCount() != null && bodies.get(i).getSwitchingPrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getNetworkCount() != null && bodies.get(i).getNetworkPrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getBarcodeDeviceCount() != null && bodies.get(i).getBarcodeDevicePrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getCameraDeviceCount() != null && bodies.get(i).getCameraDevicePrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getRfidReaderDeviceCount() != null && bodies.get(i).getRfidReaderDevicePrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getTsdCount() != null && bodies.get(i).getTsdPrice() != null) {
                    rowsForEveryBody++;
                }
                neededRows[i] = rowsForEveryBody;
                rowsForEveryBody = 0;
            }

            int startRow = 9;
            for (int i = 0; i < neededRows.length; i++) {

                insertRowsWithShift(sheet, startRow, neededRows[i]);//в
                if(neededRows[i]>1) {
                    mergeCells(sheet, startRow, startRow + neededRows[i] - 1);
                }
                Row rowForOtherPeriod = sheet.getRow(startRow);
                Cell cellForOtherPeriod = rowForOtherPeriod.getCell(0);
                cellForOtherPeriod.setCellValue(getHeaderPeriods(header).get(i));
                startRow = startRow + neededRows[i];

                //между блоками двойную линию
                for (int j = 0; j < 8; j++) {
                    if (j == 1) {
                        rowForOtherPeriod.getCell(j).setCellStyle(topRowCellsStyleAlign1); // Применяем другой стиль для j = 1
                    } else {
                        rowForOtherPeriod.getCell(j).setCellStyle(topRowCellsStyleAlign); // Применяем основной стиль для остальных ячеек
                    }
                }
            }
            checkTheBody(bodies, sheet, header);
            int sum = Arrays.stream(neededRows).sum();
            System.out.println(sum);

            Row formulaRow = sheet.getRow(9 + sum);
            Cell formulaCell = formulaRow.getCell(7);
            formulaCell.setCellFormula("SUM(H" + 10 + ":H" + (9 + sum) +")");

        } else {
            int[] neededRows = new int[bodies.size()];
            for (int i = 0; i < bodies.size(); i++) {
                int rowsForEveryBody = 0;
                if (bodies.get(i).getSDeviceCount() != null && bodies.get(i).getSDevicePrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getPDeviceCount() != null && bodies.get(i).getPDevicePrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getSwitchingCount() != null && bodies.get(i).getSwitchingPrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getNetworkCount() != null && bodies.get(i).getNetworkPrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getBarcodeDeviceCount() != null && bodies.get(i).getBarcodeDevicePrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getCameraDeviceCount() != null && bodies.get(i).getCameraDevicePrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getRfidReaderDeviceCount() != null && bodies.get(i).getRfidReaderDevicePrice() != null) {
                    rowsForEveryBody++;
                }
                if (bodies.get(i).getTsdCount() != null && bodies.get(i).getTsdPrice() != null) {
                    rowsForEveryBody++;
                }
                neededRows[i] = rowsForEveryBody;
                rowsForEveryBody = 0;
            }

            int startRow = 9;
            for (int i = 0; i < neededRows.length; i++) {
                insertRowsWithShift(sheet, startRow, neededRows[i]);
                if(neededRows[i]>1) {
                    mergeCells(sheet, startRow, startRow + neededRows[i]-1);
                }
                Row rowsForDates = sheet.getRow(startRow);
                Cell cellForDates = rowsForDates.getCell(0);
                cellForDates.setCellValue(header.getEventStartDate().plusDays(i).toString());

                for (int j = 0; j < 8; j++) {
                    if (j == 1) {
                        rowsForDates.getCell(j).setCellStyle(topRowCellsStyleAlign1); // Применяем другой стиль для j = 1
                    } else {
                        rowsForDates.getCell(j).setCellStyle(topRowCellsStyleAlign); // Применяем основной стиль для остальных ячеек
                    }
                }

                startRow = startRow + neededRows[i];
            }
            checkTheBody(bodies, sheet, header);
            int sum = Arrays.stream(neededRows).sum();
            System.out.println(sum);

            Row formulaRow = sheet.getRow(9 + sum);
            Cell formulaCell = formulaRow.getCell(7);
            formulaCell.setCellFormula("SUM(H" + 10 + ":H" + (9 + sum) +")");

            }

    }

    public static int findFirstRow(Sheet sheet, String cellContent) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains(cellContent)) {
                    return row.getRowNum();
                }
            }
        }
        return -1;
    }

    private void fillRow(String deviceName, Sheet sheet, int rowIndex, Integer deviceCount, Integer devicePrice, int workDays) {
        Row row = sheet.getRow(rowIndex);

        Cell cellName = row.getCell(1);
        cellName.setCellValue(deviceName);

        Cell cellDays = row.getCell(3);
        cellDays.setCellValue(workDays);

        Cell cellCount = row.getCell(4);
        cellCount.setCellValue(deviceCount);

        Cell cellPrice = row.getCell(5);
        cellPrice.setCellValue(devicePrice);

        Cell cellSum = row.getCell(7);
        cellSum.setCellFormula("E" + (rowIndex + 1) + "*F" + (rowIndex + 1) + "*" + workDays);
    }

    private String dateFormatterForCell(LocalDate start, LocalDate end) {

        String prettyDate = null;
        Period period = Period.between(start, end);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd");
        if (period.getDays() == 0) {
            prettyDate = start.format(formatter);
        } else if (period.getDays() > 0) {
            prettyDate = start.format(formatter2) + " - " + end.format(formatter);
        }
        return prettyDate;
    }

    private String dateFormatterForDeviceCell(LocalDate start, LocalDate end) {

        String prettyDate = null;
        Period period = Period.between(start, end);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd");
        if (period.getDays() == 0) {
            prettyDate = start.format(formatter);
        } else if (period.getDays() > 0) {
            prettyDate = start.format(formatter2) + " - " + end.format(formatter);
        }
        return prettyDate;
    }

    private String dateFormatterForFileName(LocalDate start, LocalDate end) {

        String prettyName = null;
        Period period = Period.between(start, end);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd");
        if (period.getDays() == 0) {
            prettyName = start.format(formatter);
        } else if (period.getDays() > 0) {
            prettyName = start.format(formatter) + " - " + end.format(formatter2);
        }
        return prettyName;
    }

    private String chooseTemplate(InputHeader header) {
        if (header.isSameEquipmentForAllDays()) {
            return "classpath:smeta1.xlsx";
        } else {
            return "classpath:smeta.xlsx";
        }
    }

    private void checkTheBody(List<InputBody> bodies, Sheet sheet, InputHeader header) {
        int currentRow = 9;
        for (int i = 0; i < bodies.size(); i++) {
            if (bodies.get(i).getSDeviceCount() != null && bodies.get(i).getSDevicePrice() != null) {
                Row rowDevice = sheet.getRow(currentRow);
                Cell cellSDName = rowDevice.getCell(1);
                cellSDName.setCellValue(bodies.get(i).getSoftDevice().getName());
                Cell cellSDCount = rowDevice.getCell(4);
                cellSDCount.setCellValue(bodies.get(i).getSDeviceCount());
                Cell cellSDPrice = rowDevice.getCell(5);
                cellSDPrice.setCellValue(bodies.get(i).getSDevicePrice());
                Cell cellSum = rowDevice.getCell(7);
                Cell cellWorkDays = rowDevice.getCell(3);
                if (header.isSameEquipmentForAllDays()) {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1) + "*D" + (currentRow + 1));
                    cellWorkDays.setCellValue(getDaysInPeriods(header).get(i));
                } else {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                }
                currentRow++;
            }
            if (bodies.get(i).getPDeviceCount() != null && bodies.get(i).getPDevicePrice() != null) {
                Row rowPrinter = sheet.getRow(currentRow);
                Cell cellPDName = rowPrinter.getCell(1);
                cellPDName.setCellValue(bodies.get(i).getPrinterDevice().getName());
                Cell cellPDCount = rowPrinter.getCell(4);
                cellPDCount.setCellValue(bodies.get(i).getPDeviceCount());
                Cell cellPDPrice = rowPrinter.getCell(5);
                cellPDPrice.setCellValue(bodies.get(i).getPDevicePrice());
                Cell cellSum = rowPrinter.getCell(7);
                Cell cellWorkDays = rowPrinter.getCell(3);
                if (header.isSameEquipmentForAllDays()) {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1) + "*D" + (currentRow + 1));
                    cellWorkDays.setCellValue(getDaysInPeriods(header).get(i));
                } else {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                }
                currentRow++;
            }
            if (bodies.get(i).getSwitchingCount() != null && bodies.get(i).getSwitchingPrice() != null) {
                Row rowSwitching = sheet.getRow(currentRow);
                Cell cellSWName = rowSwitching.getCell(1);
                cellSWName.setCellValue("Кабельная коммутация");
                Cell cellSWCount = rowSwitching.getCell(4);
                cellSWCount.setCellValue(bodies.get(i).getSwitchingCount());
                Cell cellSWPrice = rowSwitching.getCell(5);
                cellSWPrice.setCellValue(bodies.get(i).getSwitchingPrice());
                Cell cellSum = rowSwitching.getCell(7);
                Cell cellWorkDays = rowSwitching.getCell(3);
                if (header.isSameEquipmentForAllDays()) {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1) + "*D" + (currentRow + 1));
                    cellWorkDays.setCellValue(getDaysInPeriods(header).get(i));
                } else {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                }
                currentRow++;
            }
            if (bodies.get(i).getNetworkCount() != null && bodies.get(i).getNetworkPrice() != null) {
                Row rowNetwork = sheet.getRow(currentRow);
                Cell cellNWName = rowNetwork.getCell(1);
                cellNWName.setCellValue("Cетевое оборудование");
                Cell cellNWCount = rowNetwork.getCell(4);
                cellNWCount.setCellValue(bodies.get(i).getNetworkCount());
                Cell cellNWPrice = rowNetwork.getCell(5);
                cellNWPrice.setCellValue(bodies.get(i).getNetworkPrice());
                Cell cellSum = rowNetwork.getCell(7);
                Cell cellWorkDays = rowNetwork.getCell(3);
                if (header.isSameEquipmentForAllDays()) {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1) + "*D" + (currentRow + 1));
                    cellWorkDays.setCellValue(getDaysInPeriods(header).get(i));
                } else {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                }
                currentRow++;
            }
            if (bodies.get(i).getBarcodeDeviceCount() != null && bodies.get(i).getBarcodeDevicePrice() != null) {
                Row rowBarcode = sheet.getRow(currentRow);
                Cell cellBCName = rowBarcode.getCell(1);
                cellBCName.setCellValue("2D-сканер");
                Cell cellBCCount = rowBarcode.getCell(4);
                cellBCCount.setCellValue(bodies.get(i).getBarcodeDeviceCount());
                Cell cellBCPrice = rowBarcode.getCell(5);
                cellBCPrice.setCellValue(bodies.get(i).getBarcodeDevicePrice());
                Cell cellSum = rowBarcode.getCell(7);
                Cell cellWorkDays = rowBarcode.getCell(3);
                if (header.isSameEquipmentForAllDays()) {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1) + "*D" + (currentRow + 1));
                    cellWorkDays.setCellValue(getDaysInPeriods(header).get(i));
                } else {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                }
                currentRow++;
            }
            if (bodies.get(i).getCameraDeviceCount() != null && bodies.get(i).getCameraDevicePrice() != null) {
                Row rowCamera = sheet.getRow(currentRow);
                Cell cellCMName = rowCamera.getCell(1);
                cellCMName.setCellValue("Web-камера");
                Cell cellCMCount = rowCamera.getCell(4);
                cellCMCount.setCellValue(bodies.get(i).getCameraDeviceCount());
                Cell cellCMPrice = rowCamera.getCell(5);
                cellCMPrice.setCellValue(bodies.get(i).getCameraDevicePrice());
                Cell cellSum = rowCamera.getCell(7);
                Cell cellWorkDays = rowCamera.getCell(3);
                if (header.isSameEquipmentForAllDays()) {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1) + "*D" + (currentRow + 1));
                    cellWorkDays.setCellValue(getDaysInPeriods(header).get(i));
                } else {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                }
                currentRow++;
            }
            if (bodies.get(i).getRfidReaderDeviceCount() != null && bodies.get(i).getRfidReaderDevicePrice() != null) {
                Row rowRfid = sheet.getRow(currentRow);
                Cell cellRFName = rowRfid.getCell(1);
                cellRFName.setCellValue("RFID-считыватель");
                Cell cellRFCount = rowRfid.getCell(4);
                cellRFCount.setCellValue(bodies.get(i).getRfidReaderDeviceCount());
                Cell cellRFPrice = rowRfid.getCell(5);
                cellRFPrice.setCellValue(bodies.get(i).getRfidReaderDevicePrice());
                Cell cellSum = rowRfid.getCell(7);
                Cell cellWorkDays = rowRfid.getCell(3);
                if (header.isSameEquipmentForAllDays()) {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1) + "*D" + (currentRow + 1));
                    cellWorkDays.setCellValue(getDaysInPeriods(header).get(i));
                } else {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                }
                currentRow++;
            }
            if (bodies.get(i).getTsdCount() != null && bodies.get(i).getTsdPrice() != null) {
                Row rowTsd = sheet.getRow(currentRow);
                Cell cellTSDName = rowTsd.getCell(1);
                cellTSDName.setCellValue("ТСД");
                Cell cellTSDCount = rowTsd.getCell(4);
                cellTSDCount.setCellValue(bodies.get(i).getTsdCount());
                Cell cellTSDPrice = rowTsd.getCell(5);
                cellTSDPrice.setCellValue(bodies.get(i).getTsdPrice());
                Cell cellSum = rowTsd.getCell(7);
                Cell cellWorkDays = rowTsd.getCell(3);
                if (header.isSameEquipmentForAllDays()) {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1) + "*D" + (currentRow + 1));
                    cellWorkDays.setCellValue(getDaysInPeriods(header).get(i));
                } else {
                    cellSum.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                }
                currentRow++;
            }
        }
    }

    public List<String> getHeaderPeriods(InputHeader header) {
        List<String> periods = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MMM");

        // Проверяем первый период
        if (header.getBoolStart1() != null && header.getBoolEnd1() != null) {
            if (header.getBoolStart1().isEqual(header.getBoolEnd1())) {
                periods.add(header.getBoolStart1().format(formatter));
            } else {
                periods.add(header.getBoolStart1().format(formatter) + " - " + header.getBoolEnd1().format(formatter));
            }
        }

        // Проверяем второй период
        if (header.getBoolStart2() != null && header.getBoolEnd2() != null) {
            if (header.getBoolStart2().isEqual(header.getBoolEnd2())) {
                periods.add(header.getBoolStart2().format(formatter));
            } else {
                periods.add(header.getBoolStart2().format(formatter) + " - " + header.getBoolEnd2().format(formatter));
            }
        }

        // Проверяем третий период
        if (header.getBoolStart3() != null && header.getBoolEnd3() != null) {
            if (header.getBoolStart3().isEqual(header.getBoolEnd3())) {
                periods.add(header.getBoolStart3().format(formatter));
            } else {
                periods.add(header.getBoolStart3().format(formatter) + " - " + header.getBoolEnd3().format(formatter));
            }
        }

        // Проверяем, остались ли неохваченные даты между последним периодом и eventEndDate
        LocalDate lastPeriodEnd = header.getEventStartDate().minusDays(1);

        if (header.getBoolEnd3() != null) {
            lastPeriodEnd = header.getBoolEnd3();
        } else if (header.getBoolEnd2() != null) {
            lastPeriodEnd = header.getBoolEnd2();
        } else if (header.getBoolEnd1() != null) {
            lastPeriodEnd = header.getBoolEnd1();
        }

        if (lastPeriodEnd.isBefore(header.getEventEndDate())) {
            if (lastPeriodEnd.plusDays(1).isEqual(header.getEventEndDate())) {
                periods.add(header.getEventEndDate().format(formatter));
            } else {
                periods.add(lastPeriodEnd.plusDays(1).format(formatter) + " - " + header.getEventEndDate().format(formatter));
            }
        }

        return periods;
    }

    public static List<Integer> getDaysInPeriods(InputHeader header) {
        List<Integer> daysInPeriods = new ArrayList<>();

        // Проверяем первый период
        if (header.getBoolStart1() != null && header.getBoolEnd1() != null) {
            long days = ChronoUnit.DAYS.between(header.getBoolStart1(), header.getBoolEnd1()) + 1;
            daysInPeriods.add((int) days);
        }

        // Проверяем второй период
        if (header.getBoolStart2() != null && header.getBoolEnd2() != null) {
            long days = ChronoUnit.DAYS.between(header.getBoolStart2(), header.getBoolEnd2()) + 1;
            daysInPeriods.add((int) days);
        }

        // Проверяем третий период
        if (header.getBoolStart3() != null && header.getBoolEnd3() != null) {
            long days = ChronoUnit.DAYS.between(header.getBoolStart3(), header.getBoolEnd3()) + 1;
            daysInPeriods.add((int) days);
        }

        // Проверяем, остались ли неохваченные дни между последним периодом и eventEndDate
        LocalDate lastPeriodEnd = getLastPeriodEnd(header);
        if (lastPeriodEnd.isBefore(header.getEventEndDate())) {
            long remainingDays = ChronoUnit.DAYS.between(lastPeriodEnd.plusDays(1), header.getEventEndDate()) + 1;
            daysInPeriods.add((int) remainingDays);
        }

        return daysInPeriods;
    }

    private static LocalDate getLastPeriodEnd(InputHeader header) {
        LocalDate lastPeriodEnd = header.getEventStartDate().minusDays(1); // Начнем с даты перед началом события

        if (header.getBoolEnd1() != null) {
            lastPeriodEnd = header.getBoolEnd1();
        }
        if (header.getBoolEnd2() != null && header.getBoolEnd2().isAfter(lastPeriodEnd)) {
            lastPeriodEnd = header.getBoolEnd2();
        }
        if (header.getBoolEnd3() != null && header.getBoolEnd3().isAfter(lastPeriodEnd)) {
            lastPeriodEnd = header.getBoolEnd3();
        }

        return lastPeriodEnd;
    }

    public void updateFileWithSuppliesData(InputHeader header, Supplies supplies) {

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

            CellStyle topRowStyle = workbook.createCellStyle();
            topRowStyle.setAlignment(HorizontalAlignment.CENTER);
            topRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            topRowStyle.setBorderTop(BorderStyle.DOUBLE);
            topRowStyle.setBorderLeft(BorderStyle.THIN);
            topRowStyle.setBorderRight(BorderStyle.THIN);

            CellStyle topRowStyle1 = workbook.createCellStyle();
            topRowStyle1.setAlignment(HorizontalAlignment.LEFT);
            topRowStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
            topRowStyle1.setBorderTop(BorderStyle.DOUBLE);
            topRowStyle1.setBorderLeft(BorderStyle.THIN);
            topRowStyle1.setBorderRight(BorderStyle.THIN);

            writeSupplies(header, supplies, sheet);

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

    private void writeSupplies(InputHeader header, Supplies supplies, Sheet sheet) {
        // Подсчитываем количество строк для каждого типа расходников
        int[] mass = new int[8];
        mass[0] = (int) supplies.getBadges().stream()
                .filter(b -> isValidSupplies(Optional.ofNullable(b.getPrice()), Optional.ofNullable(b.getCount())))
                .count();
        mass[1] = (int) supplies.getLanyards().stream()
                .filter(l -> isValidSupplies(Optional.ofNullable(l.getPrice()), Optional.ofNullable(l.getCount())))
                .count();
        mass[2] = (int) supplies.getStickers().stream()
                .filter(s -> isValidSupplies(Optional.ofNullable(s.getPrice()), Optional.ofNullable(s.getCount())))
                .count();
        mass[3] = (int) supplies.getBracers().stream()
                .filter(b -> isValidSupplies(Optional.ofNullable(b.getPrice()), Optional.ofNullable(b.getCount())))
                .count();
        mass[4] = (int) supplies.getInserts().stream()
                .filter(i -> isValidSupplies(Optional.ofNullable(i.getPrice()), Optional.ofNullable(i.getCount())))
                .count();
        mass[5] = (int) supplies.getPockets().stream()
                .filter(p -> isValidSupplies(Optional.ofNullable(p.getPrice()), Optional.ofNullable(p.getCount())))
                .count();
        mass[6] = (int) supplies.getRibbons().stream()
                .filter(r -> isValidSupplies(Optional.ofNullable(r.getPrice()), Optional.ofNullable(r.getCount())))
                .count();
        mass[7] = (int) supplies.getAsups().stream()
                .filter(a -> isValidSupplies(Optional.ofNullable(a.getPrice()), Optional.ofNullable(a.getCount())))
                .count();

        int neededRows = Arrays.stream(mass).sum();
        System.out.println(neededRows);
        int startRow = findFirstRow(sheet, "РАСХОДНЫЕ МАТЕРИАЛЫ");

        if(neededRows == 0){
            removeMergedRegions(sheet, startRow+1);
            removeRow(sheet, startRow+1);
            removeMergedRegions(sheet, startRow);
            removeRow(sheet, startRow);

            Row rowForChangeBlockNumber = sheet.getRow(findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ"));
            Cell cellForChangeBlockNumber = rowForChangeBlockNumber.getCell(0);
            cellForChangeBlockNumber.setCellValue(4);

        } else {
            insertRowsWithShift(sheet, startRow+1, neededRows);
            fillSupplies(supplies, sheet, startRow+1, mass);
            if(neededRows>1) {
                mergeCells(sheet, startRow+1, startRow+1 + neededRows - 1);
                Row rowWithMergedRegion = sheet.getRow(startRow + 1);
                Cell cellForBlank = rowWithMergedRegion.getCell(0);
                cellForBlank.setCellType(CellType.BLANK);
            } else {
                Row rowWithMergedRegion = sheet.getRow(startRow + 1);
                Cell cellForBlank = rowWithMergedRegion.getCell(0);
                cellForBlank.setCellType(CellType.BLANK);
            }
            Row formulaRow = sheet.getRow(startRow+1 + neededRows);
            Cell formulaCell = formulaRow.getCell(7);
            formulaCell.setCellFormula("SUM(H" + (startRow+2) + ":H" + (startRow + neededRows + 1) +")");

            int formulaRowIndex = formulaRow.getRowNum();
            String fileName = dateFormatterForFileName(header.getEventStartDate(), header.getEventEndDate())
                    + " " + header.getCustomer() + " СМЕТА РЕГИСТРАЦИИ Ver1";
            //отладка
            System.out.println("Вот" + formulaRowIndex);
            if (formulaMap.containsKey(fileName)) {
                formulaMap.get(fileName).add(formulaRowIndex);
            } else {
                // Логирование ошибки, если файл не был инициализирован
                log.error("File {} not found in formulaMap", fileName);
            }
        }

    }

    private boolean isValidSupplies(Optional<Integer> price, Optional<Integer> count) {
        return price.orElse(0) > 0 && count.orElse(0) > 0;
    }

    private void fillSupplies(Supplies supplies, Sheet sheet, int startRow, int[] mass) {
        int currentRow = startRow;
        for (int i = 0; i < mass.length; i++) {
            for (int j = 0; j < mass[i]; j++) {
                Row rowForSupplies = sheet.getRow(currentRow);
                Cell cellForName = rowForSupplies.getCell(1);
                Cell cellForPrice = rowForSupplies.getCell(5);
                Cell cellForCount = rowForSupplies.getCell(4);
                Cell cellForFormula = rowForSupplies.getCell(7);

                switch (i) {
                    case 0: // Badges
                        String badgeDesc = "Бэйдж " + supplies.getBadges().get(j).getBadgeMaterial().toLowerCase() +
                                (supplies.getBadges().get(j).isRfid() ? " с RFID-меткой" : "") + " (" +
                                supplies.getBadges().get(j).getSize() + ", " + supplies.getBadges().get(j).getChroma() +
                                ", " + supplies.getBadges().get(j).getLaminationKind().toLowerCase() + ")";
                        cellForName.setCellValue(badgeDesc);
                        setCellValue1(cellForPrice, supplies.getBadges().get(j).getPrice());
                        setCellValue1(cellForCount, supplies.getBadges().get(j).getCount());
                        cellForFormula.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                        break;
                    case 1: // Lanyards
                        String lanyardDesc = "Лента для бэйджа (" + supplies.getLanyards().get(j).getSize() + ", " +
                                supplies.getLanyards().get(j).getApplication().toLowerCase() + ", "
                                + supplies.getLanyards().get(j).getBracing().toLowerCase() + ")";
                        cellForName.setCellValue(lanyardDesc);
                        setCellValue1(cellForPrice, supplies.getLanyards().get(j).getPrice());
                        setCellValue1(cellForCount, supplies.getLanyards().get(j).getCount());
                        cellForFormula.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                        break;
                    case 2: // Stickers
                        String stickerDesk = "Печать персонифицированных наклеек " +
                                (supplies.getStickers().get(j).getTape() == null || supplies.getStickers().get(j).getTape().isBlank()
                                        ? supplies.getStickers().get(j).getStick()
                                        : supplies.getStickers().get(j).getTape()) + "(прозрачные)";
                        cellForName.setCellValue(stickerDesk);
                        setCellValue1(cellForPrice, supplies.getStickers().get(j).getPrice());
                        setCellValue1(cellForCount, supplies.getStickers().get(j).getCount());
                        cellForFormula.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                        break;
                    case 3: // Bracers
                        String color = supplies.getBracers().get(j).getColor();
                        String bracerDesc = "Браслет " + supplies.getBracers().get(j).getMaterial().toLowerCase() +
                                (color == null || color.isBlank() ? "" : color) +
                                " (" + supplies.getBracers().get(j).getLength() + "x" + supplies.getBracers().get(j).getWidth() +
                                ", " + supplies.getBracers().get(j).getApplication() + ")";
                        cellForName.setCellValue(bracerDesc);
                        setCellValue1(cellForPrice, supplies.getBracers().get(j).getPrice());
                        setCellValue1(cellForCount, supplies.getBracers().get(j).getCount());
                        cellForFormula.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                        break;
                    case 4: // Inserts
                        String density = supplies.getInserts().get(j).getDensity();
                        String insertDesc = "Вкладыш для пластикового кармана (" + supplies.getInserts().get(j).getSize()
                                + ", " + supplies.getInserts().get(j).getChroma() +
                                (density == null || density.isBlank() ? "" : density) + ")";
                        cellForName.setCellValue(insertDesc);
                        setCellValue1(cellForPrice, supplies.getInserts().get(j).getPrice());
                        setCellValue1(cellForCount, supplies.getInserts().get(j).getCount());
                        cellForFormula.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                        break;
                    case 5: // Pockets
                        String pocketDesc = "Карман пластиковый (" + supplies.getPockets().get(j).getSize() + ")";
                        cellForName.setCellValue(pocketDesc);
                        setCellValue1(cellForPrice, supplies.getPockets().get(j).getPrice());
                        setCellValue1(cellForCount, supplies.getPockets().get(j).getCount());
                        cellForFormula.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                        break;
                    case 6: // Ribbons
                        String ribbonDesc = "Расходные (печать на пластике " + supplies.getRibbons().get(j).getKind().toLowerCase() + ")";
                        cellForName.setCellValue(ribbonDesc);
                        setCellValue1(cellForPrice, supplies.getRibbons().get(j).getPrice());
                        setCellValue1(cellForCount, supplies.getRibbons().get(j).getCount());
                        cellForFormula.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                        break;
                    case 7: // Asups
                        cellForName.setCellValue(supplies.getAsups().get(j).getDescription());
                        setCellValue1(cellForPrice, supplies.getAsups().get(j).getPrice());
                        setCellValue1(cellForCount, supplies.getAsups().get(j).getCount());
                        cellForFormula.setCellFormula("E" + (currentRow + 1) + "*F" + (currentRow + 1));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown index in mass: " + i);
                }
                currentRow++;
            }
        }
    }

    private void setCellValue1(Cell cell, Integer value) {
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    public void updateFileWithStaff(InputHeader header, List<Staff> staffs, Mounting mounting, int periods) {

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

            CellStyle bottomBlockRowStyle = workbook.createCellStyle();
            bottomBlockRowStyle.setAlignment(HorizontalAlignment.CENTER);
            bottomBlockRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            bottomBlockRowStyle.setBorderBottom(BorderStyle.DOUBLE);
            bottomBlockRowStyle.setBorderLeft(BorderStyle.THIN);
            bottomBlockRowStyle.setBorderRight(BorderStyle.THIN);

            CellStyle bottomBlockRowStyle1 = workbook.createCellStyle();
            bottomBlockRowStyle1.setAlignment(HorizontalAlignment.LEFT);
            bottomBlockRowStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
            bottomBlockRowStyle1.setBorderBottom(BorderStyle.DOUBLE);
            bottomBlockRowStyle1.setBorderLeft(BorderStyle.THIN);
            bottomBlockRowStyle1.setBorderRight(BorderStyle.THIN);

            CellStyle bottomLastRowStyle = workbook.createCellStyle();
            bottomLastRowStyle.setAlignment(HorizontalAlignment.CENTER);
            bottomLastRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            bottomLastRowStyle.setBorderBottom(BorderStyle.THICK);
            bottomLastRowStyle.setBorderLeft(BorderStyle.THIN);
            bottomLastRowStyle.setBorderRight(BorderStyle.THIN);

            CellStyle bottomLastRowStyle1 = workbook.createCellStyle();
            bottomLastRowStyle1.setAlignment(HorizontalAlignment.LEFT);
            bottomLastRowStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
            bottomLastRowStyle1.setBorderBottom(BorderStyle.THICK);
            bottomLastRowStyle1.setBorderLeft(BorderStyle.THIN);
            bottomLastRowStyle1.setBorderRight(BorderStyle.THIN);

            writeStaff2(header, staffs, mounting, sheet, periods,
                    bottomBlockRowStyle, bottomBlockRowStyle1, bottomLastRowStyle, bottomLastRowStyle1);

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

    private void writeStaff2(InputHeader header, List<Staff> staffs, Mounting mounting, Sheet sheet, int periods,
                            CellStyle bottomBlockRowStyle, CellStyle bottomBlockRowStyle1,
                            CellStyle bottomLastRowStyle, CellStyle bottomLastRowStyle1) {

        int startRow = findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ");

        int[] neededRows = new int[6];//массив на 6 блоков
        int maxBlockCount = header.isSameEquipmentForAllDays() ? periods : header.getWorkDays();//понимаем на сколько сотрудников блок

        for (int i = 0; i < maxBlockCount; i++) {
            int startIndex = i * (header.isSameEquipmentForAllDays() ? 7 : 6);
            int endIndex = startIndex + (header.isSameEquipmentForAllDays() ? 7 : 6);
            neededRows[i] = countNeededRows(staffs, startIndex, endIndex);
        }

        int newStartRow = startRow + 1;
        boolean hasMontage = mounting.getQPrice() != 0 && mounting.getHours() != 0 && mounting.getQStaff() != 0;
        boolean hasStaff = Arrays.stream(neededRows).sum() > 0;

        if (hasMontage) {
            insertRowsWithShift(sheet, startRow + 1, 1);
            fillMountingRow(sheet.getRow(startRow + 1), mounting, bottomBlockRowStyle, bottomBlockRowStyle1,
                    bottomLastRowStyle, bottomLastRowStyle1, hasStaff);
            newStartRow = startRow + 2;
        }

        //отладка
        System.out.println(newStartRow);
        int formulaStartRow = newStartRow;

        if(hasStaff) {
            //валидация на шаблоне
            if (header.isSameEquipmentForAllDays() && periods == 1) {
                insertRowsWithShift(sheet, newStartRow, neededRows[0]);
                if (neededRows[0] > 1) {
                    mergeCells2(sheet, newStartRow, newStartRow + neededRows[0] - 1);
                }
                Row lastRowInBlock = sheet.getRow(newStartRow + neededRows[0] - 1);
                Row formulaRow = sheet.getRow(newStartRow + neededRows[0]);
                //сначала получаем стартовую строку, затем берем список стафф и проходим по индексам из neededRowsForFirstBlock
                for (int i = 0; i < neededRows[0]; i++) {
                    Row rowForStaff = sheet.getRow(newStartRow);
                    Cell cellForDates = rowForStaff.getCell(0);
                    cellForDates.setCellValue(dateFormatterForCell(header.getEventStartDate(), header.getEventEndDate()));
                    Cell cellForKind = rowForStaff.getCell(1);
                    cellForKind.setCellValue(staffs.get(i).getKindOfStaff());
                    Cell cellForWorkTime = rowForStaff.getCell(3);
                    cellForWorkTime.setCellValue(staffs.get(i).getStartTime().toString() + " - " + staffs.get(i).getEndTime().toString());
                    Cell cellForWorkHours = rowForStaff.getCell(4);
                    cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(i).getStartTime(), staffs.get(i).getEndTime()));
                    //возможен такой сценарий startTime = 23:00, а endTime = 02:00
                    Cell cellForStaffQuantity = rowForStaff.getCell(5);
                    cellForStaffQuantity.setCellValue(staffs.get(i).getStaffQuantity());
                    Cell cellForBetPerHour = rowForStaff.getCell(6);
                    cellForBetPerHour.setCellValue(staffs.get(i).getBetPerHour());
                    Cell cellForFormula = rowForStaff.getCell(7);
                    String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" + header.getWorkDays();
                    cellForFormula.setCellFormula(formula);
                    newStartRow++;
                }

                applyStyles(lastRowInBlock, bottomLastRowStyle, bottomLastRowStyle1);

                //отладка
                System.out.println(newStartRow);
                Cell formulaCell = formulaRow.getCell(7);
                formulaCell.setCellFormula("SUM(H" + (formulaStartRow) + ":H" + (formulaStartRow + neededRows[1]) + ")");

            } else if (header.isSameEquipmentForAllDays() && periods > 1) {
                if (periods == 4) {
                    insertRowsWithShift(sheet, newStartRow, neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3]);
                    if (neededRows[0] > 1) {
                        mergeCells2(sheet, newStartRow, newStartRow + neededRows[0] - 1);
                    }
                    if (neededRows[1] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0],
                                newStartRow + neededRows[0] + neededRows[1] - 1);
                    }
                    if (neededRows[2] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] - 1);
                    }
                    if (neededRows[3] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1] + neededRows[2],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] + neededRows[3] - 1);
                    }
                    Row lastRowInBlock1 = sheet.getRow(newStartRow + neededRows[0] - 1);
                    Row lastRowInBlock2 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] - 1);
                    Row lastRowInBlock3 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1]
                            + neededRows[2] - 1);
                    Row lastRowInBlock4 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1]
                            + neededRows[2] + neededRows[3] - 1);
                    Row formulaRow = sheet.getRow(newStartRow + neededRows[0] + neededRows[1]
                            + neededRows[2] + neededRows[3]);
                    for (int i = 0; i < neededRows[0]; i++) {
                        Row rowForStaff = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff.getCell(0);
                        cellForDates.setCellValue(staffs.get(i).getHeader().getBoolStart1().toString() + " - " +
                                staffs.get(i).getHeader().getBoolEnd1().toString());
                        Cell cellForKind = rowForStaff.getCell(1);
                        cellForKind.setCellValue(staffs.get(i).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(i).getStartTime().toString() + "-" + staffs.get(i).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(i).getStartTime(), staffs.get(i).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(i).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(i).getBetPerHour());
                        Cell cellForFormula = rowForStaff.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" +
                                (ChronoUnit.DAYS.between(staffs.get(i).getHeader().getBoolStart1(), staffs.get(i).getHeader().getBoolEnd1()) + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }

                    applyStyles(lastRowInBlock1, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 7; j < neededRows[1] + 7; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(staffs.get(j).getHeader().getBoolStart2().toString() + " - " +
                                staffs.get(j).getHeader().getBoolEnd2().toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" +
                                (ChronoUnit.DAYS.between(staffs.get(j).getHeader().getBoolStart2(), staffs.get(j).getHeader().getBoolEnd2()) + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock2, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 14; j < neededRows[2] + 14; j++) {
                        Row rowForStaff3 = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff3.getCell(0);
                        cellForDates.setCellValue(staffs.get(j).getHeader().getBoolStart3().toString() + " - " +
                                staffs.get(j).getHeader().getBoolEnd3().toString());
                        Cell cellForKind = rowForStaff3.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff3.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff3.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff3.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff3.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff3.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" +
                                (ChronoUnit.DAYS.between(staffs.get(j).getHeader().getBoolStart3(), staffs.get(j).getHeader().getBoolEnd3()) + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock3, bottomBlockRowStyle, bottomBlockRowStyle1);


                    for (int j = 21; j < neededRows[3] + 21; j++) {
                        Row rowForStaff3 = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff3.getCell(0);
                        cellForDates.setCellValue(staffs.get(j).getHeader().getBoolStart4().toString() + " - " +
                                staffs.get(j).getHeader().getBoolEnd4().toString());
                        Cell cellForKind = rowForStaff3.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff3.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff3.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff3.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff3.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff3.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" +
                                (ChronoUnit.DAYS.between(staffs.get(j).getHeader().getBoolStart4(), staffs.get(j).getHeader().getBoolEnd4()) + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock4, bottomLastRowStyle, bottomLastRowStyle1);

                    Cell formulaCell = formulaRow.getCell(7);
                    formulaCell.setCellFormula("SUM(H" + (formulaStartRow) + ":H" +
                            (formulaStartRow + neededRows[0] + neededRows[1] +
                                    neededRows[2] + neededRows[3]) + ")");

                }
                if (periods == 3) {

                    insertRowsWithShift(sheet, newStartRow, neededRows[0] + neededRows[1] +
                            neededRows[2]);
                    if (neededRows[0] > 1) {
                        mergeCells2(sheet, newStartRow, newStartRow + neededRows[0] - 1);
                    }
                    if (neededRows[1] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0],
                                newStartRow + neededRows[0] + neededRows[1] - 1);
                    }
                    if (neededRows[2] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] - 1);
                    }
                    Row lastRowInBlock1 = sheet.getRow(newStartRow + neededRows[0] - 1);
                    Row lastRowInBlock2 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] - 1);
                    Row lastRowInBlock3 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1]
                            + neededRows[2] - 1);
                    Row formulaRow = sheet.getRow(newStartRow + neededRows[0] + neededRows[1]
                            + neededRows[2]);
                    for (int i = 0; i < neededRows[0]; i++) {
                        Row rowForStaff = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff.getCell(0);
                        cellForDates.setCellValue(staffs.get(i).getHeader().getBoolStart1().toString() + " - " +
                                staffs.get(i).getHeader().getBoolEnd1().toString());
                        Cell cellForKind = rowForStaff.getCell(1);
                        cellForKind.setCellValue(staffs.get(i).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(i).getStartTime().toString() + "-" + staffs.get(i).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(i).getStartTime(), staffs.get(i).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(i).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(i).getBetPerHour());
                        Cell cellForFormula = rowForStaff.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" +
                                (ChronoUnit.DAYS.between(staffs.get(i).getHeader().getBoolStart1(), staffs.get(i).getHeader().getBoolEnd1()) + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock1, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 7; j < neededRows[1] + 7; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(staffs.get(j).getHeader().getBoolStart2().toString() + " - " +
                                staffs.get(j).getHeader().getBoolEnd2().toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" +
                                (ChronoUnit.DAYS.between(staffs.get(j).getHeader().getBoolStart2(), staffs.get(j).getHeader().getBoolEnd2()) + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }

                    applyStyles(lastRowInBlock2, bottomBlockRowStyle, bottomBlockRowStyle1);


                    for (int j = 14; j < neededRows[2] + 14; j++) {
                        Row rowForStaff3 = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff3.getCell(0);
                        if (staffs.get(j).getHeader().getBoolStart2() != null && staffs.get(j).getHeader().getBoolEnd3() != null) {
                            cellForDates.setCellValue(staffs.get(j).getHeader().getBoolStart3().toString() + " - " +
                                    staffs.get(j).getHeader().getBoolEnd2().toString());
                        } else {
                            cellForDates.setCellValue(staffs.get(j).getHeader().getBoolStart4().toString() + " - " +
                                    staffs.get(j).getHeader().getBoolEnd4().toString());
                        }
                        Cell cellForKind = rowForStaff3.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff3.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff3.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff3.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff3.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff3.getCell(7);
                        String formula;
                        if (staffs.get(j).getHeader().getBoolStart3() != null && staffs.get(j).getHeader().getBoolEnd3() != null) {
                            formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" +
                                    (ChronoUnit.DAYS.between(staffs.get(j).getHeader().getBoolStart3(), staffs.get(j).getHeader().getBoolEnd3()) + 1);
                        } else {
                            formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" +
                                    (ChronoUnit.DAYS.between(staffs.get(j).getHeader().getBoolStart4(), staffs.get(j).getHeader().getBoolEnd4()) + 1);
                        }
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }

                    applyStyles(lastRowInBlock3, bottomLastRowStyle, bottomLastRowStyle1);

                    Cell formulaCell = formulaRow.getCell(7);
                    formulaCell.setCellFormula("SUM(H" + (formulaStartRow) + ":H" +
                            (formulaStartRow + neededRows[0] + neededRows[1] + neededRows[2]) + ")");
                }
                if (periods == 2) {

                    insertRowsWithShift(sheet, newStartRow, neededRows[0] + neededRows[1]);
                    if (neededRows[0] > 1) {
                        mergeCells2(sheet, newStartRow, newStartRow + neededRows[0] - 1);
                    }

                    if (neededRows[1] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0],
                                newStartRow + neededRows[0] + neededRows[1] - 1);
                    }
                    Row lastRowInBlock1 = sheet.getRow(newStartRow + neededRows[0] - 1);
                    Row lastRowInBlock2 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] - 1);
                    Row formulaRow = sheet.getRow(newStartRow + neededRows[0] + neededRows[1]);
                    for (int i = 0; i < neededRows[0]; i++) {
                        Row rowForStaff = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff.getCell(0);
                        cellForDates.setCellValue(staffs.get(i).getHeader().getBoolStart1().toString() + " - " +
                                staffs.get(i).getHeader().getBoolEnd1().toString());
                        Cell cellForKind = rowForStaff.getCell(1);
                        cellForKind.setCellValue(staffs.get(i).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(i).getStartTime().toString() + "-" + staffs.get(i).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(i).getStartTime(), staffs.get(i).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(i).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(i).getBetPerHour());
                        Cell cellForFormula = rowForStaff.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" +
                                (ChronoUnit.DAYS.between(staffs.get(i).getHeader().getBoolStart1(), staffs.get(i).getHeader().getBoolEnd1()) + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;

                    }

                    applyStyles(lastRowInBlock1, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 7; j < neededRows[1] + 7; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        if (staffs.get(j).getHeader().getBoolStart2() != null && staffs.get(j).getHeader().getBoolEnd2() != null) {
                            cellForDates.setCellValue(staffs.get(j).getHeader().getBoolStart2().toString() + " - " +
                                    staffs.get(j).getHeader().getBoolEnd2().toString());
                        } else {
                            cellForDates.setCellValue(staffs.get(j).getHeader().getBoolStart4().toString() + " - " +
                                    staffs.get(j).getHeader().getBoolEnd4().toString());
                        }

                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula;
                        if (staffs.get(j).getHeader().getBoolStart2() != null && staffs.get(j).getHeader().getBoolEnd2() != null) {
                            formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" +
                                    (ChronoUnit.DAYS.between(staffs.get(j).getHeader().getBoolStart2(), staffs.get(j).getHeader().getBoolEnd2()) + 1);
                        } else {
                            formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1) + "*" +
                                    (ChronoUnit.DAYS.between(staffs.get(j).getHeader().getBoolStart4(), staffs.get(j).getHeader().getBoolEnd4()) + 1);
                        }
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                        //Формат границ
                    }

                    applyStyles(lastRowInBlock2, bottomLastRowStyle, bottomLastRowStyle1);

                    Cell formulaCell = formulaRow.getCell(7);
                    formulaCell.setCellFormula("SUM(H" + (formulaStartRow) + ":H" +
                            (formulaStartRow + neededRows[0] + neededRows[1]) + ")");
                }

            } else {
                if (header.getWorkDays() == 6) {
                    insertRowsWithShift(sheet, newStartRow, neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3] + neededRows[4] + neededRows[5]);
                    if (neededRows[0] > 1) {
                        mergeCells2(sheet, newStartRow, newStartRow + neededRows[0] - 1);
                    }
                    if (neededRows[1] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0],
                                newStartRow + neededRows[0] + neededRows[1] - 1);
                    }
                    if (neededRows[2] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] - 1);
                    }
                    if (neededRows[3] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1] + neededRows[2],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] + neededRows[3] - 1);
                    }
                    if (neededRows[4] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1] + neededRows[2] + neededRows[3],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] + neededRows[3] + neededRows[4] - 1);
                    }
                    if (neededRows[5] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1] + neededRows[2] + neededRows[3] + neededRows[4],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] + neededRows[3] + neededRows[4] + neededRows[5] - 1);
                    }

                    Row lastRowInBlock1 = sheet.getRow(newStartRow + neededRows[0] - 1);
                    Row lastRowInBlock2 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] - 1);
                    Row lastRowInBlock3 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] - 1);
                    Row lastRowInBlock4 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3] - 1);
                    Row lastRowInBlock5 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3] + neededRows[4] - 1);
                    Row lastRowInBlock6 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3] + neededRows[4] + neededRows[5] - 1);
                    Row formulaRow = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3] + neededRows[4] + neededRows[5]);

                    for (int i = 0; i < neededRows[0]; i++) {
                        Row rowForStaff = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().toString());//Дата первого дня
                        Cell cellForKind = rowForStaff.getCell(1);
                        cellForKind.setCellValue(staffs.get(i).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(i).getStartTime().toString() + "-" + staffs.get(i).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(i).getStartTime(), staffs.get(i).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(i).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(i).getBetPerHour());
                        Cell cellForFormula = rowForStaff.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;

                    }
                    applyStyles(lastRowInBlock1, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 6; j < neededRows[1] + 6; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(1).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock2, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 12; j < neededRows[2] + 12; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(2).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock3, bottomBlockRowStyle, bottomBlockRowStyle1);


                    for (int j = 18; j < neededRows[3] + 18; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(3).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock4, bottomBlockRowStyle, bottomBlockRowStyle1);


                    for (int j = 24; j < neededRows[4] + 24; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(4).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock5, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 30; j < neededRows[5] + 30; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(5).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock6, bottomLastRowStyle, bottomLastRowStyle1);

                    Cell formulaCell = formulaRow.getCell(7);
                    formulaCell.setCellFormula("SUM(H" + (formulaStartRow) + ":H" +
                            (formulaStartRow + neededRows[0] + neededRows[1] + neededRows[2] +
                                    neededRows[3] + neededRows[4] + neededRows[5]) + ")");


                }
                if (header.getWorkDays() == 5) {
                    insertRowsWithShift(sheet, newStartRow, neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3] + neededRows[4]);
                    if (neededRows[0] > 1) {
                        mergeCells2(sheet, newStartRow, newStartRow + neededRows[0] - 1);
                    }
                    if (neededRows[1] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0],
                                newStartRow + neededRows[0] + neededRows[1] - 1);
                    }
                    if (neededRows[2] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] - 1);
                    }
                    if (neededRows[3] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1] + neededRows[2],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] + neededRows[3] - 1);
                    }
                    if (neededRows[4] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1] + neededRows[2] + neededRows[3],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] + neededRows[3] + neededRows[4] - 1);
                    }

                    Row lastRowInBlock1 = sheet.getRow(newStartRow + neededRows[0] - 1);
                    Row lastRowInBlock2 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] - 1);
                    Row lastRowInBlock3 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] - 1);
                    Row lastRowInBlock4 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3] - 1);
                    Row lastRowInBlock5 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3] + neededRows[4] - 1);
                    Row formulaRow = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3] + neededRows[4]);

                    for (int i = 0; i < neededRows[0]; i++) {
                        Row rowForStaff = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().toString());//Дата первого дня
                        Cell cellForKind = rowForStaff.getCell(1);
                        cellForKind.setCellValue(staffs.get(i).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(i).getStartTime().toString() + "-" + staffs.get(i).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(i).getStartTime(), staffs.get(i).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(i).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(i).getBetPerHour());
                        Cell cellForFormula = rowForStaff.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;

                    }
                    applyStyles(lastRowInBlock1, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 6; j < neededRows[1] + 6; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(1).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock2, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 12; j < neededRows[2] + 12; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(2).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock3, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 18; j < neededRows[3] + 18; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(3).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock4, bottomBlockRowStyle, bottomBlockRowStyle1);


                    for (int j = 24; j < neededRows[4] + 24; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(4).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock5, bottomLastRowStyle, bottomLastRowStyle1);

                    Cell formulaCell = formulaRow.getCell(7);
                    formulaCell.setCellFormula("SUM(H" + (formulaStartRow) + ":H" +
                            (formulaStartRow + neededRows[0] + neededRows[1] + neededRows[2] +
                                    neededRows[3] + neededRows[4]) + ")");

                }
                if (header.getWorkDays() == 4) {
                    insertRowsWithShift(sheet, newStartRow, neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3]);
                    if (neededRows[0] > 1) {
                        mergeCells2(sheet, newStartRow, newStartRow + neededRows[0] - 1);
                    }
                    if (neededRows[1] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0],
                                newStartRow + neededRows[0] + neededRows[1] - 1);
                    }
                    if (neededRows[2] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] - 1);
                    }
                    if (neededRows[3] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1] + neededRows[2],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] + neededRows[3] - 1);
                    }
                    Row lastRowInBlock1 = sheet.getRow(newStartRow + neededRows[0] - 1);
                    Row lastRowInBlock2 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] - 1);
                    Row lastRowInBlock3 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] - 1);
                    Row lastRowInBlock4 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3] - 1);
                    Row formulaRow = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] + neededRows[3]);
                    for (int i = 0; i < neededRows[0]; i++) {
                        Row rowForStaff = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().toString());//Дата первого дня
                        Cell cellForKind = rowForStaff.getCell(1);
                        cellForKind.setCellValue(staffs.get(i).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(i).getStartTime().toString() + "-" + staffs.get(i).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(i).getStartTime(), staffs.get(i).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(i).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(i).getBetPerHour());
                        Cell cellForFormula = rowForStaff.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;

                    }
                    applyStyles(lastRowInBlock1, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 6; j < neededRows[1] + 6; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(1).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock2, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 12; j < neededRows[2] + 12; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(2).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock3, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 18; j < neededRows[3] + 18; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(3).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock4, bottomLastRowStyle, bottomLastRowStyle1);

                    Cell formulaCell = formulaRow.getCell(7);
                    formulaCell.setCellFormula("SUM(H" + (formulaStartRow) + ":H" +
                            (formulaStartRow + neededRows[0] + neededRows[1] + neededRows[2] +
                                    neededRows[3]) + ")");
                }
                if (header.getWorkDays() == 3) {
                    insertRowsWithShift(sheet, newStartRow, neededRows[0] + neededRows[1] +
                            neededRows[2]);
                    if (neededRows[0] > 1) {
                        mergeCells2(sheet, newStartRow, newStartRow + neededRows[0] - 1);
                    }
                    if (neededRows[1] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0],
                                newStartRow + neededRows[0] + neededRows[1] - 1);
                    }
                    if (neededRows[2] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0] + neededRows[1],
                                newStartRow + neededRows[0] + neededRows[1] + neededRows[2] - 1);
                    }
                    Row lastRowInBlock1 = sheet.getRow(newStartRow + neededRows[0] - 1);
                    Row lastRowInBlock2 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] - 1);
                    Row lastRowInBlock3 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2] - 1);
                    Row formulaRow = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] +
                            neededRows[2]);
                    for (int i = 0; i < neededRows[0]; i++) {
                        Row rowForStaff = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().toString());//Дата первого дня
                        Cell cellForKind = rowForStaff.getCell(1);
                        cellForKind.setCellValue(staffs.get(i).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(i).getStartTime().toString() + "-" + staffs.get(i).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(i).getStartTime(), staffs.get(i).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(i).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(i).getBetPerHour());
                        Cell cellForFormula = rowForStaff.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;

                    }
                    applyStyles(lastRowInBlock1, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 6; j < neededRows[1] + 6; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(1).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock2, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 12; j < neededRows[2] + 12; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(2).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock1, bottomLastRowStyle, bottomLastRowStyle1);

                    Cell formulaCell = formulaRow.getCell(7);
                    formulaCell.setCellFormula("SUM(H" + (formulaStartRow) + ":H" +
                            (formulaStartRow + neededRows[0] + neededRows[1] + neededRows[2]) + ")");
                }
                if (header.getWorkDays() == 2) {
                    insertRowsWithShift(sheet, newStartRow, neededRows[0] + neededRows[1]);
                    if (neededRows[0] > 1) {
                        mergeCells2(sheet, newStartRow, newStartRow + neededRows[0] - 1);
                    }
                    if (neededRows[1] > 1) {
                        mergeCells(sheet, newStartRow + neededRows[0],
                                newStartRow + neededRows[0] + neededRows[1] - 1);
                    }

                    Row lastRowInBlock1 = sheet.getRow(newStartRow + neededRows[0] - 1);
                    Row lastRowInBlock2 = sheet.getRow(newStartRow + neededRows[0] + neededRows[1] - 1);
                    Row formulaRow = sheet.getRow(newStartRow + neededRows[0] + neededRows[1]);

                    for (int i = 0; i < neededRows[0]; i++) {
                        Row rowForStaff = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().toString());//Дата первого дня
                        Cell cellForKind = rowForStaff.getCell(1);
                        cellForKind.setCellValue(staffs.get(i).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(i).getStartTime().toString() + "-" + staffs.get(i).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(i).getStartTime(), staffs.get(i).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(i).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(i).getBetPerHour());
                        Cell cellForFormula = rowForStaff.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;

                    }
                    applyStyles(lastRowInBlock1, bottomBlockRowStyle, bottomBlockRowStyle1);

                    for (int j = 6; j < neededRows[1] + 6; j++) {
                        Row rowForStaff2 = sheet.getRow(newStartRow);

                        Cell cellForDates = rowForStaff2.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().plusDays(1).toString());
                        Cell cellForKind = rowForStaff2.getCell(1);
                        cellForKind.setCellValue(staffs.get(j).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff2.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff2.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(j).getStartTime(), staffs.get(j).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff2.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff2.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(j).getBetPerHour());
                        Cell cellForFormula = rowForStaff2.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;
                    }
                    applyStyles(lastRowInBlock2, bottomLastRowStyle, bottomLastRowStyle1);

                    Cell formulaCell = formulaRow.getCell(7);
                    formulaCell.setCellFormula("SUM(H" + (formulaStartRow) + ":H" +
                            (formulaStartRow + neededRows[0] + neededRows[1]) + ")");


                }
                if (header.getWorkDays() == 1) {
                    insertRowsWithShift(sheet, newStartRow, neededRows[0]);
                    if (neededRows[0] > 1) {
                        mergeCells2(sheet, newStartRow, newStartRow + neededRows[0] - 1);
                    }
                    Row lastRowInBlock1 = sheet.getRow(newStartRow + neededRows[0] - 1);
                    Row formulaRow = sheet.getRow(newStartRow + neededRows[0]);

                    for (int i = 0; i < neededRows[0]; i++) {
                        Row rowForStaff = sheet.getRow(newStartRow);
                        Cell cellForDates = rowForStaff.getCell(0);
                        cellForDates.setCellValue(header.getEventStartDate().toString());//Дата первого дня
                        Cell cellForKind = rowForStaff.getCell(1);
                        cellForKind.setCellValue(staffs.get(i).getKindOfStaff());
                        Cell cellForWorkTime = rowForStaff.getCell(3);
                        cellForWorkTime.setCellValue(staffs.get(i).getStartTime().toString() + "-" + staffs.get(i).getEndTime().toString());
                        Cell cellForWorkHours = rowForStaff.getCell(4);
                        cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(i).getStartTime(), staffs.get(i).getEndTime()));
                        //возможен такой сценарий startTime = 23:00, а endTime = 02:00 - хотя у меня на входе есть валидация - откладываем
                        Cell cellForStaffQuantity = rowForStaff.getCell(5);
                        cellForStaffQuantity.setCellValue(staffs.get(i).getStaffQuantity());
                        Cell cellForBetPerHour = rowForStaff.getCell(6);
                        cellForBetPerHour.setCellValue(staffs.get(i).getBetPerHour());
                        Cell cellForFormula = rowForStaff.getCell(7);
                        String formula = "E" + (newStartRow + 1) + "*F" + (newStartRow + 1) + "*G" + (newStartRow + 1);
                        cellForFormula.setCellFormula(formula);
                        newStartRow++;

                    }
                    applyStyles(lastRowInBlock1, bottomLastRowStyle, bottomLastRowStyle1);

                    Cell formulaCell = formulaRow.getCell(7);
                    formulaCell.setCellFormula("SUM(H" + (formulaStartRow) + ":H" +
                            (formulaStartRow + neededRows[0]) + ")");

                }
            }
        }

        handleEmptyBlocks(sheet, startRow, hasMontage, hasStaff);
    }

//    private void writeStaff(InputHeader header, List<Staff> staffs, Mounting mounting, Sheet sheet, int periods,
//                            CellStyle bottomBlockRowStyle, CellStyle bottomBlockRowStyle1,
//                            CellStyle bottomLastRowStyle, CellStyle bottomLastRowStyle1) {
//
//        int startRow = findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ");
//
//        int[] neededRows = new int[6];//массив на 6 блоков
//        int maxBlockCount = header.isSameEquipmentForAllDays() ? periods : header.getWorkDays();//понимаем на сколько сотрудников блок
//
//        for (int i = 0; i < maxBlockCount; i++) {
//            int startIndex = i * (header.isSameEquipmentForAllDays() ? 7 : 6);
//            int endIndex = startIndex + (header.isSameEquipmentForAllDays() ? 7 : 6);
//            neededRows[i] = countNeededRows(staffs, startIndex, endIndex);
//        }
//
//        int newStartRow = startRow + 1;
//        boolean hasMontage = mounting.getQPrice() != 0 && mounting.getHours() != 0 && mounting.getQStaff() != 0;
//        boolean hasStaff = Arrays.stream(neededRows).sum() > 0;
//
//        if (hasMontage) {
//            insertRowsWithShift(sheet, startRow + 1, 1);
//            fillMountingRow(sheet.getRow(startRow + 1), mounting, bottomBlockRowStyle, bottomBlockRowStyle1);
//            newStartRow = startRow + 2;
//        }
//
//        if (hasStaff) {
//            processStaffBlocks(header, staffs, sheet, neededRows, newStartRow, maxBlockCount,
//                    bottomBlockRowStyle, bottomBlockRowStyle1, bottomLastRowStyle, bottomLastRowStyle1);
//        }
//
//        handleEmptyBlocks(sheet, startRow, hasMontage, hasStaff);
//    }

    private void handleEmptyBlocks(Sheet sheet, int startRow, boolean hasMontage, boolean hasStaff) {
        if (!hasMontage && !hasStaff) {
            int rowForDelete = findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ");
            removeMergedRegions(sheet, rowForDelete + 1);
            removeRow(sheet, rowForDelete + 1);
            removeMergedRegions(sheet, rowForDelete);
            removeRow(sheet, rowForDelete);
        } else if (hasMontage && !hasStaff) {
            // Логика обработки случая, когда есть монтаж, но нет стаффов
            // Если требуется, можно выполнить другие действия здесь, например, очистить или объединить ячейки
        } else if (!hasMontage && hasStaff) {
            // Логика обработки случая, когда есть стаффы, но нет монтажа
            // Если требуется, можно выполнить другие действия здесь, например, очистить или объединить ячейки
        }
    }

    private void fillMountingRow(Row mountingRow, Mounting mounting, CellStyle bottomBlockRowStyle, CellStyle bottomBlockRowStyle1,
                                 CellStyle bottomLastRowStyle, CellStyle bottomLastRowStyle1, boolean hasStaff) {
        Cell descriptionCell = mountingRow.getCell(1);
        descriptionCell.setCellValue("Услуга монтаж/демонтаж + настройка оборудования");

        Cell timeCell = mountingRow.getCell(3);
        timeCell.setCellValue("-");

        Cell cellQHours = mountingRow.getCell(4);
        cellQHours.setCellValue(mounting.getHours());

        Cell cellQStaff = mountingRow.getCell(5);
        cellQStaff.setCellValue(mounting.getQStaff());

        Cell cellQPrice = mountingRow.getCell(6);
        cellQPrice.setCellValue(mounting.getQPrice());

        Cell cellForBlank = mountingRow.getCell(0);
        cellForBlank.setCellType(CellType.BLANK);

        Cell cellForFormula = mountingRow.getCell(7);
        String formula = "E" + (mountingRow.getRowNum() + 1) + "*F" + (mountingRow.getRowNum() + 1) + "*G" + (mountingRow.getRowNum() + 1);
        cellForFormula.setCellFormula(formula);

        if(hasStaff) {
            applyStyles(mountingRow, bottomBlockRowStyle, bottomBlockRowStyle1);
        } else {
            applyStyles(mountingRow, bottomLastRowStyle, bottomLastRowStyle1);
        }
    }

//    private void processStaffBlocks(InputHeader header, List<Staff> staffs, Sheet sheet, int[] neededRows, int startRow, int maxBlockCount,
//                                    CellStyle bottomBlockRowStyle, CellStyle bottomBlockRowStyle1,
//                                    CellStyle bottomLastRowStyle, CellStyle bottomLastRowStyle1) {
//
//        int currentRow = startRow;
//        int formulaStartRow = currentRow;
//
//        for (int block = 0; block < maxBlockCount; block++) {
//            if (neededRows[block] > 0) {
//                insertRowsWithShift(sheet, currentRow, neededRows[block]);
//                if (neededRows[block] > 1) {
//                    mergeCells(sheet, currentRow, currentRow + neededRows[block] - 1);
//                }
//
//                fillStaffRows(header, staffs, sheet, currentRow, block, neededRows[block]);
//
//                currentRow += neededRows[block];
//                applyStyles(sheet, currentRow - 1, bottomBlockRowStyle, bottomBlockRowStyle1, bottomLastRowStyle, bottomLastRowStyle1, block == maxBlockCount - 1);
//            }
//        }
//
//        Row formulaRow = sheet.getRow(currentRow);
//        Cell formulaCell = formulaRow.getCell(7);
//        formulaCell.setCellFormula("SUM(H" + formulaStartRow + ":H" + currentRow + ")");
//    }

//    private void fillStaffRows(InputHeader header, List<Staff> staffs, Sheet sheet, int startRow, int block, int rowsCount) {
//        for (int i = 0; i < rowsCount; i++) {
//            Row rowForStaff = sheet.getRow(startRow + i);
//            Cell cellForDates = rowForStaff.getCell(0);
//            cellForDates.setCellValue(dateFormatterForCell(header.getEventStartDate(), header.getEventEndDate()));
//
//            Cell cellForKind = rowForStaff.getCell(1);
//            cellForKind.setCellValue(staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getKindOfStaff());
//
//            Cell cellForWorkTime = rowForStaff.getCell(3);
//            cellForWorkTime.setCellValue(staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getStartTime().toString()
//                    + " - " + staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getEndTime().toString());
//
//            Cell cellForWorkHours = rowForStaff.getCell(4);
//            cellForWorkHours.setCellValue(ChronoUnit.HOURS.between(staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getStartTime(),
//                    staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getEndTime()));
//
//            Cell cellForStaffQuantity = rowForStaff.getCell(5);
//            cellForStaffQuantity.setCellValue(staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getStaffQuantity());
//
//            Cell cellForBetPerHour = rowForStaff.getCell(6);
//            cellForBetPerHour.setCellValue(staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getBetPerHour());
//
//            Cell cellForFormula = rowForStaff.getCell(7);
//            String formula;
//            if (staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolStart1() != null
//                    && staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolEnd1() != null) {
//                formula = "E" + (startRow + i + 1) + "*F" + (startRow + i + 1) + "*G" + (startRow + i + 1) + "*" +
//                        (ChronoUnit.DAYS.between(staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolStart1(),
//                                staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolEnd1()) + 1);
//            } else if (staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolStart2() != null
//                    && staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolEnd2() != null) {
//                formula = "E" + (startRow + i + 1) + "*F" + (startRow + i + 1) + "*G" + (startRow + i + 1) + "*" +
//                        (ChronoUnit.DAYS.between(staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolStart2(),
//                                staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolEnd2()) + 1);
//            } else if (staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolStart3() != null
//                    && staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolEnd3() != null) {
//                formula = "E" + (startRow + i + 1) + "*F" + (startRow + i + 1) + "*G" + (startRow + i + 1) + "*" +
//                        (ChronoUnit.DAYS.between(staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolStart3(),
//                                staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolEnd3()) + 1);
//            } else if (staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolStart4() != null
//                    && staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolEnd4() != null) {
//                formula = "E" + (startRow + i + 1) + "*F" + (startRow + i + 1) + "*G" + (startRow + i + 1) + "*" +
//                        (ChronoUnit.DAYS.between(staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolStart4(),
//                                staffs.get(block * (header.isSameEquipmentForAllDays() ? 7 : 6) + i).getHeader().getBoolEnd4()) + 1);
//            } else {
//                // Если даты не указаны, можно задать значение по умолчанию или оставить ячейку пустой
//                formula = "E" + (startRow + i + 1) + "*F" + (startRow + i + 1) + "*G" + (startRow + i + 1);
//            }
//        }
//    }

    private void applyStyles(Row row, CellStyle cellStyle, CellStyle cellStyle1) {
        for (int j = 0; j < 8; j++) {
            if (j == 1) {
                row.getCell(j).setCellStyle(cellStyle1);
            } else {
                row.getCell(j).setCellStyle(cellStyle);
            }
        }
    }



    private int countNeededRows(List<Staff> staffs, int start, int end) {
        int neededRows = 0;
        // Убедитесь, что 'end' не превышает размер списка
        end = Math.min(end, staffs.size());
        for (int i = start; i < end; i++) {
            if (!staffs.get(i).getKindOfStaff().isEmpty()) {
                neededRows++;
            }
        }
        return neededRows;
    }

    private void mergeCells2(Sheet sheet, int firstRow, int lastRow) {
        // Удаление пересекающихся объединений
        for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() >= firstRow && mergedRegion.getLastRow() <= lastRow && mergedRegion.getFirstColumn() == 0) {
                sheet.removeMergedRegion(i);
            }
        }

        // Объединение ячеек
        if (firstRow != lastRow) { // Если только одна строка, объединять не нужно
            CellRangeAddress cellRangeAddress = new CellRangeAddress(firstRow, lastRow, 0, 0);
            sheet.addMergedRegion(cellRangeAddress);
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

            CellStyle newCellStyle = newRow.getSheet().getWorkbook().createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
            newCellStyle.setFillPattern(FillPatternType.NO_FILL);
            Font newFont = newRow.getSheet().getWorkbook().createFont();
            newFont.setFontName("Arial");
            newFont.setFontHeightInPoints((short) 10);
            newFont.setColor(IndexedColors.BLACK.getIndex());
            newFont.setBold(false);
            newCellStyle.setFont(newFont);
            newCellStyle.setBorderTop(BorderStyle.THIN);
            newCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            newCellStyle.setBorderBottom(BorderStyle.THIN);
            newCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            newCellStyle.setBorderRight(BorderStyle.THIN);
            newCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
            newCellStyle.setBorderLeft(BorderStyle.THIN);
            newCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            newCell.setCellStyle(newCellStyle);

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

    private void removeRow(Sheet sheet, int rowIndex) {
        int lastRowNum = sheet.getLastRowNum();
        if (rowIndex >= 0 && rowIndex < lastRowNum) {
            sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
        }
        if (rowIndex == lastRowNum) {
            Row removingRow = sheet.getRow(rowIndex);
            if (removingRow != null) {
                sheet.removeRow(removingRow);
            }
        }
    }

    private void removeMergedRegions(Sheet sheet, int rowIndex) {
        for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() <= rowIndex && mergedRegion.getLastRow() >= rowIndex) {
                sheet.removeMergedRegion(i);
            }
        }
    }

    public void updateFileWithLogistics(InputHeader header, List<Logistic> logistics) {

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

            CellStyle bottomBlockRowStyle = workbook.createCellStyle();
            bottomBlockRowStyle.setAlignment(HorizontalAlignment.CENTER);
            bottomBlockRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            bottomBlockRowStyle.setBorderBottom(BorderStyle.DOUBLE);
            bottomBlockRowStyle.setBorderLeft(BorderStyle.THIN);
            bottomBlockRowStyle.setBorderRight(BorderStyle.THIN);

            CellStyle bottomBlockRowStyle1 = workbook.createCellStyle();
            bottomBlockRowStyle1.setAlignment(HorizontalAlignment.LEFT);
            bottomBlockRowStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
            bottomBlockRowStyle1.setBorderBottom(BorderStyle.DOUBLE);
            bottomBlockRowStyle1.setBorderLeft(BorderStyle.THIN);
            bottomBlockRowStyle1.setBorderRight(BorderStyle.THIN);

            CellStyle bottomLastRowStyle = workbook.createCellStyle();
            bottomLastRowStyle.setAlignment(HorizontalAlignment.CENTER);
            bottomLastRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            bottomLastRowStyle.setBorderBottom(BorderStyle.THICK);
            bottomLastRowStyle.setBorderLeft(BorderStyle.THIN);
            bottomLastRowStyle.setBorderRight(BorderStyle.THIN);

            CellStyle bottomLastRowStyle1 = workbook.createCellStyle();
            bottomLastRowStyle1.setAlignment(HorizontalAlignment.LEFT);
            bottomLastRowStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
            bottomLastRowStyle1.setBorderBottom(BorderStyle.THICK);
            bottomLastRowStyle1.setBorderLeft(BorderStyle.THIN);
            bottomLastRowStyle1.setBorderRight(BorderStyle.THIN);

            writeLogistic(header, logistics, sheet, bottomBlockRowStyle, bottomBlockRowStyle1, bottomLastRowStyle, bottomLastRowStyle1);
            if (formulaMap.containsKey(fileName)) {
                List<Integer> indices = formulaMap.get(fileName);
                // Проверяем, не пустой ли список
                if (indices != null && !indices.isEmpty()) {
                    System.out.println("Indices for file " + fileName + ":");
                    for (Integer index : indices) {
                        System.out.println(index);
                    }
                }
            }
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

    public void writeLogistic(InputHeader header, List<Logistic> logistics, Sheet sheet, CellStyle bottomBlockRowStyle,
                              CellStyle bottomBlockRowStyle1, CellStyle bottomLastRowStyle, CellStyle bottomLastRowStyle1){
        //считаем logistics

        int startRow = findFirstRow(sheet, "ЛОГИСТИКА") + 1;
        int neededRowsForLogistics = 0;
        for(int i = 0; i < logistics.size(); i++){
            if(logistics.get(i).getCount() != 0 && logistics.get(i).getPrice() != 0
                    && logistics.get(i).getDescription() != null && (!logistics.get(i).getDescription().isEmpty())
            && (!logistics.get(i).getDescription().isBlank())) {
                neededRowsForLogistics++;
            }
        } //допустим у нас 3 логистики
        if(neededRowsForLogistics != 0){
            insertRowsWithShift(sheet, startRow, neededRowsForLogistics);
            if (neededRowsForLogistics > 1) {
                mergeCells2(sheet, startRow, startRow + neededRowsForLogistics - 1);
            }
            Row lastRowInBlock = sheet.getRow(startRow + neededRowsForLogistics - 1);
            Cell cellForBlank = lastRowInBlock.getCell(0);
            cellForBlank.setCellType(CellType.BLANK);
            Row formulaRow = sheet.getRow(startRow + neededRowsForLogistics);
            int formulaStartRow = startRow;
            for (int i = 0; i < neededRowsForLogistics; i++) {
                Row rowForLogistic = sheet.getRow(startRow);
                Cell cellForDates = rowForLogistic.getCell(0);
                cellForDates.setCellType(CellType.BLANK);
                Cell cellForDescription = rowForLogistic.getCell(1);
                cellForDescription.setCellValue(logistics.get(i).getDescription());
                Cell cellForCount = rowForLogistic.getCell(4);
                cellForCount.setCellValue(logistics.get(i).getCount());
                Cell cellForPrice = rowForLogistic.getCell(5);
                cellForPrice.setCellValue(logistics.get(i).getPrice());
                Cell cellForFormula = rowForLogistic.getCell(7);
                String formula = "E" + (startRow + 1) + "*F" + (startRow + 1);
                cellForFormula.setCellFormula(formula);
                startRow++;
            }

            for (int j = 0; j < 8; j++) {
                if (j == 1) {
                    lastRowInBlock.getCell(j).setCellStyle(bottomLastRowStyle1); // Применяем другой стиль для j = 1
                } else {
                    lastRowInBlock.getCell(j).setCellStyle(bottomLastRowStyle); // Применяем основной стиль для остальных ячеек
                }
            }

            Cell formulaCell = formulaRow.getCell(7);
            formulaCell.setCellFormula("SUM(H" + (formulaStartRow + 1) + ":H" +
                    (formulaStartRow + neededRowsForLogistics) + ")");

            int formulaRowIndex = formulaRow.getRowNum();
            String fileName = dateFormatterForFileName(header.getEventStartDate(), header.getEventEndDate())
                    + " " + header.getCustomer() + " СМЕТА РЕГИСТРАЦИИ Ver1";
            //отладка
            System.out.println("Вот" + formulaRowIndex);
            if (formulaMap.containsKey(fileName)) {
                formulaMap.get(fileName).add(formulaRowIndex);
            } else {
                // Логирование ошибки, если файл не был инициализирован
                log.error("File {} not found in formulaMap", fileName);
            }
            //Собираем финальную формулу


        } else {
            int rowForDelete = findFirstRow(sheet, "ЛОГИСТИКА");
            removeMergedRegions(sheet, rowForDelete + 1);
            removeRow(sheet, rowForDelete + 1);
            removeMergedRegions(sheet, rowForDelete);
            removeRow(sheet, rowForDelete);
        }

    }



}

//    int formulaRowIndex = formulaRow.getRowNum();
//    String fileName = dateFormatterForFileName(header.getEventStartDate(), header.getEventEndDate())
//            + " " + header.getCustomer() + " СМЕТА РЕГИСТРАЦИИ Ver1";
////отладка
//            System.out.println("Вот" + formulaRowIndex);
//                    if (formulaMap.containsKey(fileName)) {
//                    formulaMap.get(fileName).add(formulaRowIndex);
//                    } else {
//                    // Логирование ошибки, если файл не был инициализирован
//                    log.error("File {} not found in formulaMap", fileName);
//                    }
