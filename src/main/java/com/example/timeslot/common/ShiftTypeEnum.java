package com.example.timeslot.common;

import java.util.Arrays;

/**
 * @author：peng-wang-12
 * @date: 11/8/22
 */
public enum ShiftTypeEnum {

    DAY(1,"白班"),
    NIGHT(2,"夜班"),
    DAY_NIGHT(3,"天地班");

    private int code;

    private String desc;

    ShiftTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ShiftTypeEnum findByCode(int code){
        return Arrays.stream(ShiftTypeEnum.values()).filter(t -> t.getCode() == code).findAny().orElse(null);
    }
}
