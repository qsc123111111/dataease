package io.dataease.controller.datamodel.enums;

public enum DatamodelEnum {
    //普通文件夹
    NORMAL_DIR(0),
    //模型文件夹
    MODEL_DIR(1);
    private final int value;

    DatamodelEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
