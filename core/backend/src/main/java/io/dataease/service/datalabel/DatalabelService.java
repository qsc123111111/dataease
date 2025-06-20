package io.dataease.service.datalabel;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datalabel.enums.DataTypeEnum;
import io.dataease.controller.datalabel.enums.FieldTypeEnum;
import io.dataease.controller.datalabel.request.DatalabelGroupRequest;
import io.dataease.controller.datalabel.request.DatalabelRequest;
import io.dataease.plugins.common.base.domain.Datalabel;
import io.dataease.plugins.common.base.domain.DatalabelGroup;
import io.dataease.plugins.common.base.mapper.DatalabelGroupMapper;
import io.dataease.plugins.common.base.mapper.DatalabelMapper;
import io.dataease.plugins.common.base.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * (Datalabel)表服务实现类
 */
@Service("datalabelService")
public class DatalabelService{
    @Resource
    private DatalabelMapper datalabelMapper;
    @Resource
    private DatalabelGroupMapper datalabelGroupMapper;
    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    public Datalabel queryById(Integer id) {
        return this.datalabelMapper.queryById(id);
    }

    public DatalabelGroup queryGroupById(Integer id) {
        return this.datalabelGroupMapper.selectByPrimaryKey(id);
    }

    /**
     * 分页查询
     *
     * @param pageNo
     * @param pageSize
     * @param time
     * @param keyWord
     * @param numSort
     * @param timeSort
     * @return
     */
    public JSONObject queryByPage(Integer pageNo, Integer pageSize, Long time, String keyWord, String numSort, String timeSort) {
        Long plusOneTime = null;
        if (time != null){
            //将当前的时间戳+1天 作为查询时间范围
            // 将时间戳转换为Instant对象
            ZoneId chinaZone = ZoneId.of("Asia/Shanghai");
            Instant instant = Instant.ofEpochMilli(time);
            // 将Instant对象转换为LocalDate对象
            LocalDate localDate = instant.atZone(chinaZone).toLocalDate();
            LocalDate newDate = localDate.plusDays(1);
            plusOneTime = newDate.atStartOfDay(chinaZone).toInstant().toEpochMilli();
        }
        long total = this.datalabelGroupMapper.simpleCount(keyWord, AuthUtils.getUser().getUserId(),time,plusOneTime);
        String sort="order by ";
        if ("asc".equalsIgnoreCase(numSort) || "desc".equalsIgnoreCase(numSort)){
            sort=sort + "invoke " + numSort + ",";
        }
        if ("asc".equalsIgnoreCase(timeSort) || "desc".equalsIgnoreCase(timeSort)){
            sort=sort + "create_time " + timeSort + ",";
        }
        if (sort.equals("order by ")){
            sort=null;
        } else {
            sort = sort.substring(0,sort.length()-1);
        }
        List<DatalabelGroup> list = datalabelGroupMapper.queryPageAllByLimit(pageNo, pageSize,keyWord, AuthUtils.getUser().getUserId(),time,plusOneTime,sort);
        list.stream().forEach(datalabel -> datalabel.setCreateBy(sysUserMapper.selectNameById(datalabel.getCreateBy())));//查询条件已经是该用户了,此处只用查一次全部赋值就可以了
        JSONObject result = new JSONObject();
        result.put("total", total);
        result.put("data", list);
        return result;
    }

    /**
     * 新增数据
     * @param datalabelGroupRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultHolder insert(DatalabelGroupRequest datalabelGroupRequest) throws Exception {
        if (datalabelGroupRequest.getName() == null) {
            throw new RuntimeException("标签名称不能为空");
        }
        //判断标签分组名称是否存在
        DatalabelGroup datalabelGroupExist = datalabelGroupMapper.queryByName(datalabelGroupRequest.getName(),AuthUtils.getUser().getUserId().toString());
        if (datalabelGroupExist != null){
            throw new Exception("标签名称已存在");
        }
        //新增分组
        DatalabelGroup datalabelGroup = new DatalabelGroup();
        BeanUtils.copyBean(datalabelGroup,datalabelGroupRequest);
        datalabelGroup.setCreateBy(AuthUtils.getUser().getUserId().toString());
        datalabelGroup.setCreateTime(System.currentTimeMillis());
        datalabelGroup.setUpdateTime(System.currentTimeMillis());
        datalabelGroup.setExpression(JSON.toJSONString(datalabelGroupRequest.getExpression()));
        int insert = datalabelGroupMapper.insert(datalabelGroup);
        Integer groupId = datalabelGroup.getId();
        datalabelGroupRequest.getLabels().forEach(datalabelRequest -> {
            setDefult(datalabelRequest);
            Datalabel datalabel = new Datalabel(true);
            datalabel.setCreateBy(AuthUtils.getUser().getUserId().toString());
            BeanUtils.copyBean(datalabel, datalabelRequest);
            datalabel.setExpression(JSON.toJSONString(datalabelRequest.getExpression()));
            datalabel.setGroupId(groupId.toString());
            datalabel.setName(datalabelRequest.getLabelName());

            System.out.println(datalabel);

            this.datalabelMapper.insert(datalabel);
        });
        return ResultHolder.successMsg("新增成功");
    }

    private static void setDefult(DatalabelRequest datalabelRequest) {
        if (datalabelRequest.getDataType() == null) {
            datalabelRequest.setDataType(DataTypeEnum.INDICATOR.getValue());
        }
        if (datalabelRequest.getFieldType() == null) {
            datalabelRequest.setFieldType(FieldTypeEnum.TEXT.getValue());
        }
    }

    /**
     * 修改数据
     * @param datalabelGroupRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public DatalabelGroup update(DatalabelGroupRequest datalabelGroupRequest) {
        if (datalabelGroupRequest.getName() == null && datalabelGroupRequest.getId() == null) {
            DEException.throwException("标签名称或id不能为空");
        }
        //判断标签分组名称在别处是否存在
        DatalabelGroup datalabelGroupExist = datalabelGroupMapper.queryByNameLimit(datalabelGroupRequest.getName(),AuthUtils.getUser().getUserId().toString(),datalabelGroupRequest.getId());
        if (datalabelGroupExist != null){
            DEException.throwException("标签名称已存在");
        }
        //修改分组信息
        DatalabelGroup datalabelGroup = new DatalabelGroup(true);
        BeanUtils.copyBean(datalabelGroup,datalabelGroupRequest);
        datalabelGroup.setExpression(JSON.toJSONString(datalabelGroupRequest.getExpression()));
        datalabelGroup.setUpdateTime(System.currentTimeMillis());
        datalabelGroupMapper.update(datalabelGroup);
        //删除之前的分组里的标签
        datalabelMapper.deleteByGroupId(datalabelGroupRequest.getId(),AuthUtils.getUser().getUserId().toString());
        //新增标签
        datalabelGroupRequest.getLabels().forEach(datalabelRequest -> {
            setDefult(datalabelRequest);
            Datalabel datalabel = new Datalabel(true);
            datalabel.setCreateBy(AuthUtils.getUser().getUserId().toString());
            BeanUtils.copyBean(datalabel, datalabelRequest);
            datalabel.setExpression(JSON.toJSONString(datalabelRequest.getExpression()));
            datalabel.setGroupId(datalabelGroupRequest.getId().toString());
            datalabel.setName(datalabelRequest.getLabelName());
            this.datalabelMapper.insert(datalabel);
        });
        return this.queryGroupById(datalabelGroupRequest.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Integer id) {
        //此id是分组id
        //删除分组
        datalabelGroupMapper.deleteById(id);
        //删除分组下的标签
        return datalabelMapper.deleteByGroupId(id,AuthUtils.getUser().getUserId().toString()) > 0;
    }

    public ResultHolder deleteBatch(List<Integer> ids) {
        final StringBuilder idsTextBuilder = new StringBuilder();
        ids.forEach(id -> idsTextBuilder.append(id + ","));
        String idsText = idsTextBuilder.toString();
        //截取最后一个字符
        idsText = idsText.substring(0, idsText.length() - 1);
        Integer line = datalabelGroupMapper.deleteBatch(idsText, AuthUtils.getUser().getUserId().toString());
        Integer deleteLabel = datalabelMapper.deleteBatchByGroupId(idsText, AuthUtils.getUser().getUserId().toString());
        if (line > 0) {
            return ResultHolder.successMsg("删除成功");
        }
        return ResultHolder.error("删除失败");
    }

    public List<DatalabelGroup> querylabelByPage(String keyWord) {
        List<DatalabelGroup> datalabelGroups = datalabelGroupMapper.queryIdAndNameAll(keyWord, AuthUtils.getUser().getUserId().toString());
        // datalabelGroups.forEach(datalabelGroup -> {
        //     //查询分组的标签
        //     List<Datalabel> labels = datalabelMapper.queryByGroupId(datalabelGroup.getId());
        //     datalabelGroup.setLabels(labels);
        // });
        return datalabelGroups;
    }

    public List<Datalabel> getLables(Integer id) {
        return datalabelMapper.queryByGroupId(id);
    }


    // 标签上架
    public ResultHolder publish(List<Integer> ids) {

        Integer line = datalabelGroupMapper.publish(ids, AuthUtils.getUser().getUserId().toString());
        if (line > 0) {
            return ResultHolder.successMsg("批量上架成功");
        }
        return ResultHolder.error("批量上架失败");
    }

    // 标签下架
    public ResultHolder unpublish(List<Integer> ids) {

        Integer line = datalabelGroupMapper.unpublish(ids, AuthUtils.getUser().getUserId().toString());
        if (line > 0) {
            return ResultHolder.successMsg("批量下架成功");
        }
        return ResultHolder.error("批量下架失败");
    }


    public List<DatalabelGroup> queryEnableAll() {
        List<DatalabelGroup> datalabelGroups = datalabelGroupMapper.queryEnableAll(AuthUtils.getUser().getUserId().toString());

        return datalabelGroups;
    }
}
