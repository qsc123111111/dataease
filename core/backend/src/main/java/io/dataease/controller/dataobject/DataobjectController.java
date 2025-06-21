package io.dataease.controller.dataobject;

import com.google.gson.Gson;
import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.DePermissions;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.dataobject.enums.ObjectPeriodEnum;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.dto.authModel.modelCacheEnum;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.mapper.DatamodelMapper;
import io.dataease.service.authModel.VAuthModelService;
import io.dataease.service.dataset.DataSetTableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Api(tags = "主题对象：主题对象管理")
@RequestMapping("/dataobject")
@RestController
public class DataobjectController {
    @Resource
    private DatamodelMapper datamodelMapper;
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

        //TODO 输出datasetTable
        System.out.println("datasetTable"+datasetTable);

        //查询表是否同步完成
        //主题对象 都是多表关联  只需要类型是union的
        //dataSetTable添加period字段 1：对象主题 2:主题模型
        if (datasetTable.getType().equalsIgnoreCase("union")) {
            int count = datamodelMapper.selectByObjectId(datasetTable.getId());
            if (count>0){
                throw new Exception("该主体对象已被使用,无法修改,如需修改 请先删除引用的主题模型");
            }
            CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
            datasetTable.setPeriod(ObjectPeriodEnum.OBJECT.getValue());
            List<VAuthModelDTO> dataset = vAuthModelService.queryAuthModelByIdsAddObject("dataset", Collections.singletonList(dataSetTableService.saveObjectAndRed(datasetTable).getId()));
            return ResultHolder.success(dataset);
        } else {
            return ResultHolder.error("主题对象只支持 联表");
        }
    }

    @ApiOperation("主题对象：分页数据")
    @GetMapping("/queryObjectPage")
    public HashMap queryObjectPage(@RequestParam Integer pageNo,
                                   @RequestParam Integer pageSize,
                                   @RequestParam(required = false) String keyWord,
                                   @RequestParam(required = false) String creatSort,
                                   @RequestParam(required = false) String createTimeSort,
                                   @RequestParam(defaultValue = "desc") String updateTimeSort) {
        pageNo = (pageNo-1)*pageSize;
        return dataSetTableService.queryObjectPage(pageNo,pageSize,keyWord,creatSort,createTimeSort,updateTimeSort);
    }

    @ApiOperation("主题对象：不分页数据")
    @GetMapping("/queryObjectAll")
    public List<DatasetTable> queryObjectAll(@RequestParam(required = false) String keyWord) {
        return dataSetTableService.queryObjectAll(keyWord);
    }
}
