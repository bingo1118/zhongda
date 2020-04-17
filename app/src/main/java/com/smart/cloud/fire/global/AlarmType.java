package com.smart.cloud.fire.global;

public class AlarmType {

    public int alarmCode;
    public String alarmName;

    public AlarmType() {

    }

    public AlarmType(int alarmCode, String alarmName) {
        this.alarmCode = alarmCode;
        this.alarmName = alarmName;
    }

    public int getAlarmCode() {
        return alarmCode;
    }

    public void setAlarmCode(int alarmCode) {
        this.alarmCode = alarmCode;
    }

    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

}
