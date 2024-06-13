package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean montage = true;
    private LocalTime adminWorkTimeStart;
    private LocalTime adminWorkTimeEnd;
    private int adminsCount = 1;
    private int workHours = 4;
    private int adminHourPrice = 1000;

    private String staffKind1;
    private int staffCount1;
    private LocalTime staffWorkTime1Start;
    private LocalTime staffWorkTime1End;
    private int staffHourPrice1 = 800;

    private String staffKind2;
    private int staffCount2;
    private LocalTime staffWorkTime2Start;
    private LocalTime staffWorkTime2End;
    private int staffHourPrice2 = 800;

    private String staffKind3;
    private int staffCount3;
    private LocalTime staffWorkTime3Start;
    private LocalTime staffWorkTime3End;
    private int staffHourPrice3 = 800;

    private String staffKind4;
    private int staffCount4;
    private LocalTime staffWorkTime4Start;
    private LocalTime staffWorkTime4End;
    private int staffHourPrice4 = 800;

    private String staffKind5;
    private int staffCount5;
    private LocalTime staffWorkTime5Start;
    private LocalTime staffWorkTime5End;
    private int staffHourPrice5 = 800;

    @OneToOne
    @JoinColumn(name = "header_id", nullable = false)
    private InputHeader header;

    @Override
    public String toString() {
        return "Staff{" +
                "id=" + id +
                ", montage=" + montage +
                ", adminWorkTimeStart=" + adminWorkTimeStart +
                ", adminWorkTimeEnd=" + adminWorkTimeEnd +
                ", adminsCount=" + adminsCount +
                ", workHours=" + workHours +
                ", adminHourPrice=" + adminHourPrice +
                ", staffKind1='" + staffKind1 + '\'' +
                ", staffCount1=" + staffCount1 +
                ", staffWorkTime1Start=" + staffWorkTime1Start +
                ", staffWorkTime1End=" + staffWorkTime1End +
                ", staffHourPrice1=" + staffHourPrice1 +
                ", staffKind2='" + staffKind2 + '\'' +
                ", staffCount2=" + staffCount2 +
                ", staffWorkTime2Start=" + staffWorkTime2Start +
                ", staffWorkTime2End=" + staffWorkTime2End +
                ", staffHourPrice2=" + staffHourPrice2 +
                ", staffKind3='" + staffKind3 + '\'' +
                ", staffCount3=" + staffCount3 +
                ", staffWorkTime3Start=" + staffWorkTime3Start +
                ", staffWorkTime3End=" + staffWorkTime3End +
                ", staffHourPrice3=" + staffHourPrice3 +
                ", staffKind4='" + staffKind4 + '\'' +
                ", staffCount4=" + staffCount4 +
                ", staffWorkTime4Start=" + staffWorkTime4Start +
                ", staffWorkTime4End=" + staffWorkTime4End +
                ", staffHourPrice4=" + staffHourPrice4 +
                ", staffKind5='" + staffKind5 + '\'' +
                ", staffCount5=" + staffCount5 +
                ", staffWorkTime5Start=" + staffWorkTime5Start +
                ", staffWorkTime5End=" + staffWorkTime5End +
                ", staffHourPrice5=" + staffHourPrice5 +
                ", header=" + header +
                '}';
    }
}
