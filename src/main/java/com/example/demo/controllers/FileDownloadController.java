//package com.example.demo;
//
//import com.example.demo.excelService.service.ExcelServiceImpl;
//import jakarta.annotation.Resource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.UrlResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
//import java.io.File;
//import java.io.IOException;
//
//@Controller
//public class FileDownloadController {
//
//    @Autowired
//    private ExcelServiceImpl excelService;
//
//    @GetMapping("/download")
//    public ResponseEntity<Resource> downloadFile() throws IOException {
//        // Вызываем метод updateExcelFile из сервиса
//        File tempFile = excelService.updateExcelFile();
//
//        // Создаем Resource из временного файла
//        Resource resource = (Resource) new UrlResource(tempFile.toURI());
//
//        // Отправляем файл пользователю
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//    }
//}
