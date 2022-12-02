package com.example.timeslot.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.example.timeslot.domain.*;
import com.example.timeslot.utils.TimePair;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

/**
 * @author：peng-wang-12
 * @date: 12/2/22
 */
@Service
public class PlanService {

    public void planTest() {

//        SolverFactory<TableSolution> solverFactory = SolverFactory.create(new SolverConfig()
//                .withSolutionClass(TableSolution.class)
//                .withEntityClasses(DayStaffPo.class)
//                .withConstraintProviderClass(TableConstraintProvider.class)
//                .withTerminationSpentLimit(Duration.ofMinutes(2)));
//        Solver<TableSolution> solver = solverFactory.buildSolver();
//        TableSolution tableSolution = buildTableSolution();
//        solver.solve(tableSolution);
//        System.out.println("solve end");

    }

    public TableSolution buildTableSolution() {
        TableSolution tableSolution = new TableSolution();
        String filePath = "/Users/wangpeng/Downloads/opt-timeslot-master/src/main/resources/排班测试001.xlsx";
        //每日班次
        tableSolution.setDayShiftPoList(buildDayShiftPoList(filePath));
        //每日时段需求人力信息
        tableSolution.setDayTimeSoltPoList(buildDayTimeSoltPoList(filePath));
        //每日客服
        tableSolution.setDayStaffPoList(buildDayStaffPoList(filePath));
        return tableSolution;
    }

    private List<DayStaffPo> buildDayStaffPoList(String filePath) {
        ExcelReader sheet1Reader = ExcelUtil.getReader(filePath, "每日客服fmp");
        List<Map<String, Object>> mapList = sheet1Reader.readAll();
        Set<String> keys = new HashSet<>(mapList.get(0).keySet());
        keys.remove("员工id");
        List<DayStaffPo> dayStaffPos = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            Long casUserId = (Long) map.get("员工id");
            for (String dateString : keys) {
                Date day = DateUtil.parse(dateString);
                DayStaffPo dayStaffPo = new DayStaffPo();
                dayStaffPo.setDay(day);
                dayStaffPo.setCasUserId(casUserId);
                Double s = (Double) (map.get(dateString));
                BigDecimal b = new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
                dayStaffPo.setFmp(b);
                dayStaffPos.add(dayStaffPo);
            }

        }
        return dayStaffPos;
    }

    //每日班次
    private List<DayShiftPo> buildDayShiftPoList(String filePath) {
        ExcelReader sheet1Reader = ExcelUtil.getReader(filePath, "每日班次");
        List<Map<String, Object>> mapList = sheet1Reader.readAll();
        Set<String> keys = new HashSet<>(mapList.get(0).keySet());
        keys.remove("班次id");
        keys.remove("时段");
        keys.remove("班次类型");
        List<DayShiftPo> dayShiftPos = new ArrayList<>();
        for (Map<String, Object> map : mapList) {

            Long shiftId = (Long) map.get("班次id");
            Long shiftType = (Long) map.get("班次类型");
            String timeRange = String.valueOf(map.get("时段"));

            for (String dateString : keys) {
                Date day = DateUtil.parse(dateString);
                DayShiftPo dayShiftPo = new DayShiftPo();
                dayShiftPo.setDay(day);
                dayShiftPo.setShiftId(shiftId);
                if (shiftId == 0) {
                    dayShiftPos.add(dayShiftPo);
                    continue;
                }
                String[] shiftranges = timeRange.split(",");
                List<TimePair> timePairs = new ArrayList<>();
                for (int i = 0; i < shiftranges.length; i++) {
                    TimePair timePair = new TimePair();
                    String rangItem = shiftranges[i];
                    String[] range = rangItem.split("-");
                    String start = range[0];
                    String end = range[1];

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(day);
                    if (start.contains("次日")) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        start = start.replaceAll("次日", "");
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(start.split(":")[0]));
                    calendar.set(Calendar.MINUTE, Integer.valueOf(start.split(":")[1]));
                    timePair.setStart(calendar.getTime().getTime());

                    Calendar calendar2 = Calendar.getInstance();
                    calendar2.setTime(day);
                    if (end.contains("次日")) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        end = end.replaceAll("次日", "");
                    }
                    calendar2.set(Calendar.HOUR_OF_DAY, Integer.valueOf(end.split(":")[0]));
                    calendar2.set(Calendar.MINUTE, Integer.valueOf(end.split(":")[1]));
                    timePair.setEnd(calendar2.getTime().getTime());
                    timePairs.add(timePair);
                }
                dayShiftPo.setTimePairs(timePairs);
                dayShiftPo.setShiftType(Math.toIntExact(shiftType));
                dayShiftPos.add(dayShiftPo);
            }

        }
        return dayShiftPos;


//        String filename = "/test001/dayShiftPos.json";
//        InputStreamReader read = new InputStreamReader(this.getClass().getResourceAsStream(filename)) ;
//        String json= IoUtil.read(read);
//        List<DayShiftPo> list = new ArrayList<>();
//        List<DayShiftPo> dayShiftPos = JSON.parseArray(json, DayShiftPo.class);
//        list.addAll(dayShiftPos);
//
//        List<String> dates = new ArrayList<>();
//        dates.add("2022-10-10");
//        dates.add("2022-10-11");
//        dates.add("2022-10-12");
//        dates.add("2022-10-13");
//        dates.add("2022-10-14");
//        Date jsonDate = dayShiftPos.get(0).getDay();
//        for (int i = 0; i < dates.size(); i++) {
//            int dayoffset = Math.toIntExact(DateUtil.between(DateUtil.parse(dates.get(i)), jsonDate, DateUnit.DAY, false));
//            for (DayShiftPo dayShiftPo : dayShiftPos) {
//                DayShiftPo dayShiftPo1 = new DayShiftPo();
//                BeanUtils.copyProperties(dayShiftPo, dayShiftPo1);
//                dayShiftPo1.setDay(DateUtil.offsetDay(dayShiftPo.getDay(),dayoffset));
//                if (CollectionUtils.isNotEmpty(dayShiftPo1.getTimePairs())) {
//                    int timePairsSize = dayShiftPo1.getTimePairs().size();
//                    for (int j = 0; j < timePairsSize; j++) {
//                        dayShiftPo1.getTimePairs().get(j).setStratDate(DateUtil.offsetDay(dayShiftPo.getTimePairs().get(j).getStratDate(), dayoffset));
//                        dayShiftPo1.getTimePairs().get(j).setEndDate(DateUtil.offsetDay(dayShiftPo.getTimePairs().get(j).getEndDate(), dayoffset));
//                    }
//                }
//                list.addAll(dayShiftPos);
//            }
//        }
//        return list;
    }

    private List<DayTimeSoltPo> buildDayTimeSoltPoList(String filePath) {
        ExcelReader sheet1Reader = ExcelUtil.getReader(filePath, "每日时段需求fmp");
        List<Map<String, Object>> mapList = sheet1Reader.readAll();
        Set<String> keys = new HashSet<>(mapList.get(0).keySet());
        keys.remove("时段");
        List<DayTimeSoltPo> dayTimeSoltPos = new ArrayList<>();
        for (Map<String, Object> map : mapList) {

            String timeRange = String.valueOf(map.get("时段"));
            String[] range = timeRange.split("-");
            String start = range[0];
            String end = range[1];
            for (String dateString : keys) {
                Date day = DateUtil.parse(dateString);
                DayTimeSoltPo dayTimeSoltPo = new DayTimeSoltPo();
                dayTimeSoltPo.setDay(day);
                TimePair timePair = new TimePair();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(day);
                if (start.contains("次日")&& end.contains("次日")){
                    System.out.println("");
                }
                if (start.contains("次日")) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    start = start.replaceAll("次日", "");
                }
                calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(start.split(":")[0]));
                calendar.set(Calendar.MINUTE, Integer.valueOf(start.split(":")[1]));

                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(day);
                if (end.contains("次日")) {
                    calendar2.add(Calendar.DAY_OF_MONTH, 1);
                    end = end.replaceAll("次日", "");
                }
                calendar2.set(Calendar.HOUR_OF_DAY, Integer.valueOf(end.split(":")[0]));
                calendar2.set(Calendar.MINUTE, Integer.valueOf(end.split(":")[1]));
                timePair.setStart(calendar.getTime().getTime());
                timePair.setEnd(calendar2.getTime().getTime());
                dayTimeSoltPo.setTimePair(timePair);
                String s = String.valueOf(map.get(dateString));
                BigDecimal b = new BigDecimal(s).setScale(2, BigDecimal.ROUND_HALF_UP);
                dayTimeSoltPo.setNeedFmp(b);
                dayTimeSoltPos.add(dayTimeSoltPo);
            }
        }
        return dayTimeSoltPos;
    }

}
