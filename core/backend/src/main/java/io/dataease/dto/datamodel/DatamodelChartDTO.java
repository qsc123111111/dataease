package io.dataease.dto.datamodel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatamodelChartDTO {
    private TreeSet<String> labels;
    private HashMap<String,List<DatamodelLabelRefDTO>> rules;
}
