package io.dataease.controller.datamodel;

import io.dataease.auth.annotation.DePermission;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datamodel.request.DatamodelRequest;
import io.dataease.dto.SysLogDTO;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.dto.authModel.modelCacheEnum;
import io.dataease.dto.datamodel.DatamodelChartDTO;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.DatasetGroup;
import io.dataease.plugins.common.base.mapper.TermTableMapper;
import io.dataease.service.authModel.VAuthModelService;
import io.dataease.service.datamodel.DatamodelService;
import io.dataease.service.dataset.DataSetGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "主题模型")
@RestController
@RequestMapping("/datamodel")
public class DatamodelController {
    @Resource
    private TermTableMapper termTableMapper;
    @Resource
    private VAuthModelService vAuthModelService;
    @Resource
    private DataSetGroupService dataSetGroupService;

    @Resource
    private DatamodelService datamodelService;

    @ApiOperation("主题模型：添加")
    @PostMapping("/save")
    public ResultHolder save(@RequestBody DatamodelRequest datamodelRequest) throws Exception {
        // 需要sceneId，在该sceneId下面创建文件夹
        if (datamodelRequest.getSceneId() == null){
            return ResultHolder.error("所属文件夹不能为空");
        }
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        return datamodelService.saveNew(datamodelRequest);
    }

    @ApiOperation("主题模型：更新")
    @PostMapping("/update")
    public ResultHolder update(@RequestBody DatamodelRequest datamodelRequest) throws Exception {
        // 需要sceneId，在该sceneId下面创建文件夹
        if (datamodelRequest.getId() == null){
            return ResultHolder.error("模型id不能为空");
        }
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        //查询旧模型创建时间
        DatasetGroup datasetGroup = dataSetGroupService.selectById(datamodelRequest.getId());
        datamodelRequest.setCreateTime(datasetGroup.getCreateTime());
        //删除旧模型
        dataSetGroupService.deleteRef(datamodelRequest.getId());
        //创建模型
        datamodelRequest.setId(null);
        return datamodelService.saveNew(datamodelRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("主题模型：删除")
    @PostMapping("/delete/{id}")
    public void delete(@PathVariable String id) throws Exception {
        List<VAuthModelDTO> vAuthModelDTOS = vAuthModelService.detailChild(id);
        DatasetGroup datasetGroup = dataSetGroupService.getScene(id);
        SysLogDTO sysLogDTO = DeLogUtils.buildLog(SysLogConstants.OPERATE_TYPE.DELETE, SysLogConstants.SOURCE_TYPE.DATASET, id, datasetGroup.getPid(), null, null);
        dataSetGroupService.deleteRef(id);
        List<String> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(vAuthModelDTOS)){
            VAuthModelDTO vAuthModelDTO = vAuthModelDTOS.get(0);
            List<VAuthModelDTO> children = vAuthModelDTO.getChildren();
            if (children != null && children.size() > 0){
                children.forEach(item -> ids.add(item.getId()));
                //删除 term_table
                termTableMapper.deleteByModelIds(ids);
            }
        }
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        DeLogUtils.save(sysLogDTO);
    }

    @ApiOperation("主题模型：child数据")
    @GetMapping("/detailChild/{id}")
    public List<VAuthModelDTO> detailChild(@PathVariable String id) throws Exception {
        //查询此路径下的详细数据
        return vAuthModelService.detailChild(id);
    }

    @ApiOperation("主题模型：获取详细信息 用于修改数据回显 模型组成展示")
    @GetMapping("/getInfo/{id}")
    public DatamodelRequest getInfo(@PathVariable String id) throws Exception {
        //查询此路径下的详细数据
        return datamodelService.getInfo(id);
    }

    @ApiOperation("主题模型：模型预览")
    @GetMapping("/getModelChart/{id}")
    public DatamodelChartDTO getModelChart(@PathVariable String id) throws Exception {
        //查询此路径下的详细数据
        return datamodelService.getModelChart(id);
    }

    @ApiOperation("主题模型：上架、下架")
    @GetMapping("/upDown/{id}/{tag}")
    public Boolean upDown(@PathVariable String id,@PathVariable Integer tag) {
        Assert.notNull(id, "id cannot be null");
        if (tag != 1 && tag !=0){
            throw new RuntimeException("tag值不符合规范");
        }
        return datamodelService.upDown(id,tag);
    }
}
