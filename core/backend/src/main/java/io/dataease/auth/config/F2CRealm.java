package io.dataease.auth.config;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.entity.ASKToken;
import io.dataease.auth.entity.JWTToken;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.handler.ApiKeyHandler;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.service.GatewayService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.*;
import io.dataease.controller.sys.request.SysUserCreateRequest;
import io.dataease.dto.LoginVo;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.DeCorrespAuth;
import io.dataease.plugins.common.base.domain.SysUser;
import io.dataease.plugins.common.base.mapper.DeCorrespAuthMapper;
import io.dataease.service.sys.SysUserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class F2CRealm extends AuthorizingRealm {

    @Autowired
    @Lazy // shiro组件加载过早 让authUserService等一等再注入 否则 注入的可能不是代理对象
    private AuthUserService authUserService;

    @Autowired
    @Lazy // shiro组件加载过早 让authUserService等一等再注入 否则 注入的可能不是代理对象
    private SysUserService sysUserService;

    @Autowired
    @Lazy
    private GatewayService gatewayService;

    private static ReentrantLock lock = new ReentrantLock();

    @Resource
    @Lazy
    private DeCorrespAuthMapper deCorrespAuthMapper;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken || token instanceof ASKToken;
    }

    // 验证资源权限
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        CurrentUserDto userDto = (CurrentUserDto) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        Set<String> role = new HashSet<>(
                userDto.getRoles().stream().map(item -> (item.getId() + "")).collect(Collectors.toSet()));
        simpleAuthorizationInfo.addRoles(role);
        Set<String> permission = new HashSet<>(userDto.getPermissions());
        simpleAuthorizationInfo.addStringPermissions(permission);
        return simpleAuthorizationInfo;
    }

    // 验证登录权限

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        TokenInfo tokenInfo;
        String token;
        try {
            token = (String) auth.getCredentials();
            if (!token.startsWith("Bearer")){
                return nativeRealm(auth);
            }
            // 解密获得username，用于和数据库进行对比
            JSONObject userInfoByToken = gatewayService.getUserInfoByToken(token);
            if (userInfoByToken.containsKey("fail")){
                JSONObject fail = userInfoByToken.getJSONObject("fail");
                String errMsg = fail.getStr("errMsg");
                throw new AuthenticationException("token invalid:" + errMsg);
            } else if (userInfoByToken.containsKey("code")) {
                if (userInfoByToken.getInt("code") == 401 ){
                    throw new AuthenticationException("token invalid:" + userInfoByToken.getStr("message"));
                }
                throw new AuthenticationException("token invalid:" + userInfoByToken.getStr("message"));
            } else {
                JSONObject menuAndRoles = new JSONObject(userInfoByToken);
                JSONObject user = menuAndRoles.getJSONObject("user");
                String authId = user.getStr("id");
                String userName = user.getStr("userName");
                JSONArray role = menuAndRoles.getJSONArray("role");
                JSONObject roleInfo = role.getJSONObject(0);
//                if ("超级管理员".equals(roleInfo.get("name"))){
//                    //超级管理员  返回userId 1
//                    SysUserEntity sysUser = userWithId(1L);
//                    CurrentUserDto currentUserDto = queryCacheUserDto(sysUser);
//                    return new SimpleAuthenticationInfo(currentUserDto, token, "f2cReam");
//                } else {
//                    //普通人员
//                    return simpleUser(token, authId, userName);
//                }
                return simpleUser(token, authId, userName, roleInfo.get("name").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthenticationException(e);
        }
    }

    @NotNull
    private SimpleAuthenticationInfo simpleUser(String token, String authId, String userName,String roleName) throws InterruptedException {
        if (lock.tryLock()){
            try {
                DeCorrespAuth deCorrespAuth = deCorrespAuthMapper.selectByAuthId(authId);
                if (ObjectUtils.isEmpty(deCorrespAuth)){
                    //此前没有此用户
                    deCorrespAuth = new DeCorrespAuth();
                    deCorrespAuth.setAuthId(authId);
                    //在系统注册此用户
                    SysUserCreateRequest request = new SysUserCreateRequest();
                    request.setUsername(userName);
                    request.setNickName(userName);
                    request.setGender("男");
                    request.setEmail(authId + "@qq.com");
                    request.setEnabled(1L);
                    request.setPhonePrefix("+86");
                    request.setRoleIds(Arrays.asList(1L));
                    //request.setUserId(Long.valueOf(id));
                    Long save = sysUserService.saveAuth(request);
                    deCorrespAuth.setUserId(save);
                    if ("超级管理员".equals(roleName)){
                        deCorrespAuth.setIsAdmin(Boolean.TRUE);
                    } else {
                        deCorrespAuth.setIsAdmin(Boolean.FALSE);
                    }
                    deCorrespAuthMapper.insert(deCorrespAuth);
                    SysUserEntity sysUser = userWithId(save);
                    CurrentUserDto currentUserDto = queryCacheUserDto(sysUser);
                    return new SimpleAuthenticationInfo(currentUserDto, token, "f2cReam");
                } else {
                    //此前已存在此用户
                    //查询用户名
                    SysUserEntity sysUser = userWithId(Long.valueOf(deCorrespAuth.getUserId()));
                    CurrentUserDto currentUserDto = queryCacheUserDto(sysUser);
                    return new SimpleAuthenticationInfo(currentUserDto, token, "f2cReam");
                }
            } finally {
                lock.unlock();
            }
        } else {
            Thread.sleep(100);
            return simpleUser(token, authId, userName, roleName);
        }

    }

    public AuthenticationInfo nativeRealm(AuthenticationToken auth) throws AuthenticationException {
        if (auth instanceof ASKToken) {
            if (!authUserService.pluginLoaded()) {
                throw new AuthenticationException("license error");
            }

            Object accessKey = auth.getPrincipal();
            Object signature = auth.getCredentials();
            Long userId = ApiKeyHandler.getUser(accessKey.toString(), signature.toString());

            SysUserEntity userEntity = userWithId(userId);
            CurrentUserDto currentUserDto = queryCacheUserDto(userEntity);
            return new SimpleAuthenticationInfo(currentUserDto, signature, "f2cReam");
        }

        try {
            CacheUtils.get("lic_info", "lic");
        } catch (Exception e) {
            LogUtil.error(e);
            throw new AuthenticationException("license error");
        }

        TokenInfo tokenInfo;
        String token;
        try {
            token = (String) auth.getCredentials();
            // 解密获得username，用于和数据库进行对比
            tokenInfo = JWTUtils.tokenInfoByToken(token);
            if (TokenCacheUtils.invalid(token)) {
                throw new AuthenticationException("token invalid");
            }
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }

        Long userId = tokenInfo.getUserId();
        String username = tokenInfo.getUsername();
        if (username == null) {
            throw new AuthenticationException("token invalid");
        }

        SysUserEntity user = userWithId(userId);
        String pass = null;
        try {
            pass = user.getPassword();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!JWTUtils.verify(token, tokenInfo, pass)) {
            throw new AuthenticationException("Username or password error");
        }

        CurrentUserDto currentUserDto = queryCacheUserDto(user);
        return new SimpleAuthenticationInfo(currentUserDto, token, "f2cReam");
    }

    //@Override
    //protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
    //
    //    if (auth instanceof ASKToken) {
    //        if (!authUserService.pluginLoaded()) {
    //            throw new AuthenticationException("license error");
    //        }
    //
    //        Object accessKey = auth.getPrincipal();
    //        Object signature = auth.getCredentials();
    //        Long userId = ApiKeyHandler.getUser(accessKey.toString(), signature.toString());
    //
    //        SysUserEntity userEntity = userWithId(userId);
    //        CurrentUserDto currentUserDto = queryCacheUserDto(userEntity);
    //        return new SimpleAuthenticationInfo(currentUserDto, signature, "f2cReam");
    //    }
    //
    //    try {
    //        CacheUtils.get("lic_info", "lic");
    //    } catch (Exception e) {
    //        LogUtil.error(e);
    //        throw new AuthenticationException("license error");
    //    }
    //
    //    TokenInfo tokenInfo;
    //    String token;
    //    try {
    //        token = (String) auth.getCredentials();
    //        // 解密获得username，用于和数据库进行对比
    //        tokenInfo = JWTUtils.tokenInfoByToken(token);
    //        if (TokenCacheUtils.invalid(token)) {
    //            throw new AuthenticationException("token invalid");
    //        }
    //    } catch (Exception e) {
    //        throw new AuthenticationException(e);
    //    }
    //
    //    Long userId = tokenInfo.getUserId();
    //    String username = tokenInfo.getUsername();
    //    if (username == null) {
    //        throw new AuthenticationException("token invalid");
    //    }
    //
    //    SysUserEntity user = userWithId(userId);
    //    String pass = null;
    //    try {
    //        pass = user.getPassword();
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //    if (!JWTUtils.verify(token, tokenInfo, pass)) {
    //        throw new AuthenticationException("Username or password error");
    //    }
    //
    //    CurrentUserDto currentUserDto = queryCacheUserDto(user);
    //    return new SimpleAuthenticationInfo(currentUserDto, token, "f2cReam");
    //}

    public SysUserEntity userWithId(Long userId) {
        SysUserEntity user = authUserService.getUserById(userId);
        if (user == null) {
            throw new AuthenticationException("User didn't existed!");
        }
        if (user.getEnabled() == 0) {
            throw new AuthenticationException("User is valid!");
        }
        return user;
    }
    //获取角色权限
    public CurrentUserDto queryCacheUserDto(SysUserEntity user) {
        // 使用缓存
        List<CurrentRoleDto> currentRoleDtos = authUserService.roleInfos(user.getUserId());
        // 使用缓存
        List<String> permissions = authUserService.permissions(user.getUserId());
        CurrentUserDto currentUserDto = BeanUtils.copyBean(new CurrentUserDto(), user);
        currentUserDto.setRoles(currentRoleDtos);
        currentUserDto.setPermissions(permissions);
        return currentUserDto;
    }
}
