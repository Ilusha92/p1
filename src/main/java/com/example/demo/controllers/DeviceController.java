package com.example.demo.controllers;

import com.example.demo.entities.Device;
import com.example.demo.services.DeviceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class DeviceController {

    @Autowired
    private DeviceServiceImpl deviceServiceImpl;

    @GetMapping("/devices")
    @ResponseBody
    public List<Device> getAllDevices() {
        return deviceServiceImpl.getAllDevices();
    }

}