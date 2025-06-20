package io.dataease.controller.dataobject.enums;

public enum ObjectPeriodEnum {
    OBJECT(1),
    MODEL(2);
    private final int value;
    ObjectPeriodEnum(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
}
