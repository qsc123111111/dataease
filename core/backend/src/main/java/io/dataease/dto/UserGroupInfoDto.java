package io.dataease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupInfoDto {
    /**
     * "description": "组织机构",
     *         "id": "cca75ba11cba48d7913f2ec6edecd1e4",
     *         "name": "组织机构",
     *         "parentId": "0"
     */
    private String id;
    private String parentId;
    private String name;
    private List<UserGroupInfoDto> childs;
    private String description;
}
