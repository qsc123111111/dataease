package io.dataease.service.datalabel;

import cn.hutool.json.JSONObject;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datalabel.enums.DataTypeEnum;
import io.dataease.controller.datalabel.enums.FieldTypeEnum;
import io.dataease.controller.datalabel.request.DatalabelRequest;
import io.dataease.plugins.common.base.domain.Datalabel;
import io.dataease.plugins.common.base.mapper.DatalabelMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (Datalabel)表服务实现类
 */
@Service("datalabelService")
public class DatalabelService{
    @Resource
    private DatalabelMapper datalabelMapper;

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
     * @param pageNo
     * @param pageSize
     * @param keyWord
     * @return
     */
    public JSONObject queryByPage(Integer pageNo, Integer pageSize, String keyWord) {
        long total = this.datalabelMapper.simpleCount(keyWord, AuthUtils.getUser().getUserId());
        List<Datalabel> list = datalabelMapper.queryPageAllByLimit(pageNo, pageSize,keyWord, AuthUtils.getUser().getUserId());
        JSONObject result = new JSONObject();
        result.put("total", total);
        result.put("data", list);
        return result;
    }

    /**
     * 新增数据
     * @param datalabelRequest
     * @return
     */
    public Datalabel insert(DatalabelRequest datalabelRequest) {
        if (datalabelRequest.getName() == null) {
            throw new RuntimeException("标签名称不能为空");
        }
        setDefult(datalabelRequest);
        Datalabel datalabel = new Datalabel(true);
        datalabel.setCreateBy(AuthUtils.getUser().getUserId().toString());
        BeanUtils.copyBean(datalabel, datalabelRequest);
        this.datalabelMapper.insert(datalabel);
        return datalabel;
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
