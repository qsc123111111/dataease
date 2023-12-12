package io.dataease.service.datalabel;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
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

    /**
     * 分页查询
     *
     * @param pageNo
     * @param pageSize
     * @param time
     * @param keyWord
     * @return
     */
    public JSONObject queryByPage(Integer pageNo, Integer pageSize, Long time, String keyWord) {
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
        long total = this.datalabelMapper.simpleCount(keyWord, AuthUtils.getUser().getUserId(),time,plusOneTime);
        List<Datalabel> list = datalabelMapper.queryPageAllByLimit(pageNo, pageSize,keyWord, AuthUtils.getUser().getUserId(),time,plusOneTime);
        list.stream().forEach(datalabel -> datalabel.setCreateBy(sysUserMapper.selectNameById(datalabel.getCreateBy())));
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
        DatalabelGroup datalabelGroup = new DatalabelGroup(true);
        BeanUtils.copyBean(datalabelGroup,datalabelGroupRequest);
        datalabelGroup.setCreateBy(AuthUtils.getUser().getUserId().toString());
        int insert = datalabelGroupMapper.insert(datalabelGroup);
        Integer groupId = datalabelGroup.getId();
        datalabelGroupRequest.getLabels().forEach(datalabelRequest -> {
            setDefult(datalabelRequest);
            Datalabel datalabel = new Datalabel(true);
            datalabel.setCreateBy(AuthUtils.getUser().getUserId().toString());
            BeanUtils.copyBean(datalabel, datalabelRequest);
            datalabel.setExpression(JSON.toJSONString(datalabelRequest.getExpression()));
            datalabel.setGroup(groupId.toString());
            datalabel.setName(datalabelRequest.getLabelName());
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
     * @param datalabelRequest
     * @return
     */
    public Datalabel update(DatalabelRequest datalabelRequest) {
        if (datalabelRequest.getName() == null && datalabelRequest.getId() == null) {
            throw new RuntimeException("标签名称或id不能为空");
        }
        setDefult(datalabelRequest);
        Datalabel datalabel = new Datalabel(false);
        datalabel.setCreateBy(AuthUtils.getUser().getUserId().toString());
        datalabel.setUpdateTime(System.currentTimeMillis());
        BeanUtils.copyBean(datalabel, datalabelRequest);
        if (datalabelRequest.getExpression() != null) {
            datalabel.setExpression(JSON.toJSONString(datalabelRequest.getExpression()));
        }
        this.datalabelMapper.update(datalabel);
        return this.queryById(datalabel.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    public boolean deleteById(Integer id) {
        return this.datalabelMapper.deleteById(id,AuthUtils.getUser().getUserId().toString()) > 0;
    }

    public ResultHolder deleteBatch(List<Integer> ids) {
        final StringBuilder idsTextBuilder = new StringBuilder();
        ids.forEach(id -> idsTextBuilder.append(id + ","));
        String idsText = idsTextBuilder.toString();
        //截取最后一个字符
        idsText = idsText.substring(0, idsText.length() - 1);
        Integer line = datalabelMapper.deleteBatch(idsText, AuthUtils.getUser().getUserId().toString());
        if (line > 0) {
            return ResultHolder.successMsg("删除成功");
        }
        return ResultHolder.error("删除失败");
    }

    public List<Datalabel> querylabelByPage() {
        return datalabelMapper.queryIdAndNameAll(AuthUtils.getUser().getUserId().toString());
    }
}
