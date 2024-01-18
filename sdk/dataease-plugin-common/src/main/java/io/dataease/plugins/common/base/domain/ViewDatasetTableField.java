package io.dataease.plugins.common.base.domain;

import io.dataease.plugins.common.dto.chart.ChartCustomFilterDTO;
import io.dataease.plugins.common.dto.chart.ChartCustomFilterItemDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ViewDatasetTableField extends DatasetTableField{
    private Boolean disabled;
    private List<ChartCustomFilterItemDTO> filter;
    private Integer index;
    private String logic;
    private String filterType;
    private ArrayList enumChechField;

}
