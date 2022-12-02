package com.example.timeslot.domain;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

/**
 * @author：peng-wang-12
 * @date: 11/29/22
 */
@PlanningSolution
public class TableSolution {


    /**
     * 每天时段信息
     */
    @ProblemFactCollectionProperty
    private List<DayTimeSoltPo> dayTimeSoltPoList;

    /**
     * 每天班次
     */
    @ValueRangeProvider(id = "dayShiftRange")
    @ProblemFactCollectionProperty
    private List<DayShiftPo> dayShiftPoList;

    /**
     * 每天员工
     */
    @PlanningEntityCollectionProperty
    private List<DayStaffPo> dayStaffPoList;

    /**
     * 评分
     */
    @PlanningScore
    private HardSoftScore score;

//    @ProblemFactCollectionProperty
    public List<DayTimeSoltPo> getDayTimeSoltPoList() {
        return dayTimeSoltPoList;
    }

    public void setDayTimeSoltPoList(List<DayTimeSoltPo> dayTimeSoltPoList) {
        this.dayTimeSoltPoList = dayTimeSoltPoList;
    }

//    @ValueRangeProvider(id = "dayShiftRange")
//    @ProblemFactCollectionProperty
    public List<DayShiftPo> getDayShiftPoList() {
        return dayShiftPoList;
    }

    public void setDayShiftPoList(List<DayShiftPo> dayShiftPoList) {
        this.dayShiftPoList = dayShiftPoList;
    }

//    @PlanningEntityCollectionProperty
    public List<DayStaffPo> getDayStaffPoList() {
        return dayStaffPoList;
    }

    public void setDayStaffPoList(List<DayStaffPo> dayStaffPoList) {
        this.dayStaffPoList = dayStaffPoList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}
