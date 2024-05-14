package com.example.demo.entities;

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
    private String sDeviceCount;
    private String sDevicePrice;

    @ManyToOne
    private Device printerDevice;
    private String pDeviceCount;
    private String pDevicePrice;

    private String cameraDeviceCount;
    private String cameraDevicePrice;

    private String barcodeDeviceCount;
    private String barcodeDevicePrice;

    private String rfidReaderDeviceCount;
    private String rfidReaderDevicePrice;

    private String switchingCount;
    private String switchingPrice;

    private String networkCount;
    private String networkPrice;

    private String tsdCount;
    private String tsdPrice;

    @ManyToOne
    @JoinColumn(name = "header_id", nullable = false)
    private InputHeader header;
}
