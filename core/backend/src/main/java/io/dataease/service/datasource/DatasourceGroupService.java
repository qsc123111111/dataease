package io.dataease.service.datasource;

import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datasource.request.InsertDataSourceGroupRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.SysLogDTO;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.domain.DatasourceGroup;
import io.dataease.plugins.common.base.mapper.DatasourceGroupMapper;
import io.dataease.service.dataset.DataSetTableService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DatasourceGroupService {
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private DatasourceGroupMapper datasourceGroupMapper;

    public ResultHolder save(InsertDataSourceGroupRequest req) {
        int count = datasourceGroupMapper.queryByName(req.getName(),AuthUtils.getUser().getUserId().toString());
        if (count > 0) {
            return ResultHolder.error("该名称已存在");
        }
        DatasourceGroup datasourceGroup = new DatasourceGroup(true);
        datasourceGroup.setName(req.getName());
        datasourceGroup.setCreateBy(AuthUtils.getUser().getUserId().toString());
        if (StringUtils.isNotBlank(req.getDesc())) {
            datasourceGroup.setDesc(req.getDesc());
        }
        int line = datasourceGroupMapper.insert(datasourceGroup);
        return line > 0 ? ResultHolder.successMsg("新建分组成功") : ResultHolder.error("新建分组失败");
    }

    public ResultHolder update(InsertDataSourceGroupRequest req) {
        DatasourceGroup datasourceGroup = BeanUtils.copyBean(new DatasourceGroup(), req);
        datasourceGroup.setUpdateTime(System.currentTimeMillis());
        datasourceGroup.setCreateBy(AuthUtils.getUser().getUserId().toString());
        int line = datasourceGroupMapper.updateById(datasourceGroup);
        return line > 0 ? ResultHolder.successMsg("更新分组成功") : ResultHolder.error("更新分组失败");
    }

    public ResultHolder delete(String id) throws Exception {
        int line = datasourceGroupMapper.deleteById(id, AuthUtils.getUser().getUserId().toString());
        //TODO 删除其他的数据(数据集 主题对象 等) (待测试)
        //查询分组里的数据集
        DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
        dataSetTableRequest.setGroupId(id);
        List<DatasetTable> datasetTables = dataSetTableService.listByGroup(dataSetTableRequest);
        //删除数据集和数据源
        for (DatasetTable datasetTable : datasetTables) {
            //数据集id
            String datasetTableId = datasetTable.getId();
            dataSetTableService.delete(datasetTableId);
            //数据源id
            String dataSourceId = datasetTable.getDataSourceId();
            Datasource datasource = datasourceService.get(dataSourceId);
            SysLogDTO sysLogDTO = DeLogUtils.buildLog(SysLogConstants.OPERATE_TYPE.DELETE, SysLogConstants.SOURCE_TYPE.DATASOURCE, dataSourceId, datasource.getType(), null, null);
            ResultHolder resultHolder = datasourceService.deleteDatasource(dataSourceId);
        }
        return line > 0 ? ResultHolder.successMsg("删除分组成功") : ResultHolder.error("删除分组失败");
    }

    public ResultHolder list() {
        return ResultHolder.success("列表查询成功", datasourceGroupMapper.listAll(AuthUtils.getUser().getUserId().toString()));
    }
}
