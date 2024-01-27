package io.dataease.dto.authModel;

public enum modelCacheEnum {
    modeltree("modeltree");
    private final String value;
    modelCacheEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
