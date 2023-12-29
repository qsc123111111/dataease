package io.dataease.controller.datamodel.enums;

public enum DatamodelUpDownEnum {
    //下架
    DOWN(0),
    //上架
    UP(1);
    private final int value;

    public int getValue() {
        return value;
    }

    DatamodelUpDownEnum(int value) {
        this.value = value;
    }
}
