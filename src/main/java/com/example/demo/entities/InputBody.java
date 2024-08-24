package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "input_body")
public class InputBody {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Device softDevice;
    private Integer sDeviceCount;
    private Integer sDevicePrice;

    @ManyToOne
    private Device printerDevice;
    private Integer pDeviceCount;
    private Integer pDevicePrice;

    private Integer cameraDeviceCount;
    private Integer cameraDevicePrice;

    private Integer barcodeDeviceCount;
    private Integer barcodeDevicePrice;

    private Integer rfidReaderDeviceCount;
    private Integer rfidReaderDevicePrice;

    private Integer switchingCount;
    private Integer switchingPrice;

    private Integer networkCount;
    private Integer networkPrice;

    private Integer tsdCount;
    private Integer tsdPrice;

    @JsonBackReference
    @ManyToOne
    private InputHeader header;
}
