package com.example.timeslot.domain;

import cn.hutool.core.date.DateUtil;
import com.example.timeslot.utils.TimePair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author：peng-wang-12
 * @date: 11/29/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayShiftPo {

//    /**
//     * 特殊班次 表示休息
//     */
//    private boolean isRest;

    /**
     * 日期
     */
    private Date day;

    private Long shiftId;

    private Integer shiftType;

    /**
     * 最早开始时间
     */
    private Long firstStart;

    /**
     * 最晚结束时间
     */
    private Long lastEnd;

    private List<TimePair> timePairs;

    public void setTimePairs(List<TimePair> timePairs) {
        this.timePairs = timePairs;
        for (TimePair timePair : timePairs) {
            if (this.firstStart == null) {
                this.firstStart = timePair.getStart();
            } else {
                this.firstStart = Math.min(this.firstStart, timePair.getStart());
            }

            if (this.lastEnd == null) {
                this.lastEnd = timePair.getEnd();
            } else {
                this.lastEnd = Math.max(this.lastEnd, timePair.getEnd());
            }
        }
    }

    public String dayShiftIdKey() {
        return String.join(",", DateUtil.formatDate(day), shiftId.toString());
    }



}
