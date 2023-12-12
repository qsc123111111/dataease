package io.dataease.controller.datalabel.request;

import io.dataease.plugins.common.base.domain.DatalabelGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatalabelGroupRequest extends DatalabelGroup {
    List<DatalabelRequest> labels;
}
