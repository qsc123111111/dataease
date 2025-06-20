package io.dataease.controller.datalabel.enums;
//数据类型：1维度 2指标
public enum DataTypeEnum {
    /**
     * 维度
     */
    DIMENSION(1),
    /**
     * 指标
     */
    INDICATOR(2);
    private final int value;
    DataTypeEnum(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
}
