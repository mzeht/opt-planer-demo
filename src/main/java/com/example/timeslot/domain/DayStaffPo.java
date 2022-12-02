package com.example.timeslot.domain;

import cn.hutool.core.date.DateUtil;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author：peng-wang-12
 * @date: 11/29/22
 */
@PlanningEntity
public class DayStaffPo {

    private Long casUserId;

    private Date joinTime;

    /**
     * 日期
     */
    private Date day;

    /**
     * 是否为带教
     */
    private Boolean isTeacher;

    /**
     * 该日期下人员的成熟人力值
     */
    private BigDecimal fmp;

    @PlanningVariable(valueRangeProviderRefs = "dayShiftRange")
    private DayShiftPo dayShiftPo;

    public Long getCasUserId() {
        return casUserId;
    }

    public void setCasUserId(Long casUserId) {
        this.casUserId = casUserId;
    }

    public Date getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Date joinTime) {
        this.joinTime = joinTime;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public BigDecimal getFmp() {
        return fmp;
    }

    public void setFmp(BigDecimal fmp) {
        this.fmp = fmp;
    }

//    @PlanningVariable(valueRangeProviderRefs = "dayShiftRange")
    public DayShiftPo getDayShiftPo() {
        return dayShiftPo;
    }

    public void setDayShiftPo(DayShiftPo dayShiftPo) {
        this.dayShiftPo = dayShiftPo;
    }

    public Boolean getTeacher() {
        return isTeacher;
    }

    public void setTeacher(Boolean teacher) {
        isTeacher = teacher;
    }


    public String dayShiftIdKey() {
        return String.join(",", DateUtil.formatDate(day), getDayShiftPo().getShiftId().toString());
    }
}
