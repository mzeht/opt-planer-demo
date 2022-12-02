package com.example.timeslot;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import com.example.timeslot.domain.*;
import com.example.timeslot.utils.TimePair;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.CollectionUtils;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

import java.math.BigDecimal;
import java.util.Collections;

class TimeslotApplicationTests {


    private ConstraintVerifier<TableConstraintProvider, TableSolution> constraintVerifier
            = ConstraintVerifier.build(new TableConstraintProvider(), TableSolution.class, DayStaffPo.class);


    @Test
    public void contextLoads() {
        DayTimeSoltPo dayTimeSoltPo = new DayTimeSoltPo();
        dayTimeSoltPo.setDay(DateUtil.parse("2022-10-10"));
        TimePair timePair = new TimePair();
        timePair.setStart(DateUtil.parse("2022-10-10 8:00").getTime());
        timePair.setEnd(DateUtil.parse("2022-10-10 9:00").getTime());
        dayTimeSoltPo.setNeedFmp(BigDecimal.valueOf(10));
        dayTimeSoltPo.setTimePair(timePair);

        DayStaffPo dayStaffPo = new DayStaffPo();
        dayStaffPo.setDay(DateUtil.parse("2022-10-10"));
        dayStaffPo.setCasUserId(10L);
        dayStaffPo.setFmp(BigDecimal.valueOf(2));

        DayShiftPo dayShiftPo = new DayShiftPo();
        dayShiftPo.setDay(DateUtil.parse("2022-10-10"));
        TimePair timePair2 = new TimePair();
        timePair2.setStart(DateUtil.parse("2022-10-10 8:00").getTime());
        timePair2.setEnd(DateUtil.parse("2022-10-10 12:00").getTime());
        dayShiftPo.setTimePairs(Collections.singletonList(timePair2));
        dayStaffPo.setDayShiftPo(dayShiftPo);


        DayStaffPo dayStaffPo2 = new DayStaffPo();
        dayStaffPo2.setDay(DateUtil.parse("2022-10-10"));
        dayStaffPo2.setCasUserId(20L);
        dayStaffPo2.setFmp(BigDecimal.valueOf(3));

        DayShiftPo dayShiftPo2 = new DayShiftPo();
        dayShiftPo2.setDay(DateUtil.parse("2022-10-10"));
        TimePair timePair3 = new TimePair();
        timePair3.setStart(DateUtil.parse("2022-10-10 8:00").getTime());
        timePair3.setEnd(DateUtil.parse("2022-10-10 12:00").getTime());
        dayShiftPo2.setTimePairs(Collections.singletonList(timePair3));
        dayStaffPo2.setDayShiftPo(dayShiftPo2);

        constraintVerifier.verifyThat(TableConstraintProvider::shiftNeedFmtGapV2).given(dayTimeSoltPo,dayShiftPo,dayShiftPo2)
                .penalizesBy(50);
    }

}
