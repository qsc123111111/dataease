package io.dataease.dto.datamodel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatamodelChartDTO {
    private List<String> labels;
    private HashMap<String,List<DatamodelLableRefDTO>> data;
}
