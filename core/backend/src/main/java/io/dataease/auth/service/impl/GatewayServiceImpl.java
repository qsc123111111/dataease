package io.dataease.auth.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import io.dataease.auth.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// 不认证
//@Service
@Slf4j
public class GatewayServiceImpl implements GatewayService {
    @Value("${gateway.url}")
    private String GATEWAY_URL;
    @Value("${getToken.path}")
    private String GETTOKEN_PATH;
    @Value("${getMenuAndRoles.path}")
    private String GETMENUANDROLES_PATH;
    @Override
    public JSONObject getUserInfoByToken(String token) {
        log.info("feign获取地址:{}",GATEWAY_URL + GETMENUANDROLES_PATH);
        //从feign获取用户信息
        String resultBody = HttpUtil.createGet(GATEWAY_URL + GETMENUANDROLES_PATH)
            .header("Authorization", token)
            .execute()
            .body();
        log.debug("从feign获取用户信息{}", resultBody);
        JSONObject menuAndRoles = new JSONObject(resultBody);
        /**
         * {
         * 	role: [{
         * 		        id: “123”,
         * 		    name: “超级管理员”,
         * 		        description: “超级管理员”,
         * 	        ...
         *        }],
         * 	menu: [{
         * 		id: “123”,
         * 		name: “数据服务”,
         * 		        href: “xxx”,
         * 		iconName: “123”,
         * 	        ...,
         * 		childs: [{
         * 			id: “111”,
         * 	            ...,
         * 			            childs: [{}, {}, ...]
         *        },
         *  {}
         * ]
         *        }],
         * 	    user: {
         * 		id: “123”,
         * 		        userName: “superAdmin”,
         * 		email: “”,
         * ...
         *        }
         * }
         */
        return menuAndRoles;
    }
}

// 不认证
