package com.example.timeslot.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DateTimeException;
import java.util.Date;
import java.util.Objects;

/**
 * @author：peng-wang-12
 * @date: 11/24/22
 */
@Data
@NoArgsConstructor
public class TimePair{

//    public TimePair(Date start, Date end) {
//        if(end.getTime()<start.getTime()){
//            throw new DateTimeException("end不能小于start");
//        }
//        this.start = start.getTime();
//        this.end = end.getTime();
//    }

    public TimePair(long start, long end) {
        if(end<start){
            throw new DateTimeException("end不能小于start");
        }
        this.start = start;
        this.end = end;
    }
    private long start;

    private long end;

    private Date stratDate;

    private Date endDate;

    public void setStratDate(Date stratDate) {
        this.stratDate = stratDate;
        this.start = stratDate.getTime();
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
        this.end = endDate.getTime();
    }


    public void setStart(long start) {
        this.start = start;
        this.stratDate = new Date(start);

    }

    public void setEnd(long end) {
        this.end = end;
        this.endDate = new Date(end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimePair timePair = (TimePair) o;
        return start == timePair.start && end == timePair.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}

