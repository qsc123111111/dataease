package io.dataease.controller.datamodel.enums;

public enum DatamodelStatusEnum {
    //进行中
    DOING(0),
    //完成
    Done(1),
    //错误
    ERROR(2);
    private final int value;

    DatamodelStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}