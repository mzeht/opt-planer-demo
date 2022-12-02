package com.example.timeslot.rest;

import cn.hutool.core.date.DateUtil;
import com.example.timeslot.domain.DayStaffPo;
import com.example.timeslot.domain.TableSolution;
//import com.example.timeslot.domain.TimeTable;
import com.example.timeslot.service.PlanService;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @program: timeslot
 * @description: ${description}
 * @author: qing.ye
 * @create: 2021-06-01 10:30
 **/
@RestController
@RequestMapping("/timeTable")
public class TimeTableController {
//
//    @Autowired
//    private SolverManager<TimeTable, UUID> solverManager;

    @Autowired
    private SolverManager<TableSolution, UUID> solverManager2;

    @Autowired
    ScoreManager scoreManager;

    @Autowired
    PlanService planService;
//
//    @PostMapping("/solve")
//    public TimeTable solve(@RequestBody TimeTable problem) {
//        UUID problemId = UUID.randomUUID();
//        // Submit the problem to start solving
//        SolverJob<TimeTable, UUID> solverJob = solverManager.solve(problemId, problem);
//        TimeTable solution;
//        try {
//            // Wait until the solving ends
//            solution = solverJob.getFinalBestSolution();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new IllegalStateException("Solving failed.", e);
//        }
//        return solution;
//    }

    @PostMapping("/solve2")
    public void solve2() {
        UUID problemId = UUID.randomUUID();
        // Submit the problem to start solving
        TableSolution tableSolution = planService.buildTableSolution();
        SolverJob<TableSolution, UUID> solverJob = solverManager2.solve(problemId, tableSolution);
        System.out.println("");
        TableSolution solution;
        try {
            // Wait until the solving ends
            solution = solverJob.getFinalBestSolution();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Solving failed.", e);
        }
        System.out.println("solve2 end");
        List<DayStaffPo> dayStaffPos = solution.getDayStaffPoList().stream()
                .filter(dayStaffPo -> DateUtil.formatDate(dayStaffPo.getDay()).equals("2022-10-10"))
                .filter(dayStaffPo -> dayStaffPo.getDayShiftPo().getShiftId() != 0)
                .collect(Collectors.toList());
        System.out.println(dayStaffPos.size());
        System.out.println(solution.getScore());
        System.out.println(scoreManager.explainScore(solution));

    }


}
