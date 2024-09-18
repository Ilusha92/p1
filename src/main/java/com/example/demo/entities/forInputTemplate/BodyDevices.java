package com.example.demo.entities.forInputTemplate;

import com.example.demo.entities.Device;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BodyDevices {

    List<Device> devices;

}
