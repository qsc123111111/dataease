package io.dataease.auth.service;

import cn.hutool.json.JSONObject;

public interface GatewayService {
    JSONObject getUserInfoByToken(String token);
}
