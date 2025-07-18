package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.MyPlugin;
import io.dataease.plugins.common.base.domain.MyPluginExample;
import java.util.List;

import io.dataease.plugins.common.request.KeywordRequest;
import org.apache.ibatis.annotations.Param;

public interface MyPluginMapper {
    long countByExample(MyPluginExample example);

    int deleteByExample(MyPluginExample example);

    int deleteByPrimaryKey(Long pluginId);

    int insert(MyPlugin record);

    int insertSelective(MyPlugin record);

    List<MyPlugin> selectByExample(MyPluginExample example);

    MyPlugin selectByPrimaryKey(Long pluginId);

    int updateByExampleSelective(@Param("record") MyPlugin record, @Param("example") MyPluginExample example);

    int updateByExample(@Param("record") MyPlugin record, @Param("example") MyPluginExample example);

    int updateByPrimaryKeySelective(MyPlugin record);

    int updateByPrimaryKey(MyPlugin record);

    List<MyPlugin> findList(KeywordRequest request);
    List<MyPlugin> queryAll();

//    List<String> findNameList(@Param("ids")List<Long> ids);
}