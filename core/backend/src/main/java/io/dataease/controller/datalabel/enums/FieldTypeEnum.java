package io.dataease.controller.datalabel.enums;
//字段类型：1文本 2数值
public enum FieldTypeEnum {
    /**
     * 文本
     */
    TEXT(1),
    /**
     * 数值
     */
    NUMBER(2);
    private final int value;
    FieldTypeEnum(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
}
