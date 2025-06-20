package io.dataease.plugins.common.base.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 业务系统用户和dataease用户对照表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeCorrespAuth {
    private Long userId;
    private String authId;
    private Boolean isAdmin;
}
