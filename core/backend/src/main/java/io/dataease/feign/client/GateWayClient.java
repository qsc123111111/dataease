//package io.dataease.feign.client;
//
//import cn.hutool.json.JSONObject;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//@FeignClient(value = "${feign.gateway.name}")
//public interface GateWayClient {
//    @PostMapping("/auth/oauth/token")
//    JSONObject login(   @RequestParam(value = "username") String username,
//                        @RequestParam(value = "password") String password,
//                        @RequestParam(value = "grant_type", defaultValue = "password") String grant_type,
//                        @RequestParam(value = "client_id", defaultValue = "client-app") String client_id,
//                        @RequestParam(value = "client_secret", defaultValue = "123456") String client_secret);
//}
