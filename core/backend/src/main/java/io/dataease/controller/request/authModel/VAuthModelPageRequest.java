package io.dataease.controller.request.authModel;

import lombok.Data;

@Data
public class VAuthModelPageRequest {
    private Integer page;
    private Integer pageSize;
    private String pid;
    private String name;
    private String desc;
    private String id;
}
