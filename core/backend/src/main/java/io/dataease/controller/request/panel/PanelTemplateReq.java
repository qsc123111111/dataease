package io.dataease.controller.request.panel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PanelTemplateReq {
    @NotNull
    private String id;
    @NotNull
    private String templateType;
    @NotNull
    private String name;
}
