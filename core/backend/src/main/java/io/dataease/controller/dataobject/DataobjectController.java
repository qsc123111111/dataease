package io.dataease.controller.dataobject;

import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.DePermissions;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.dataobject.enums.ObjectPeriodEnum;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.service.authModel.VAuthModelService;
import io.dataease.service.dataset.DataSetTableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Api(tags = "主题对象：主题对象管理")
@RequestMapping("/dataobject")
@RestController
public class DataobjectController {
    @Resource
    private VAuthModelService vAuthModelService;
    @Resource
    private DataSetTableService dataSetTableService;

    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE),
            @DePermission(type = DePermissionType.DATASET, value = "sceneId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE),
            @DePermission(type = DePermissionType.DATASOURCE, value = "dataSourceId", level = ResourceAuthLevel.DATASOURCE_LEVEL_USE)
    }, logical = Logical.AND)
    @ApiOperation("主题对象：更新/添加")
    @PostMapping("/update")
    public ResultHolder save(@RequestBody DataSetTableRequest datasetTable) throws Exception {
        //主题对象 都是多表关联  只需要类型是union的
        //dataSetTable添加period字段 1：对象主题 2:主题模型
        if (datasetTable.getType().equalsIgnoreCase("union")) {
            datasetTable.setPeriod(ObjectPeriodEnum.OBJECT.getValue());
            List<VAuthModelDTO> dataset = vAuthModelService.queryAuthModelByIdsAddObject("dataset", Collections.singletonList(dataSetTableService.save(datasetTable).getId()));
            //
            return ResultHolder.success(dataset);
        } else {
            return ResultHolder.error("主题对象只支持 联表");
        }
    }

    @ApiOperation("主题对象：分页数据")
    @GetMapping("/queryObjectPage")
    public List<DatasetTable> queryObjectPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize, @RequestParam(required = false) String keyWord) {
        pageNo = (1 - pageNo)*pageSize;
        return dataSetTableService.queryObjectPage(pageNo,pageSize,keyWord);
    }
}
