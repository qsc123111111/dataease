package io.dataease.dto.datamodel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatamodelLabelRefDTO {
    private String datasetLabel;
    private String labelRef;
}
