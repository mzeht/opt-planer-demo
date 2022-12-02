package com.example.timeslot.domain;

import com.example.timeslot.utils.TimePair;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author：peng-wang-12
 * @date: 12/1/22
 */
@Data
public class DayTimeSoltPo {

    private Date day;

    private TimePair timePair;

    private BigDecimal needFmp;
}
