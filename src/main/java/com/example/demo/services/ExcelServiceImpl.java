package com.example.demo.services;

import com.example.demo.entities.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.Duration;
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
                    + " " + header.getEventName() + " СМЕТА РЕГИСТРАЦИИ Ver1";
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
        cell3.setCellValue(formatPeriod(header.getEventStartDate(), header.getEventEndDate()));

        // Время работы
        Row row4 = sheet.getRow(6);
        Cell cell4 = row4.getCell(3);
        cell4.setCellValue(header.getEventWorkStartTime().toString() + "-" + header.getEventWorkEndTime().toString());

        // Количество посетителей
        Row row5 = sheet.getRow(7);
        Cell cell5 = row5.getCell(3);
        cell5.setCellValue(header.getVisitorsCount());
    }

    public void updateFileWithBodyData(InputHeader header) {
        try {
            String fileName = dateFormatterForFileName(header.getEventStartDate(), header.getEventEndDate())
                    + " " + header.getEventName() + " СМЕТА РЕГИСТРАЦИИ Ver1";
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

            markupBody(header, sheet);
            fillBodyRows(header, sheet, bottomLastRowStyle, bottomLastRowStyle1, bottomBlockRowStyle, bottomBlockRowStyle1);

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

    private void markupBody(InputHeader header, Sheet sheet) {
        int startRowIndex = 9; // Начальный индекс строки для вставки
        System.out.println("Начальный индекс строки для вставки: " + startRowIndex);
        int inputBodiesCount = header.getInputBodies().size(); // Получаем реальное количество inputBodies
        int[] neededRows = new int[inputBodiesCount];

        // Отладочная информация
        System.out.println("Общее количество inputBodies: " + inputBodiesCount);
        // Заполняем массив neededRows количеством девайсов в каждом inputBody
        for (int i = 0; i < inputBodiesCount; i++) {
            neededRows[i] = header.getInputBodies().get(i).getDevices().size();
            System.out.println("InputBody " + i + " содержит " + neededRows[i] + " устройств.");

        }

        for (int i = 0; i < neededRows.length; i++) {
            insertRowsWithShift(sheet, startRowIndex, neededRows[i]);
            if(!header.isWithManyRegPoints()){
                if (neededRows[i] > 1) {
                    System.out.println("Объединяем ячейки для inputBody " + i + " с " + startRowIndex + " по " + (startRowIndex + neededRows[i] - 1));
                    mergeCells(sheet, startRowIndex, startRowIndex + neededRows[i] - 1);
                }
            } else {
                if (neededRows[i] > 1) {
                    mergeCellsForRegPoints(sheet, startRowIndex, startRowIndex + neededRows[i] - 1);
                }
            }

            startRowIndex += neededRows[i];
        }

        if(header.isWithManyRegPoints()){
            additionalMarkUpForManyRegPointsTemplate(sheet, header);
        }
    }

    private void fillBodyRows(InputHeader header, Sheet sheet, CellStyle bottomLastRowStyle, CellStyle bottomLastRowStyle1,
                              CellStyle bottomBlockRowStyle, CellStyle bottomBlockRowStyle1) {
        int startIndex = 9;

        if(header.isSameEquipmentForAllDays()){
            List<String> stringPeriods = getPeriodsStrings(header);
            List<Integer> workDaysInPeriods = getPeriodsDaysCount(header);
            for(int i = 0; i < header.getInputBodies().size(); i++){
                List<Device> devices = header.getInputBodies()
                        .get(header.getInputBodies().size() - 1 - i) // Получаем InputBody с конца
                        .getDevices();
                int workDays = workDaysInPeriods.get(i);
                String period = stringPeriods.get(i);

                for(int j = 0; j < devices.size(); j++ ){
                    Row row = sheet.getRow(startIndex);
                    Cell cellPeriod = row.getCell(0);
                    cellPeriod.setCellValue(period);
                    Cell cellName = row.getCell(1);
                    cellName.setCellValue(devices.get(j).getName());
                    Cell cellDays = row.getCell(3);
                    cellDays.setCellValue(workDays);
                    Cell cellCount = row.getCell(4);
                    cellCount.setCellValue(devices.get(j).getCount());
                    Cell cellPrice = row.getCell(5);
                    cellPrice.setCellValue(devices.get(j).getPriceFor1q());
                    Cell cellSum = row.getCell(7);
                    cellSum.setCellFormula("E" + (startIndex + 1) + "*F" + (startIndex + 1) + "*" + workDays);
                    startIndex++;
                    if (j == devices.size() - 1) {
                        applyStyles(sheet.getRow(startIndex-1), bottomBlockRowStyle, bottomBlockRowStyle1);
                    }
                }

            }
            applyStyles(sheet.getRow(startIndex-1), bottomLastRowStyle, bottomLastRowStyle1);
            Row rowForFormula = sheet.getRow(startIndex);
            Cell cellForFormula = rowForFormula.getCell(7);
            cellForFormula.setCellFormula("SUM(H" + (10) + ":H" +
                    (startIndex) + ")");
            Row finalFormulaRow = sheet.getRow(findFirstRow(sheet, "Заказчик предоставляет:*") - 1);
            Cell finalFormulaCell = finalFormulaRow.getCell(7);
            finalFormulaCell.setCellFormula("SUM(H" + (rowForFormula.getRowNum()+1) + ")");

        } else if(header.isWithManyRegPoints()) {
            List<String> stringPeriods = getRegPointsDates(header);
            List<String> regPoints = getRegPointsDescriptions(header);
            List<Integer> workDaysInPeriods = getRegPointsDaysCount(header);
            for(int i = 0; i < header.getInputBodies().size(); i++){
                List<Device> devices = header.getInputBodies().get(i).getDevices();
                int workDays = workDaysInPeriods.get(i);
                String period = stringPeriods.get(i);
                String regPoint = regPoints.get(i);
                for(int j = 0; j < devices.size(); j++ ){
                    Row row = sheet.getRow(startIndex);
                    Cell cellPeriod = row.getCell(0);
                    cellPeriod.setCellValue(period);
                    Cell cellRegPoint = row.getCell(1);
                    cellRegPoint.setCellValue(regPoint);
                    Cell cellName = row.getCell(2);
                    cellName.setCellValue(devices.get(j).getName());
                    Cell cellDays = row.getCell(5);
                    cellDays.setCellValue(workDays);
                    Cell cellCount = row.getCell(4);
                    cellCount.setCellValue(devices.get(j).getCount());
                    Cell cellPrice = row.getCell(6);
                    cellPrice.setCellValue(devices.get(j).getPriceFor1q());
                    Cell cellSum = row.getCell(7);
                    cellSum.setCellFormula("E" + (startIndex + 1) + "*F" + (startIndex + 1) + "*G" + (startIndex + 1));
                    startIndex++;
                    if (j == devices.size() - 1) {
                        applyStylesWithRegPoints(sheet.getRow(startIndex-1), bottomBlockRowStyle, bottomBlockRowStyle1);
                    }
                }

            }
            applyStylesWithRegPoints(sheet.getRow(startIndex-1), bottomLastRowStyle, bottomLastRowStyle1);
            Row rowForFormula = sheet.getRow(startIndex);
            Cell cellForFormula = rowForFormula.getCell(7);
            cellForFormula.setCellFormula("SUM(H" + (10) + ":H" +
                    (startIndex) + ")");
            Row finalFormulaRow = sheet.getRow(findFirstRow(sheet, "Заказчик предоставляет:*") - 1);
            Cell finalFormulaCell = finalFormulaRow.getCell(7);
            finalFormulaCell.setCellFormula("SUM(H" + (rowForFormula.getRowNum()+1) + ")");

        } else {
            List<String> workDates = getAllDaysBetweenPeriods(header);
            for(int i = 0; i < header.getInputBodies().size(); i++){
                List<Device> devices = header.getInputBodies().get(i).getDevices();
                String date = workDates.get(i);
                for(int j = 0; j < devices.size(); j++ ){
                    Row row = sheet.getRow(startIndex);
                    Cell cellPeriod = row.getCell(0);
                    cellPeriod.setCellValue(date);
                    Cell cellName = row.getCell(1);
                    cellName.setCellValue(devices.get(j).getName());
                    Cell cellCount = row.getCell(4);
                    cellCount.setCellValue(devices.get(j).getCount());
                    Cell cellPrice = row.getCell(5);
                    cellPrice.setCellValue(devices.get(j).getPriceFor1q());
                    Cell cellSum = row.getCell(7);
                    cellSum.setCellFormula("E" + (startIndex + 1) + "*F" + (startIndex + 1));
                    startIndex++;
                    if (j == devices.size() - 1) {
                        applyStyles(sheet.getRow(startIndex-1), bottomBlockRowStyle, bottomBlockRowStyle1);
                    }
                }

            }
            applyStyles(sheet.getRow(startIndex-1), bottomLastRowStyle, bottomLastRowStyle1);
            Row rowForFormula = sheet.getRow(startIndex);
            Cell cellForFormula = rowForFormula.getCell(7);
            cellForFormula.setCellFormula("SUM(H" + (10) + ":H" +
                    (startIndex) + ")");
            //TODO: запись стартиндекса для финальной формулы
            Row finalFormulaRow = sheet.getRow(findFirstRow(sheet, "Заказчик предоставляет:*") - 1);
            Cell finalFormulaCell = finalFormulaRow.getCell(7);
            finalFormulaCell.setCellFormula("SUM(H" + (rowForFormula.getRowNum()+1) + ")");
        }

    }

    public List<String> getAllDaysBetweenPeriods(InputHeader header) {
        List<String> allDays = new ArrayList<>();

        // Добавляем все даты между prePrintStart и prePrintEnd
        if (header.getPrePrintStart() != null && header.getPrePrintEnd() != null) {
            LocalDate currentDate = header.getPrePrintStart();
            while (!currentDate.isAfter(header.getPrePrintEnd())) {
                allDays.add(currentDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                currentDate = currentDate.plusDays(1);
            }
        }

        // Добавляем все даты между EventStartDate и EventEndDate
        if (header.getEventStartDate() != null && header.getEventEndDate() != null) {
            LocalDate currentDate = header.getEventStartDate();
            while (!currentDate.isAfter(header.getEventEndDate())) {
                allDays.add(currentDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                currentDate = currentDate.plusDays(1);
            }
        }

        return allDays;
    }

    public List<String> getPeriodsStrings(InputHeader header) {
        List<String> periodsStrings = new ArrayList<>();

        if (header.getPrePrintStart() != null && header.getPrePrintEnd() != null) {
            String prePrintPeriod = formatPeriod(header.getPrePrintStart(), header.getPrePrintEnd());
            periodsStrings.add(prePrintPeriod);
        }
        if (header.getBoolStart1() != null && header.getBoolEnd1() != null) {
            periodsStrings.add(formatPeriod(header.getBoolStart1(), header.getBoolEnd1()));
        }
        if (header.getBoolStart2() != null && header.getBoolEnd2() != null) {
            periodsStrings.add(formatPeriod(header.getBoolStart2(), header.getBoolEnd2()));
        }
        if (header.getBoolStart3() != null && header.getBoolEnd3() != null) {
            periodsStrings.add(formatPeriod(header.getBoolStart3(), header.getBoolEnd3()));
        }
        if (header.getBoolStart4() != null && header.getBoolEnd4() != null) {
            periodsStrings.add(formatPeriod(header.getBoolStart4(), header.getBoolEnd4()));
        }

        return periodsStrings;
    }

    public List<Integer> getPeriodsDaysCount(InputHeader header) {
        List<Integer> periodsDaysCount = new ArrayList<>();

        // Добавляем период prePrint
        if (header.getPrePrintStart() != null && header.getPrePrintEnd() != null) {
            int days = calculateDaysBetween(header.getPrePrintStart(), header.getPrePrintEnd());
            periodsDaysCount.add(days);
        }

        // Добавляем периоды boolStart - boolEnd
        addPeriodDaysToList(periodsDaysCount, header.getBoolStart1(), header.getBoolEnd1());
        addPeriodDaysToList(periodsDaysCount, header.getBoolStart2(), header.getBoolEnd2());
        addPeriodDaysToList(periodsDaysCount, header.getBoolStart3(), header.getBoolEnd3());
        addPeriodDaysToList(periodsDaysCount, header.getBoolStart4(), header.getBoolEnd4());

        return periodsDaysCount;
    }

    private int calculateDaysBetween(LocalDate start, LocalDate end) {
        return (int) ChronoUnit.DAYS.between(start, end) + 1; // Включаем начальную дату в подсчет
    }

    private void addPeriodDaysToList(List<Integer> periodsDaysCount, LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            int days = calculateDaysBetween(start, end);
            periodsDaysCount.add(days);
        }
    }

    private void additionalMarkUpForManyRegPointsTemplate(Sheet sheet, InputHeader header) {
        Cell firstCell = sheet.getRow(8).getCell(2);
        CellStyle originalStyle = firstCell.getCellStyle();
        String originalValue = firstCell.getStringCellValue();

        // Удаление старого объединения
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() == 8 && mergedRegion.getLastRow() == 8 &&
                    mergedRegion.getFirstColumn() == 2 && mergedRegion.getLastColumn() == 3) {
                sheet.removeMergedRegion(i);
                break;
            }
        }

        // Добавляем новое объединение
        sheet.addMergedRegion(new CellRangeAddress(8, 8, 1, 3));
        Cell newFirstCell = sheet.getRow(8).getCell(1);
        if (newFirstCell == null) {
            newFirstCell = sheet.getRow(8).createCell(1);
        }
        newFirstCell.setCellStyle(originalStyle);
        newFirstCell.setCellValue(originalValue);

        // Создаем HashMap: даты -> ID InputBody
        Map<String, List<Long>> dateToInputBodyMap = new LinkedHashMap<>();
        List<String> sortedRegPoints = getRegPointsDates(header);

        for (int i = 0; i < sortedRegPoints.size(); i++) {
            String date = sortedRegPoints.get(i);
            Long inputBodyId = header.getInputBodies().get(i).getId();

            dateToInputBodyMap.computeIfAbsent(date, k -> new ArrayList<>()).add(inputBodyId);
        }

        // Инициализация firstRow для первого выполнения mergeCells
        int firstRow = 9;

        //отладка
        for (Map.Entry<String, List<Long>> entry : dateToInputBodyMap.entrySet()) {
            String date = entry.getKey();
            List<Long> inputBodyIds = entry.getValue();

            // Выводим дату
            System.out.print("Дата: " + date + " -> InputBody IDs: ");

            // Выводим все ID, связанные с этой датой
            for (Long id : inputBodyIds) {
                System.out.print(id + " ");
            }
            System.out.println(); // Переход на следующую строку
        }
        // Проходим по всем датам в HashMap и выполняем mergeCells
        for (Map.Entry<String, List<Long>> entry : dateToInputBodyMap.entrySet()) {
            String date = entry.getKey();
            List<Long> inputBodyIds = entry.getValue();

            // Суммируем количество устройств для всех InputBody, связанных с этой датой
            int totalDeviceCount = 0;

            for (Long inputBodyId : inputBodyIds) {
                InputBody inputBody = getInputBodyById(header, inputBodyId);  // Метод для получения InputBody по ID
                totalDeviceCount += inputBody.getDevices().size();  // Суммируем deviceCount
            }

            // Вычисляем lastRow на основе общего количества устройств для всех InputBody
            int lastRow = firstRow + totalDeviceCount - 1;

            if (totalDeviceCount > 1) {
                mergeCells(sheet, firstRow, lastRow);
            }

            firstRow = lastRow + 1;
        }
    }

    private InputBody getInputBodyById(InputHeader header, Long inputBodyId) {
        return header.getInputBodies().stream()
                .filter(inputBody -> inputBody.getId().equals(inputBodyId))
                .findFirst()
                .orElse(null);
    }

    private InputStaff getInputStaffById(InputHeader header, Long inputStaffId) {
        return header.getInputStaffs().stream()
                .filter(inputStaff -> inputStaff.getId().equals(inputStaffId))
                .findFirst()
                .orElse(null);
    }

    public String formatPeriod(LocalDate start, LocalDate end) {
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("dd.MM");

        if (start.equals(end)) {
            // Если даты равны, выводим только одну дату
            return start.format(fullFormatter);
        } else if (start.getMonth().equals(end.getMonth()) && start.getYear() == end.getYear()) {
            // Если месяц и год одинаковые, выводим формат dd-dd.MM.yyyy
            return start.getDayOfMonth() + "-" + end.format(fullFormatter);
        } else {
            // Если даты разные по месяцу или году, выводим обе даты полностью
            return start.format(shortFormatter) + "-" + end.format(fullFormatter);
        }
    }

    public List<String> getRegPointsDates(InputHeader header) {
        List<String> stringsRegPoints = new ArrayList<>();
        List<RegPoint> regPointsForSort = sortRegPoints(header.getRegPoints());

        for (RegPoint regPoint : regPointsForSort) {
            // Применяем метод formatPeriod для форматирования дат
            String dateRange = formatPeriod(regPoint.getStartRPDate(), regPoint.getEndRPDate());
            stringsRegPoints.add(dateRange);
        }

        return stringsRegPoints;
    }

    private List<RegPoint> sortRegPoints(List<RegPoint> regPoints) {
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

    public List<String> getRegPointsDescriptions(InputHeader header) {
        List<String> regPointsDescriptions = new ArrayList<>();
        List<RegPoint> regPointsForSort = sortRegPoints(header.getRegPoints());

        for (RegPoint regPoint : regPointsForSort) {
            String formattedDescription = regPoint.getDescription() + "\n(" + regPoint.getName() + ")";
            regPointsDescriptions.add(formattedDescription);
        }

        return regPointsDescriptions;
    }

    public List<Integer> getRegPointsDaysCount(InputHeader header) {
        List<Integer> daysCounts = new ArrayList<>();
        List<RegPoint> regPointsForSort = sortRegPoints(header.getRegPoints());

        for (RegPoint regPoint : regPointsForSort) {
            // Подсчитываем количество дней между датами
            long daysBetween = ChronoUnit.DAYS.between(regPoint.getStartRPDate(), regPoint.getEndRPDate()) + 1; // +1 чтобы включить оба дня
            daysCounts.add((int) daysBetween); // Преобразуем в int для добавления в список
        }

        return daysCounts;
    }

    private void mergeCellsForRegPoints(Sheet sheet, int firstRow, int lastRow) {
        // Удаление существующих объединений, которые пересекаются с новыми объединениями
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() >= firstRow && mergedRegion.getLastRow() <= lastRow && mergedRegion.getFirstColumn() == 1) {
                sheet.removeMergedRegion(i);
                i--; // Обновляем индекс, поскольку список уменьшился
            }
        }

        // Проверяем, что объединение имеет смысл (чтобы было больше одной строки для объединения)
        if (firstRow != lastRow) {
            CellRangeAddress cellRangeAddress = new CellRangeAddress(firstRow, lastRow, 1, 1);
            sheet.addMergedRegion(cellRangeAddress);
        }
    }

    private void insertRowsWithShift(Sheet sheet, int startRow, int numberOfRows) {
        System.out.println("Вставка " + numberOfRows + " строк начиная с индекса " + startRow);

        for (int i = 0; i < numberOfRows; i++) {
            int currentRow = startRow + i;
            System.out.println("Сдвигаем строки начиная с " + currentRow);
            sheet.shiftRows(currentRow, sheet.getLastRowNum(), 1);

            // Получаем строку-источник и создаем новую строку
            Row sourceRow = sheet.getRow(currentRow - 1);
            Row newRow = sheet.createRow(currentRow);

            System.out.println("Копируем содержимое и стили из строки " + (currentRow - 1) + " в строку " + currentRow);
            // Копирование стилей и значений ячеек
            copyRow(sourceRow, newRow);

            System.out.println("Копируем объединенные ячейки из строки " + (currentRow - 1) + " в строку " + currentRow);
            // Копирование объединенных ячеек
            copyMergedRegions(sheet, currentRow - 1, currentRow);
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
        if (header.isWithManyRegPoints()){
            return "classpath:smeta2.xlsx";
        } else if (header.isSameEquipmentForAllDays()) {
            return "classpath:smeta1.xlsx";
        } else {
            return "classpath:smeta.xlsx";
        }
    }

    public void updateFileWithSuppliesData(InputHeader header, Supplies supplies) {

        try {
            String fileName = dateFormatterForFileName(header.getEventStartDate(), header.getEventEndDate())
                    + " " + header.getEventName() + " СМЕТА РЕГИСТРАЦИИ Ver1";
            String filePath = excelFilesDirectory + File.separator + fileName + ".xlsx";
            File file = new File(filePath);

            if (!file.exists()) {
                throw new FileNotFoundException("Файл не найден: " + filePath);
            }

            Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
            Sheet sheet = workbook.getSheetAt(0);

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

            writeSupplies(supplies, sheet, bottomLastRowStyle, bottomLastRowStyle1);

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

    private void writeSupplies(Supplies supplies, Sheet sheet,
                               CellStyle bottomLastRowStyle, CellStyle bottomLastRowStyle1) {
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
            applyStyles(sheet.getRow(formulaRow.getRowNum()-1), bottomLastRowStyle, bottomLastRowStyle1);
            Row finalFormulaRow = sheet.getRow(findFirstRow(sheet, "Заказчик предоставляет:*") - 1);
            Cell finalFormulaCell = finalFormulaRow.getCell(7);
            String forUpdateFinalFormula = finalFormulaCell.getCellFormula();
            System.out.println("T<FYF" + forUpdateFinalFormula);
            finalFormulaCell.setCellFormula(forUpdateFinalFormula.substring(0, forUpdateFinalFormula.length() - 1) + ",H"
                    + (formulaRow.getRowNum()+1) + ")");
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

    public void updateFileWithStaff(InputHeader header) {

        try {
            String fileName = dateFormatterForFileName(header.getEventStartDate(), header.getEventEndDate())
                    + " " + header.getEventName() + " СМЕТА РЕГИСТРАЦИИ Ver1";
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

            if(header.getInputStaffs().size() != 0 && header.getMountings().size() != 0) {
                markupOtherService(header, sheet);
                fillMountingRows(header,sheet, bottomLastRowStyle, bottomLastRowStyle1, bottomBlockRowStyle, bottomBlockRowStyle1);
                fillStaffRows(header, sheet, bottomLastRowStyle, bottomLastRowStyle1, bottomBlockRowStyle, bottomBlockRowStyle1);

                Row finalFormulaRow = sheet.getRow(findFirstRow(sheet, "Заказчик предоставляет:*") - 1);
                Row blockFormulaRow = sheet.getRow(findFirstRow(sheet, "ЛОГИСТИКА") - 1);
                Cell finalFormulaCell = finalFormulaRow.getCell(7);
                String forUpdateFinalFormula = finalFormulaCell.getCellFormula();
                System.out.println("T<FYF" + forUpdateFinalFormula);
                finalFormulaCell.setCellFormula(forUpdateFinalFormula.substring(0, forUpdateFinalFormula.length() - 1) + ",H"
                        + (blockFormulaRow.getRowNum()+1) + ")");
            } else {
                int removeRowIndex = findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ");
                removeMergedRegions(sheet, removeRowIndex+1);
                removeRow(sheet, removeRowIndex+1);
                removeMergedRegions(sheet, removeRowIndex);
                removeRow(sheet, removeRowIndex);
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

    private void markupOtherService(InputHeader header, Sheet sheet) {

        boolean hasMontage = false;
        boolean hasStaff = false;

        int startRowIndex = findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ") + 1;
        System.out.println("Начальный индекс строки для вставки: " + startRowIndex);


        if(header.getInputStaffs().size() != 0){
            hasStaff = true;
            int inputStaffsCount = header.getInputStaffs().size();
            int[] neededRows = new int[inputStaffsCount];

            for (int i = 0; i < inputStaffsCount; i++) {
                neededRows[i] = header.getInputStaffs().get(i).getStaffs().size();
                System.out.println("InputStaff " + i + " содержит " + neededRows[i] + " устройств.");
            }

            for (int i = 0; i < neededRows.length; i++) {
                insertRowsWithShift(sheet, startRowIndex, neededRows[i]);
                if (!header.isWithManyRegPoints()) {
                    if (neededRows[i] > 1) {
                        System.out.println("Объединяем ячейки для inputBody " + i + " с " + startRowIndex + " по " + (startRowIndex + neededRows[i] - 1));
                        mergeCells(sheet, startRowIndex, startRowIndex + neededRows[i] - 1);
                    }
                } else {
                    if (neededRows[i] > 1) {
                        mergeCellsForRegPoints(sheet, startRowIndex, startRowIndex + neededRows[i] - 1);
                    }
                }

                startRowIndex += neededRows[i];
            }
            if(header.isWithManyRegPoints()){
                additionalMarkUpForManyRegPointsTemplate2(sheet, header);
            }
        }

        if(header.getMountings().size() != 0){
            hasMontage = true;
            int start =  findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ") + 1;

            if(header.isWithManyRegPoints() && !hasStaff){
                int startRowHead = findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ");
                Cell firstCell = sheet.getRow(startRowHead).getCell(2);
                CellStyle originalStyle = firstCell.getCellStyle();
                String originalValue = firstCell.getStringCellValue();

                // Удаление старого объединения
                for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                    CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
                    if (mergedRegion.getFirstRow() == startRowHead && mergedRegion.getLastRow() == startRowHead &&
                            mergedRegion.getFirstColumn() == 2 && mergedRegion.getLastColumn() == 3) {
                        sheet.removeMergedRegion(i);
                        break;
                    }
                }
                sheet.addMergedRegion(new CellRangeAddress(startRowHead, startRowHead, 1, 3));
                Cell newFirstCell = sheet.getRow(startRowHead).getCell(1);
                if (newFirstCell == null) {
                    newFirstCell = sheet.getRow(startRowHead).createCell(1);
                }
                newFirstCell.setCellStyle(originalStyle);
                newFirstCell.setCellValue(originalValue);
            }

            int mSize = header.getMountings().size(); // Получаем реальное количество m строк
            insertRowsWithShift(sheet, start, mSize);
            if(mSize > 1){
                mergeCells(sheet, start, start + mSize - 1);
            }
        }

    }

    private void additionalMarkUpForManyRegPointsTemplate2(Sheet sheet, InputHeader header) {
        int startRowIndex = findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ");
        Cell firstCell = sheet.getRow(startRowIndex).getCell(2);
        CellStyle originalStyle = firstCell.getCellStyle();
        String originalValue = firstCell.getStringCellValue();

        // Удаление старого объединения
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() == startRowIndex && mergedRegion.getLastRow() == startRowIndex &&
                    mergedRegion.getFirstColumn() == 2 && mergedRegion.getLastColumn() == 2) {
                sheet.removeMergedRegion(i);
                break;
            }
        }

        // Добавляем новое объединение
        sheet.addMergedRegion(new CellRangeAddress(startRowIndex, startRowIndex, 1, 2));
        Cell newFirstCell = sheet.getRow(startRowIndex).getCell(1);
        if (newFirstCell == null) {
            newFirstCell = sheet.getRow(startRowIndex).createCell(1);
        }
        newFirstCell.setCellStyle(originalStyle);
        newFirstCell.setCellValue(originalValue);

        // Создаем HashMap: даты -> ID InputBody
        Map<String, List<Long>> dateToInputStaffMap = new LinkedHashMap<>();
        List<String> sortedRegPoints = getRegPointsDates(header);

        for (int i = 0; i < sortedRegPoints.size(); i++) {
            String date = sortedRegPoints.get(i);
            Long inputStaffId = header.getInputStaffs().get(i).getId();

            dateToInputStaffMap.computeIfAbsent(date, k -> new ArrayList<>()).add(inputStaffId);
        }

        // Инициализация firstRow для первого выполнения mergeCells
        int firstRow = startRowIndex+1;

        //отладка
        for (Map.Entry<String, List<Long>> entry : dateToInputStaffMap.entrySet()) {
            String date = entry.getKey();
            List<Long> inputStaffIds = entry.getValue();

            // Выводим дату
            System.out.print("Дата: " + date + " -> inputStaffIds IDs: ");

            // Выводим все ID, связанные с этой датой
            for (Long id : inputStaffIds) {
                System.out.print(id + " ");
            }
            System.out.println(); // Переход на следующую строку
        }
        // Проходим по всем датам в HashMap и выполняем mergeCells
        for (Map.Entry<String, List<Long>> entry : dateToInputStaffMap.entrySet()) {
            String date = entry.getKey();
            List<Long> inputStaffIds = entry.getValue();

            // Суммируем количество устройств для всех InputBody, связанных с этой датой
            int totalStaffCount = 0;

            for (Long inputBodyId : inputStaffIds) {
                InputStaff inputStaff = getInputStaffById(header, inputBodyId);  // Метод для получения InputBody по ID
                totalStaffCount += inputStaff.getStaffs().size();  // Суммируем deviceCount
            }

            // Вычисляем lastRow на основе общего количества устройств для всех InputBody
            int lastRow = firstRow + totalStaffCount - 1;

            if (totalStaffCount > 1) {
                mergeCells(sheet, firstRow, lastRow);
            }
            firstRow = lastRow + 1;
        }
    }

    private void fillMountingRows(InputHeader header, Sheet sheet, CellStyle bottomLastRowStyle, CellStyle bottomLastRowStyle1,
                               CellStyle bottomBlockRowStyle, CellStyle bottomBlockRowStyle1) {
        int startIndex = findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ")+1;
        List<Mounting> mountings = header.getMountings();
        for(int j = 0; j < mountings.size(); j++ ){
            Row row = sheet.getRow(startIndex);
            Cell cellForBlank = row.getCell(0);
            cellForBlank.setBlank();
            Cell cellName = row.getCell(1);
            cellName.setCellValue(mountings.get(j).getKindOfService());
            Cell cellHours = row.getCell(4);
            cellHours.setCellValue(mountings.get(j).getWorkHours());
            Cell cellWorkPeriod = row.getCell(3);
            cellWorkPeriod.setCellValue("-");
            Cell cellQuantity = row.getCell(5);
            cellQuantity.setCellValue(mountings.get(j).getAgentCount());
            Cell cellPriceForHour = row.getCell(6);
            cellPriceForHour.setCellValue(mountings.get(j).getPriceForHour());

            Cell cellSum = row.getCell(7);
            cellSum.setCellFormula("E" + (startIndex + 1) + "*F" + (startIndex + 1) + "*G" + (startIndex + 1));
            startIndex++;
            if (j == mountings.size() - 1) {
                applyStyles(sheet.getRow(startIndex-1), bottomBlockRowStyle, bottomBlockRowStyle1);
            }

        }

    }

    private void fillStaffRows(InputHeader header, Sheet sheet, CellStyle bottomLastRowStyle, CellStyle bottomLastRowStyle1,
                              CellStyle bottomBlockRowStyle, CellStyle bottomBlockRowStyle1) {
        int startIndex = findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ") + header.getMountings().size() + 1;
        int startFormula = findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ") + header.getMountings().size() + 1;
        if(header.isSameEquipmentForAllDays()){
            List<String> stringPeriods = getPeriodsStrings(header);
            List<Integer> workDaysInPeriods = getPeriodsDaysCount(header);
            for(int i = 0; i < header.getInputStaffs().size(); i++){
                List<Staff> staffs = header.getInputStaffs().get(i).getStaffs();
                int workDays = workDaysInPeriods.get(i);
                String period = stringPeriods.get(i);

                for(int j = 0; j < staffs.size(); j++ ){
                    Row row = sheet.getRow(startIndex);
                    Cell cellPeriod = row.getCell(0);
                    cellPeriod.setCellValue(period);
                    Cell cellName = row.getCell(1);
                    cellName.setCellValue(staffs.get(j).getKindOfStaff());
                    Cell cellHours = row.getCell(4);
                    cellHours.setCellValue(Duration.between(staffs.get(j).getStartTime(),staffs.get(j).getEndTime()).toHours());
                    Cell cellWorkPeriod = row.getCell(3);
                    cellWorkPeriod.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                    Cell cellQuantity = row.getCell(5);
                    cellQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                    Cell cellPriceForHour = row.getCell(6);
                    cellPriceForHour.setCellValue(staffs.get(j).getBetPerHour());

                    Cell cellSum = row.getCell(7);
                    cellSum.setCellFormula("E" + (startIndex + 1) + "*F" + (startIndex + 1) + "*G" + (startIndex + 1) + "*" + workDays);
                    startIndex++;
                    if (j == staffs.size() - 1) {
                        applyStyles(sheet.getRow(startIndex-1), bottomBlockRowStyle, bottomBlockRowStyle1);
                    }
                }
            }
            applyStyles(sheet.getRow(startIndex-1), bottomLastRowStyle, bottomLastRowStyle1);
            Row rowForFormula = sheet.getRow(startIndex);
            Cell cellForFormula = rowForFormula.getCell(7);
            cellForFormula.setCellFormula("SUM(H" + (findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ")+1) + ":H" +
                    (startIndex) + ")");



        } else if(header.isWithManyRegPoints()) {
            List<String> stringPeriods = getRegPointsDates(header);
            List<String> regPoints = getRegPointsDescriptions(header);
            List<Integer> workDaysInPeriods = getRegPointsDaysCount(header);
            for(int i = 0; i < header.getInputStaffs().size(); i++){
                List<Staff> staffs = header.getInputStaffs().get(i).getStaffs();
                int workDays = workDaysInPeriods.get(i);
                String period = stringPeriods.get(i);
                String regPoint = regPoints.get(i);
                for(int j = 0; j < staffs.size(); j++ ){
                    Row row = sheet.getRow(startIndex);
                    Cell cellPeriod = row.getCell(0);
                    cellPeriod.setCellValue(period);
                    Cell cellRegPoint = row.getCell(1);
                    cellRegPoint.setCellValue(regPoint);
                    Cell cellName = row.getCell(2);
                    cellName.setCellValue(staffs.get(j).getKindOfStaff());
                    Cell cellHours = row.getCell(4);
                    cellHours.setCellValue(Duration.between(staffs.get(j).getStartTime(),staffs.get(j).getEndTime()).toHours());
                    Cell cellWorkPeriod = row.getCell(3);
                    cellWorkPeriod.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                    Cell cellQuantity = row.getCell(5);
                    cellQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                    Cell cellPriceForHour = row.getCell(6);
                    cellPriceForHour.setCellValue(staffs.get(j).getBetPerHour());
                    Cell cellSum = row.getCell(7);
                    cellSum.setCellFormula("E" + (startIndex + 1) + "*F" + (startIndex + 1) + "*G" + (startIndex + 1) + "*" + workDays);
                    startIndex++;
                    if (j == staffs.size() - 1) {
                        applyStylesWithRegPoints(sheet.getRow(startIndex-1), bottomBlockRowStyle, bottomBlockRowStyle1);
                    }
                }

            }
            applyStylesWithRegPoints(sheet.getRow(startIndex-1), bottomLastRowStyle, bottomLastRowStyle1);
            Row rowForFormula = sheet.getRow(startIndex);
            Cell cellForFormula = rowForFormula.getCell(7);
            cellForFormula.setCellFormula("SUM(H" + (findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ")+1) + ":H" +
                    (startIndex) + ")");

//            Row finalFormulaRow = sheet.getRow(findFirstRow(sheet, "Заказчик предоставляет:*") - 1);
//            Cell finalFormulaCell = finalFormulaRow.getCell(7);
//            String forUpdateFinalFormula = finalFormulaCell.getCellFormula();
//            System.out.println("T<FYF" + forUpdateFinalFormula);
//            finalFormulaCell.setCellFormula(forUpdateFinalFormula.substring(0, forUpdateFinalFormula.length() - 1) + ",H"
//                    + startIndex+1 + ")");

        } else {
            List<String> workDates = getAllDaysBetweenPeriods(header);
            for(int i = 0; i < header.getInputStaffs().size(); i++){
                List<Staff> staffs = header.getInputStaffs().get(i).getStaffs();
                String date = workDates.get(i);
                for(int j = 0; j < staffs.size(); j++ ){
                    Row row = sheet.getRow(startIndex);
                    Cell cellPeriod = row.getCell(0);
                    cellPeriod.setCellValue(date);
                    Cell cellName = row.getCell(1);
                    cellName.setCellValue(staffs.get(j).getKindOfStaff());
                    Cell cellHours = row.getCell(4);
                    cellHours.setCellValue(Duration.between(staffs.get(j).getStartTime(),staffs.get(j).getEndTime()).toHours());
                    Cell cellWorkPeriod = row.getCell(3);
                    cellWorkPeriod.setCellValue(staffs.get(j).getStartTime().toString() + "-" + staffs.get(j).getEndTime().toString());
                    Cell cellQuantity = row.getCell(5);
                    cellQuantity.setCellValue(staffs.get(j).getStaffQuantity());
                    Cell cellPriceForHour = row.getCell(6);
                    cellPriceForHour.setCellValue(staffs.get(j).getBetPerHour());
                    Cell cellSum = row.getCell(7);
                    cellSum.setCellFormula("E" + (startIndex + 1) + "*F" + (startIndex + 1) + "*G" + (startIndex + 1));
                    startIndex++;
                    if (j == staffs.size() - 1) {
                        applyStyles(sheet.getRow(startIndex-1), bottomBlockRowStyle, bottomBlockRowStyle1);
                    }
                }

            }
            applyStyles(sheet.getRow(startIndex-1), bottomLastRowStyle, bottomLastRowStyle1);
            Row rowForFormula = sheet.getRow(startIndex);
            Cell cellForFormula = rowForFormula.getCell(7);
            cellForFormula.setCellFormula("SUM(H" + (findFirstRow(sheet, "ПРОЧИЕ УСЛУГИ")+1) + ":H" +
                    (startIndex) + ")");

//            Row finalFormulaRow = sheet.getRow(findFirstRow(sheet, "Заказчик предоставляет:*") - 1);
//            Cell finalFormulaCell = finalFormulaRow.getCell(7);
//            String forUpdateFinalFormula = finalFormulaCell.getCellFormula();
//            System.out.println("T<FYF" + forUpdateFinalFormula);
//            finalFormulaCell.setCellFormula(forUpdateFinalFormula.substring(0, forUpdateFinalFormula.length() - 1) + ",H"
//                    + startIndex+1 + ")");
            //TODO: запись стартиндекса для финальной формулы
        }

    }

    private void applyStyles(Row row, CellStyle cellStyle, CellStyle cellStyle1) {
        for (int j = 0; j < 8; j++) {
            if (j == 1) {
                row.getCell(j).setCellStyle(cellStyle1);
            } else {
                row.getCell(j).setCellStyle(cellStyle);
            }
        }
    }

    private void applyStylesWithRegPoints(Row row, CellStyle cellStyle, CellStyle cellStyle1) {
        for (int j = 0; j < 8; j++) {
            if (j == 2) {
                row.getCell(j).setCellStyle(cellStyle1);
            } else {
                row.getCell(j).setCellStyle(cellStyle);
            }
        }
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
                    + " " + header.getEventName() + " СМЕТА РЕГИСТРАЦИИ Ver1";
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

            Row finalFormulaRow = sheet.getRow(findFirstRow(sheet, "Заказчик предоставляет:*") - 1);
            Cell finalFormulaCell = finalFormulaRow.getCell(7);
            String forUpdateFinalFormula = finalFormulaCell.getCellFormula();
            System.out.println("T<FYF" + forUpdateFinalFormula);
            finalFormulaCell.setCellFormula(forUpdateFinalFormula.substring(0, forUpdateFinalFormula.length() - 1) + ",H"
                    + (formulaStartRow + neededRowsForLogistics + 1) + ")");


        } else {
            int rowForDelete = findFirstRow(sheet, "ЛОГИСТИКА");
            removeMergedRegions(sheet, rowForDelete + 1);
            removeRow(sheet, rowForDelete + 1);
            removeMergedRegions(sheet, rowForDelete);
            removeRow(sheet, rowForDelete);
        }

    }

}
