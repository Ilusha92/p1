package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FileController {

    private static final String DIRECTORY = "C:\\Users\\Bokan\\Desktop\\demo\\files";


    @GetMapping("/")
    public String listFiles(Model model) {
        File folder = new File(DIRECTORY);
        List<String> files = Arrays.stream(folder.listFiles())
                .filter(File::isFile)
                .map(File::getName)
                .collect(Collectors.toList());
        model.addAttribute("files", files);
        return "home";
    }
}
