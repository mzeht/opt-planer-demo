package com.example.timeslot.domain;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.BooleanUtil;
import com.example.timeslot.common.ShiftTypeEnum;
import com.example.timeslot.utils.DateHelper;
import com.example.timeslot.utils.TimePair;
import lombok.extern.slf4j.Slf4j;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author：peng-wang-12
 * @date: 11/29/22
 */
@Slf4j
public class TableConstraintProvider implements ConstraintProvider {

    private static final List<ShiftTypeEnum> AFTER_REST_FIRST_WORK_NOTON_SHIFTTYPES = new ArrayList<>();

    private static final List<ShiftTypeEnum> TEACHER_NOTON_SHIFTTYPES = new ArrayList<>();

    /**
     * 连续工作天数不超过 i天
     */
    private static final int MAX_WORK_RANGE = 6;

    /**
     * 连续休息天数尽量 为i天
     */
    private static final int BEST_REST_RANGE = 2;

    /**
     * 两个班次间休息时间大于等于12H
     */
    private static final int SHIFT_GAP_HOUR = 12;

    public static final BigDecimal gap = new BigDecimal("0.22").setScale(2,RoundingMode.HALF_UP);

    static {
        AFTER_REST_FIRST_WORK_NOTON_SHIFTTYPES.add(ShiftTypeEnum.NIGHT);
        AFTER_REST_FIRST_WORK_NOTON_SHIFTTYPES.add(ShiftTypeEnum.DAY_NIGHT);
        TEACHER_NOTON_SHIFTTYPES.add(ShiftTypeEnum.NIGHT);
        TEACHER_NOTON_SHIFTTYPES.add(ShiftTypeEnum.DAY_NIGHT);
    }


    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {

        return new Constraint[]{
                //硬约束------------------------------------------------------------------------------------------

                //班次要安排人
                shiftNeedPlan(constraintFactory),
//                oneDayStaffShiftLimit(constraintFactory),
                //连续工作天数不超过 i天
//                workContinuousLengthLimit(constraintFactory, MAX_WORK_RANGE),
                //带教不排夜班 天地班
//                teacherNotShift(constraintFactory, TEACHER_NOTON_SHIFTTYPES),

                //软约束------------------------------------------------------------------------------------------
                //班次尽量满足预测人力值，计算差值占比的平方，进行扣分
                shiftNeedFmtGapV3(constraintFactory),
                //客服和班次的日期要一致
                shiftDayStaffDaySame(constraintFactory),
//                //连续休息天数尽量 为i天，违反一次扣一分
//                dayRestRangeLimit(constraintFactory, BEST_REST_RANGE),
//                //休息后排班次：夜班，天地班，违反一次扣一分
//                afterRestWorkLimit(constraintFactory, AFTER_REST_FIRST_WORK_NOTON_SHIFTTYPES),
//                //两个班次间休息时间大于等于12H，违反一次扣一分
//                shiftGapLimit(constraintFactory, SHIFT_GAP_HOUR, DateUnit.HOUR)

        };
    }

    private Constraint shiftNeedPlan(ConstraintFactory constraintFactory) {
        return constraintFactory.from(DayShiftPo.class)
                .filter(dayShiftPo -> dayShiftPo.getShiftId() != 0)
                .join(DayStaffPo.class, Joiners.equal(DayShiftPo::dayShiftIdKey, DayStaffPo::dayShiftIdKey))
                .groupBy((dayShiftPo, dayStaffPo) -> dayShiftPo, ConstraintCollectors.countBi())
                .filter((dayShiftPo, integer) -> integer <= 0)
                .penalize("shiftNeedPlan", HardSoftScore.ONE_HARD);
//                .penalize("shiftNeedPlan",HardSoftScore.ONE_HARD,)
    }

//    Constraint shiftNeedFmtGap(ConstraintFactory constraintFactory) {
//        //某个班次
//        return constraintFactory.from(DayShiftPo.class)
//                .filter(dayShiftPo -> dayShiftPo.getShiftId() != 0)//排班
//                .join(DayStaffPo.class, Joiners.equal(DayShiftPo::dayShiftIdKey, DayStaffPo::dayShiftIdKey))
//                .groupBy((shiftPo, staffPo) -> shiftPo, ConstraintCollectors.sumBigDecimal((shiftpo, staffpo) -> staffpo.getFmp()))
//                .filter((shiftPo, totalStaffFmp) -> shiftPo.getNeedFmp().compareTo(totalStaffFmp) != 0)
//                .penalizeBigDecimal("shiftNeedFmtGap", HardSoftScore.ONE_SOFT, (shiftPo, totalStaffFmp) -> computeShiftFmtGap(shiftPo.getNeedFmp(), totalStaffFmp));
//
//    }

    public Constraint shiftNeedFmtGapV2(ConstraintFactory constraintFactory) {
        //某天的一个预测时段
        return constraintFactory.from(DayTimeSoltPo.class)
                // 连接 日客服信息 by 日预测时段 和日客服班次时段有重叠且为同一天
                .join(DayStaffPo.class, Joiners.filtering((dayTimeSoltPo, dayStaffPo) -> isOverlap(dayTimeSoltPo, dayStaffPo)))
                // 统计日预测时段内 客服fmp总和
                .groupBy((dayTimeSoltPo, dayStaffPo) -> dayTimeSoltPo, ConstraintCollectors.sumBigDecimal((dayTimeSoltPo1, dayStaffPo1) -> dayStaffPo1.getFmp()))
                // 计算差距值
                .penalizeBigDecimal("shiftNeedFmtGapV2", HardSoftScore.ONE_SOFT, (dayTimeSoltPo, totalFmp) -> {
                    return computeShiftFmtGapV2(dayTimeSoltPo, totalFmp);
                });
    }

    public Constraint shiftNeedFmtGapV3(ConstraintFactory constraintFactory) {
        //某天的一个预测时段
        return constraintFactory.from(DayTimeSoltPo.class)
                // 连接 日客服信息 by 日预测时段 和日客服班次时段有重叠且为同一天
                .join(DayStaffPo.class, Joiners.filtering((dayTimeSoltPo, dayStaffPo) -> isOverlap(dayTimeSoltPo, dayStaffPo)))
                // 统计日预测时段内 客服fmp总和
                .groupBy((dayTimeSoltPo, dayStaffPo) -> dayTimeSoltPo, ConstraintCollectors.sumBigDecimal((dayTimeSoltPo1, dayStaffPo1) -> dayStaffPo1.getFmp()))
                // 计算差距值
                .filter((dayTimeSoltPo, bigDecimal) -> computeShiftFmtGapV2(dayTimeSoltPo, bigDecimal).compareTo(gap)>0)
                .penalize("shiftNeedFmtGapV3", HardSoftScore.ONE_SOFT,((dayTimeSoltPo, bigDecimal) -> computeShiftFmtGapV2(dayTimeSoltPo, bigDecimal).intValue() ));
    }


    private static BigDecimal computeShiftFmtGapV1(DayTimeSoltPo dayTimeSoltPo, BigDecimal totalStaffFmp) {
        try {
            BigDecimal gap = dayTimeSoltPo.getNeedFmp().subtract(totalStaffFmp).abs();
            BigDecimal n = gap.setScale(2, RoundingMode.HALF_DOWN);
            log.info("computeShiftFmtGap:need={},planed={},gapScore={}", dayTimeSoltPo.getNeedFmp(), totalStaffFmp, n);
            return n;
        } catch (Exception e) {
            log.error("computeShiftFmtGap error {},{}", dayTimeSoltPo.getNeedFmp(), totalStaffFmp, e);
            return BigDecimal.ZERO;
        }
    }

    private static BigDecimal computeShiftFmtGapV4(DayTimeSoltPo dayTimeSoltPo, BigDecimal totalStaffFmp) {
        try {
            BigDecimal gap = dayTimeSoltPo.getNeedFmp().subtract(totalStaffFmp).abs();
            BigDecimal n = gap.setScale(2, RoundingMode.HALF_DOWN);
            log.info("computeShiftFmtGap:need={},planed={},gapScore={}", dayTimeSoltPo.getNeedFmp(), totalStaffFmp, n);
            return n;
        } catch (Exception e) {
            log.error("computeShiftFmtGap error {},{}", dayTimeSoltPo.getNeedFmp(), totalStaffFmp, e);
            return BigDecimal.ZERO;
        }
    }

    private static BigDecimal computeShiftFmtGapV2(DayTimeSoltPo dayTimeSoltPo, BigDecimal totalStaffFmp) {
        try {
            BigDecimal gap = BigDecimal.valueOf(dayTimeSoltPo.getNeedFmp().subtract(totalStaffFmp).abs().divide(dayTimeSoltPo.getNeedFmp(), 2, RoundingMode.HALF_UP).doubleValue()*100);
            BigDecimal n = gap.setScale(2, RoundingMode.HALF_DOWN);
            log.info("computeShiftFmtGap:need={},planed={},gapScore={}", dayTimeSoltPo.getNeedFmp(), totalStaffFmp, n);
            return n;
        } catch (Exception e) {
            log.error("computeShiftFmtGap error {},{}", dayTimeSoltPo.getNeedFmp(), totalStaffFmp, e);
            return BigDecimal.ZERO;
        }
    }


    private static boolean isOverlap(DayTimeSoltPo dayTimeSoltPo, DayStaffPo dayStaffPo) {
        if (dayStaffPo.getDayShiftPo() == null || dayStaffPo.getDayShiftPo().getShiftId() == 0) {
            return false;
        }
        if (CollectionUtils.isEmpty(dayStaffPo.getDayShiftPo().getTimePairs())) {
            return false;
        }
        if (dayTimeSoltPo.getDay().compareTo(dayStaffPo.getDay()) != 0) {
            return false;
        }
        List<TimePair> timePairs = new ArrayList<>();
        timePairs.add(dayTimeSoltPo.getTimePair());
        timePairs.addAll(dayStaffPo.getDayShiftPo().getTimePairs());
        return DateHelper.isOverlap(timePairs, true);
    }


    /**
     * （（（班次排班人力-班次预测人力）/ 班次预测人力） *100）
     *
     * @param needFmp
     * @param totalStaffFmp
     * @return
     */
    private static BigDecimal computeShiftFmtGap(BigDecimal needFmp, BigDecimal totalStaffFmp) {
//        BigDecimal gap = BigDecimal.valueOf(Math.pow(needFmp.subtract(totalStaffFmp).abs().divide(needFmp).doubleValue() * 100, 2));
        try {
            BigDecimal gap = BigDecimal.valueOf(needFmp.subtract(totalStaffFmp).abs().divide(needFmp, 2, RoundingMode.HALF_UP).doubleValue() * 100);
            BigDecimal n = gap.setScale(2, RoundingMode.HALF_DOWN);
            log.info("computeShiftFmtGap:need={},planed={},gapScore={}", needFmp, totalStaffFmp, n);
            return n;
        } catch (Exception e) {
            log.error("computeShiftFmtGap error {},{}", needFmp, totalStaffFmp, e);
            return BigDecimal.ZERO;
        }

    }

    /**
     * 两个班次间休息时间大于等于12H
     *
     * @param constraintFactory
     * @param i
     * @param dateUnit
     * @return
     */
    private Constraint shiftGapLimit(ConstraintFactory constraintFactory, int i, DateUnit dateUnit) {
        return constraintFactory.from(DayStaffPo.class).filter(dayStaffPo -> dayStaffPo.getDayShiftPo().getShiftId() != 0)//上班
                .groupBy(DayStaffPo::getCasUserId, ConstraintCollectors.toList()).filter((casUserId, dayShiftPos) -> computeShiftGapHard(dayShiftPos, i, dateUnit) > 0)
                .penalize("shiftGapLimit", HardSoftScore.ONE_SOFT, (casUserId, dayShiftPos) -> computeShiftGapHard(dayShiftPos, i, dateUnit));
    }

    /**
     * 班次间隔小于指定时间的情况数
     *
     * @param dayShiftPos
     * @param n
     * @param dateUnit
     * @return
     */
    private int computeShiftGapHard(List<DayStaffPo> dayShiftPos, int n, DateUnit dateUnit) {
        if (CollectionUtils.isEmpty(dayShiftPos)) {
            return 0;
        }
        dayShiftPos = dayShiftPos.stream().sorted(Comparator.comparing(DayStaffPo::getDay)).collect(Collectors.toList());
        int count = 0;
        DayShiftPo preDayShift = null;
        for (int i = 0; i < dayShiftPos.size(); i++) {
            DayShiftPo currentShift = dayShiftPos.get(i).getDayShiftPo();
            if (preDayShift != null && DateUtil.between(new Date(preDayShift.getLastEnd()), new Date(currentShift.getFirstStart()), dateUnit) < n) {
                count++;
            }
            preDayShift = dayShiftPos.get(i).getDayShiftPo();
        }
        if (count > 0) {
            log.info("computeShiftGapHard:{}", count);
        }
        return count;

    }

    /**
     * 客服和班次的日期要一致
     *
     * @param constraintFactory
     * @return
     */
    Constraint shiftDayStaffDaySame(ConstraintFactory constraintFactory) {
        return constraintFactory.from(DayStaffPo.class)
                .filter(dayStaffPo -> dayStaffPo.getDay().compareTo(dayStaffPo.getDayShiftPo().getDay()) != 0)
                .penalize("shiftDayStaffDaySame", HardSoftScore.ONE_HARD);
    }


//    /**
//     * 一个客服一天（4-4）只能有一个班次
//     *
//     * @param constraintFactory
//     * @return
//     */
//    Constraint oneDayStaffShiftLimit(ConstraintFactory constraintFactory) {
//        return constraintFactory.from(DayStaffPo.class)
//                .join(DayStaffPo.class, Joiners.equal(DayStaffPo::getDay), Joiners.equal(DayStaffPo::getCasUserId))
//                .groupBy((dayStaffPo1, dayStaffPo2) -> dayStaffPo1, ConstraintCollectors.countBi())
////                .penalize("oneDayStaffShiftLimit", HardSoftScore.ONE_HARD);
//    }


    /**
     * 连续休息天数尽量 为i天
     *
     * @param constraintFactory
     * @param i
     * @return
     */
    Constraint dayRestRangeLimit(ConstraintFactory constraintFactory, int i) {
        return constraintFactory.from(DayStaffPo.class).filter(dayStaffPo -> dayStaffPo.getDayShiftPo().getShiftId() == 0)//休息
                .groupBy(DayStaffPo::getCasUserId, ConstraintCollectors.toList()).filter((casUserId, dayShiftPos) -> computeDayRangeHard(dayShiftPos, i) > 0)
                .penalize("dayRestRangeLimit", HardSoftScore.ONE_SOFT, (casUserId, dayShiftPos) -> computeDayRangeHard(dayShiftPos, i));

    }

    /**
     * 连续工作天数不超过 i天
     *
     * @param constraintFactory
     * @param i
     * @return
     */
    Constraint workContinuousLengthLimit(ConstraintFactory constraintFactory, int i) {
        return constraintFactory.from(DayStaffPo.class).filter(dayStaffPo -> dayStaffPo.getDayShiftPo().getShiftId() != 0)//排班
                .groupBy(DayStaffPo::getCasUserId, ConstraintCollectors.toList()).filter((casUserId, dayShiftPos) -> computeDayRangeHard(dayShiftPos, i) > 0).penalize("workContinuousLengthLimit", HardSoftScore.ONE_HARD);
    }


    /**
     * 计算一个客服不同天班次中 不符合连续i天的情况数
     *
     * @param dayShiftPos
     * @param i
     * @return
     */
    public static int computeDayRangeHard(List<DayStaffPo> dayShiftPos, int i) {
        if (CollectionUtils.isEmpty(dayShiftPos)) {
            return 0;
        }
        List<Date> dates = dayShiftPos.stream().map(DayStaffPo::getDay).sorted(Date::compareTo).collect(Collectors.toList());
        return doComputeDaysRangeSize(dates, i);

    }

    public static int doComputeDaysRangeSize(List<Date> dates, int i) {
        dates = dates.stream().sorted(Date::compareTo).collect(Collectors.toList());
        List<List<Date>> list = new ArrayList<>();
        for (Date date : dates) {
            boolean handle = false;
            for (List<Date> dateList : list) {
                for (int l = 0; l < dateList.size(); l++) {
                    if (DateUtil.between(dateList.get(l), date, DateUnit.DAY) == 1) {
                        dateList.add(date);
                        handle = true;
                    }
                }
            }
            if (!handle) {
                List<Date> dates1 = new ArrayList<>();
                dates1.add(date);
                list.add(dates1);
            }
        }
        return Math.toIntExact(list.stream().filter(dates1 -> dates1.size() != i).count());
    }

    Constraint teacherNotShift(ConstraintFactory constraintFactory, List<ShiftTypeEnum> shiftTypeEnums) {
        return constraintFactory.from(DayStaffPo.class).filter(dayStaffPo -> BooleanUtil.isTrue(dayStaffPo.getTeacher()))//是带教
                .filter(dayStaffPo -> dayStaffPo.getDayShiftPo().getShiftId() != 0)//是排班
                .groupBy(DayStaffPo::getCasUserId, ConstraintCollectors.toList()).filter((casUserId, dayShiftPos) -> computeTeacherShiftHard(dayShiftPos, shiftTypeEnums) > 0).penalize("teacherNotShift", HardSoftScore.ONE_HARD, (casUserId, dayShiftPos) -> computeTeacherShiftHard(dayShiftPos, shiftTypeEnums));
    }

    /**
     * 带教 有的班别不排
     *
     * @param dayShiftPos
     * @param shiftTypeEnums
     * @return
     */
    private int computeTeacherShiftHard(List<DayStaffPo> dayShiftPos, List<ShiftTypeEnum> shiftTypeEnums) {
        if (CollectionUtils.isEmpty(dayShiftPos)) {
            return 0;
        }
        List<Integer> shiftTypes = shiftTypeEnums.stream().map(ShiftTypeEnum::getCode).collect(Collectors.toList());
        int count = 0;
        for (DayStaffPo dayShiftPo : dayShiftPos) {
            if (shiftTypes.contains(dayShiftPo.getDayShiftPo().getShiftType())) {
                count++;
            }
        }
        return count;
    }

    /**
     * 休息后排班次：不排残酷班（及0点后的班）和夜班，天地班
     *
     * @param constraintFactory
     * @param shiftTypeEnums
     * @return
     */
    Constraint afterRestWorkLimit(ConstraintFactory constraintFactory, List<ShiftTypeEnum> shiftTypeEnums) {
        return constraintFactory.from(DayStaffPo.class)
                .groupBy(DayStaffPo::getCasUserId, ConstraintCollectors.toList()).filter((casUserId, dayShiftPos) -> computeAfterRestWorkHard(dayShiftPos, shiftTypeEnums) > 0)
                .penalize("afterRestWorkLimit", HardSoftScore.ONE_HARD, (casUserId, dayShiftPos) -> computeAfterRestWorkHard(dayShiftPos, shiftTypeEnums));
    }

    /**
     * 休息后一天排班是 夜班，天地班的情况数量
     *
     * @param dayShiftPos
     * @param shiftTypeEnums
     * @return
     */
    private int computeAfterRestWorkHard(List<DayStaffPo> dayShiftPos, List<ShiftTypeEnum> shiftTypeEnums) {
        if (CollectionUtils.isEmpty(dayShiftPos) || shiftTypeEnums.isEmpty()) {
            return 0;
        }
        List<Integer> shiftTypes = shiftTypeEnums.stream().map(ShiftTypeEnum::getCode).collect(Collectors.toList());
        dayShiftPos = dayShiftPos.stream().sorted(Comparator.comparing(DayStaffPo::getDay)).collect(Collectors.toList());
        int count = 0;
        Long preDayShiftId = 1L;
        for (int i = 0; i < dayShiftPos.size(); i++) {
            Long currentShiftId = dayShiftPos.get(i).getDayShiftPo().getShiftId();
            //当天上班 前提休息 且当天是夜班或者天地班
            if (currentShiftId != 1 && preDayShiftId == 0 && shiftTypes.contains(dayShiftPos.get(i).getDayShiftPo().getShiftType())) {
                count++;
            }
            preDayShiftId = dayShiftPos.get(i).getDayShiftPo().getShiftId();
        }
        if (count > 0) {
            log.info("computeAfterRestWorkHard:{}", count);
        }
        return count;
    }


}
