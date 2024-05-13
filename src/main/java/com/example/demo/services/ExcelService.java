package com.example.demo.services;

import com.example.demo.entities.InputHeader;

import java.io.File;

public interface ExcelService {

    File createNewFile(InputHeader header);

}
