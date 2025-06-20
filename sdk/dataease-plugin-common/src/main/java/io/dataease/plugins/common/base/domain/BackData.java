package io.dataease.plugins.common.base.domain;

import lombok.Data;

import java.util.List;

@Data
public class BackData {
    private String notation;
    private List<String> range;
    private String tag;
    private String type;
}
