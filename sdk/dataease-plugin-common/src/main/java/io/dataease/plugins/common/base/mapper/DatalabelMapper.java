package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.Datalabel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (Datalabel)表数据库访问层
 *
 * @author makejava
 * @since 2023-12-01 16:58:37
 */
public interface DatalabelMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Datalabel queryById(Integer id);

    /**
     * 查询指定行数据
     * @param pageNo
     * @param pageSize
     * @param keyWord
     * @param userId
     * @return
     */
    List<Datalabel> queryAllByLimit(@Param("pageNo") Integer pageNo,@Param("pageSize") Integer pageSize,@Param("keyWord") String keyWord,@Param("userId") Long userId);
    List<Datalabel> queryPageAllByLimit(@Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize, @Param("keyWord") String keyWord, @Param("userId") Long userId,@Param("time") Long time,@Param("plusOneTime") Long plusOneTime);

    /**
     * 统计总行数
     *
     * @param datalabel 查询条件
     * @return 总行数
     */
    long count(Datalabel datalabel);

    /**
     * 新增数据
     *
     * @param datalabel 实例对象
     * @return 影响行数
     */
    int insert(Datalabel datalabel);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<Datalabel> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Datalabel> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<Datalabel> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<Datalabel> entities);

    /**
     * 修改数据
     *
     * @param datalabel 实例对象
     * @return 影响行数
     */
    int update(Datalabel datalabel);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id,@Param("createBy") String createBy);

    long simpleCount(@Param("keyWord") String keyWord, @Param("userId") Long userId,@Param("time") Long time,@Param("plusOneTime") Long plusOneTime);

    Integer deleteBatch(@Param("idsText") String idsText,@Param("createBy") String createBy);
    Integer deleteBatchByGroupId(@Param("idsText") String idsText,@Param("createBy") String createBy);

    List<Datalabel> queryIdAndNameAll(String createBy);

    Datalabel queryByName(@Param("name") String name,@Param("userId") String userId);

    Integer deleteByGroupId(@Param("groupId") Integer groupId,@Param("createBy") String createBy);

    List<Datalabel> queryByGroupId(Integer id);
}

