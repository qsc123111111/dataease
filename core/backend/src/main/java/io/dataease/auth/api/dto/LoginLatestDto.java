package io.dataease.auth.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author： qsc
 * @date： 2025/5/11 15:52
 * @description：
 */

@Data
public class LoginLatestDto implements Serializable {


    @ApiModelProperty(value = "账号", required = true)
    private String username;

    @ApiModelProperty(value = "系统用户id", required = true)
    private String systemUserId;


}
