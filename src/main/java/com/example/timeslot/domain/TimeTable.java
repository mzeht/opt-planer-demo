//package com.example.timeslot.domain;
//
//import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
//import org.optaplanner.core.api.domain.solution.PlanningScore;
//import org.optaplanner.core.api.domain.solution.PlanningSolution;
//import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
//import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
//import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
//
//import java.util.List;
//
///**
// * @program: timeslot
// * @description: ${description}
// * @author: qing.ye
// * @create: 2021-06-01 10:19
// **/
//@PlanningSolution
//public class TimeTable {
//
//    /**
//     * 这是一个problem facts（问题事实）的列表，因为它们在解题过程中不会改变
//     */
//    @ValueRangeProvider(id = "timeslotRange")
//    @ProblemFactCollectionProperty
//    private List<Timeslot> timeslotList;
//
//
//    /**
//     * 这是一个problem facts（问题事实）的列表，因为它们在解题过程中不会改变
//     */
//    @ValueRangeProvider(id = "roomRange")
//    @ProblemFactCollectionProperty
//    private List<Room> roomList;
//
//
//    /**
//     * 这是一个planning entities（计划实体）的列表，因为它们在解题过程中会改变
//     */
//    @PlanningEntityCollectionProperty
//    private List<Lesson> lessonList;
//
//    /**
//     * 打分
//     */
//    @PlanningScore
//    private HardSoftScore score;
//
//    private TimeTable() {
//    }
//
//    public TimeTable(List<Timeslot> timeslotList, List<Room> roomList,
//                     List<Lesson> lessonList) {
//        this.timeslotList = timeslotList;
//        this.roomList = roomList;
//        this.lessonList = lessonList;
//    }
//
//    // ********************************
//    // Getters and setters
//    // ********************************
//
//    public List<Timeslot> getTimeslotList() {
//        return timeslotList;
//    }
//
//    public List<Room> getRoomList() {
//        return roomList;
//    }
//
//    public List<Lesson> getLessonList() {
//        return lessonList;
//    }
//
//    public HardSoftScore getScore() {
//        return score;
//    }
//}
