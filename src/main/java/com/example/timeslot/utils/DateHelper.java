package com.example.timeslot.utils;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import org.springframework.util.CollectionUtils;

import java.time.DateTimeException;
import java.util.*;

/**
 * @author：peng-wang-12
 * @date: 11/24/22
 */
public class DateHelper {


    /**
     * 是否连续
     *
     * @param timePairList
     * @return
     */
    public static boolean isContinuous(List<TimePair> timePairList) {
        if (CollectionUtils.isEmpty(timePairList)) {
            return false;
        }
        TimePair[] timePairs = new TimePair[timePairList.size()];
        for (int i = 0; i < timePairList.size(); i++) {
            timePairs[i] = timePairList.get(i);
        }
        Arrays.sort(timePairs, Comparator.comparingLong(TimePair::getStart));

        for (int i = 1; i < timePairs.length; i++) {
            if (!(timePairs[i - 1].getEnd() == timePairs[i].getStart())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOverlap(List<TimePair> timePairs, boolean isStrict) {
        if (CollectionUtils.isEmpty(timePairs)) {
            return false;
        }
        TimePair[] timePairs1 = new TimePair[timePairs.size()];
        for (int i = 0; i < timePairs.size(); i++) {
            timePairs1[i] = timePairs.get(i);
        }
        return isOverlap(timePairs1, isStrict);
    }

    /**
     * 判断多个时间段是否有重叠（交集）
     *
     * @param timePairs 时间段数组
     * @param isStrict  是否严格重叠，true 严格，没有任何相交或相等；false 不严格，可以首尾相等，比如2021-05-29到2021-05-31和2021-05-31到2021-06-01，不重叠。
     * @return 返回是否重叠
     */
    public static boolean isOverlap(TimePair[] timePairs, boolean isStrict) {
        if (timePairs == null || timePairs.length == 0) {
            throw new DateTimeException("timePairs不能为空");
        }

        Arrays.sort(timePairs, Comparator.comparingLong(TimePair::getStart));

        for (int i = 1; i < timePairs.length; i++) {
            if (isStrict) {
                if (!(timePairs[i - 1].getEnd() < timePairs[i].getStart())) {
                    return true;
                }
            } else {
                if (!(timePairs[i - 1].getEnd() <= timePairs[i].getStart())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Date convertDate(Integer dayOffset, Integer hour, Integer min) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 00);
        calendar.add(Calendar.DAY_OF_MONTH, dayOffset);
        return calendar.getTime();
    }

    public static Date convertDate(Date date,Integer dayOffset, Integer hour, Integer min) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 00);
        calendar.add(Calendar.DAY_OF_MONTH, dayOffset);
        return calendar.getTime();
    }


    public static List<String> getBetweenDays(Date startTime, Date endTime) {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        while (!calendar.getTime().after(endTime)) {
            dates.add(DateUtil.formatDate(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }

    /**
     * 计算时段内 占用半小时的当天偏移量集合
     * 如  8:00~9:00, 计算结果为 16,17,18
     * @param start
     * @param end
     * @return
     */
    public static Set<Long> computeHalfHourOffsetSet(Date start, Date end) {
        if (start == null || end == null || end.before(start)) {
            return null;
        }
        Date date = DateUtil.beginOfDay(start);
        long offsetStart = (start.getTime() - date.getTime()) / (DateUnit.MINUTE.getMillis() * 30);
        long offsetEnd = (end.getTime() - date.getTime()) / (DateUnit.MINUTE.getMillis() * 30);
        Set<Long> set = new HashSet<>();
        while (offsetStart <= offsetEnd) {
            set.add(offsetStart);
            offsetStart++;
        }
        return set;

    }



    public static Date addMins(Date beginOfDay, int mins) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginOfDay);
        calendar.add(Calendar.MINUTE, mins);
        return calendar.getTime();
    }
}
