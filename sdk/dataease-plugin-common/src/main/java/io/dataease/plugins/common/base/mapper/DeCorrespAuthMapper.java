package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DeCorrespAuth;

public interface DeCorrespAuthMapper {
    DeCorrespAuth selectByUserId(Long userId);
    DeCorrespAuth selectByAuthId(String authId);

    int insert(DeCorrespAuth deCorrespAuth);

    int deleteByUserId(Long userId);
}
