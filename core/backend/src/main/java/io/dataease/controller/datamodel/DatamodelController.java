package io.dataease.controller.datamodel;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datamodel.request.DatamodelRequest;
import io.dataease.dto.SysLogDTO;
import io.dataease.plugins.common.base.domain.DatasetGroup;
import io.dataease.service.datamodel.DatamodelService;
import io.dataease.service.dataset.DataSetGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "主题模型")
@RestController
@RequestMapping("/datamodel")
public class DatamodelController {
    @Resource
    private DataSetGroupService dataSetGroupService;

    @Resource
    private DatamodelService datamodelService;

    @ApiOperation("主题模型：更新/添加")
    @PostMapping("/save")
    public ResultHolder save(@RequestBody DatamodelRequest datamodelRequest) throws Exception {
        //需要sceneId，在该sceneId下面创建文件夹
        if (datamodelRequest.getSceneId() == null){
            return ResultHolder.error("所属文件夹不能为空");
        }
        return datamodelService.save(datamodelRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("主题模型：删除")
    @PostMapping("/delete/{id}")
    public void delete(@PathVariable String id) throws Exception {
        DatasetGroup datasetGroup = dataSetGroupService.getScene(id);
        SysLogDTO sysLogDTO = DeLogUtils.buildLog(SysLogConstants.OPERATE_TYPE.DELETE, SysLogConstants.SOURCE_TYPE.DATASET, id, datasetGroup.getPid(), null, null);
        dataSetGroupService.delete(id);
        DeLogUtils.save(sysLogDTO);
    }
}
