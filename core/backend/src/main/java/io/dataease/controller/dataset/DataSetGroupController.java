package io.dataease.controller.dataset;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.DePermissions;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.controller.datamodel.enums.DatamodelEnum;
import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.controller.request.dataset.DataSetGroupRequest;
import io.dataease.dto.SysLogDTO;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.dto.authModel.modelCacheEnum;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.DatasetGroup;
import io.dataease.service.authModel.VAuthModelService;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.kettle.KettleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/20 8:29 下午
 */
@Api(tags = "数据集：数据集组")
@ApiSupport(order = 40)
@RestController
@RequestMapping("dataset/group")
public class DataSetGroupController {
    @Resource
    private DataSetGroupService dataSetGroupService;
    @Resource
    private KettleService kettleService;

    @Resource
    private VAuthModelService vAuthModelService;

    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id"),
            @DePermission(type = DePermissionType.DATASET, value = "pid", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    }, logical = Logical.AND)
    @ApiOperation("保存")
    @PostMapping("/save")
    public VAuthModelDTO save(@RequestBody DatasetGroup datasetGroup) throws Exception {
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        datasetGroup.setDirType(DatamodelEnum.NORMAL_DIR.getValue());
        DataSetGroupDTO result = dataSetGroupService.save(datasetGroup);
        return vAuthModelService.queryAuthModelByIds("dataset", Arrays.asList(result.getId())).get(0);
    }

    @ApiIgnore
    @PostMapping("/tree")
    public List<DataSetGroupDTO> tree(@RequestBody DataSetGroupRequest datasetGroup) {
        return dataSetGroupService.tree(datasetGroup);
    }

    @ApiIgnore
    @PostMapping("/treeNode")
    public List<DataSetGroupDTO> treeNode(@RequestBody DataSetGroupRequest datasetGroup) {
        return dataSetGroupService.treeNode(datasetGroup);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("删除")
    @PostMapping("/delete/{id}")
    public void delete(@PathVariable String id) throws Exception {
        //删除前 先判断是否含有主题模型
        VAuthModelRequest request = new VAuthModelRequest();
        request.setModelType("dataset");
        List<VAuthModelDTO> vAuthModelDTOS = vAuthModelService.queryAuthModel(request);
        List<VAuthModelDTO> currentTree = getsTheCurrentTree(id, vAuthModelDTOS);
        Boolean aBoolean = checkTreeDirType(currentTree);
        if (aBoolean){
            throw new Exception("该目录下含有主题模型，请先删除主题模型再删除目录");
        } else {
            CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
            DatasetGroup datasetGroup = dataSetGroupService.getScene(id);
            SysLogDTO sysLogDTO = DeLogUtils.buildLog(SysLogConstants.OPERATE_TYPE.DELETE, SysLogConstants.SOURCE_TYPE.DATASET, id, datasetGroup.getPid(), null, null);
            dataSetGroupService.delete(id);
            DeLogUtils.save(sysLogDTO);
        }
    }
    private Boolean checkTreeDirType(List<VAuthModelDTO> vAuthModelDTOS) {
        if (CollectionUtils.isEmpty(vAuthModelDTOS)){
            return false;
        }
        for (VAuthModelDTO vAuthModelDTO : vAuthModelDTOS) {
            if (vAuthModelDTO.getDirType() == DatamodelEnum.MODEL_DIR.getValue()){
                return true;
            } else {
                checkTreeDirType(vAuthModelDTO.getChildren());
            }
        }
        return false;
    }

    private List<VAuthModelDTO> getsTheCurrentTree(String id, List<VAuthModelDTO> vAuthModelDTOS) {
        if (CollectionUtils.isEmpty(vAuthModelDTOS)){
            return null;
        }
        for (VAuthModelDTO vAuthModelDTO : vAuthModelDTOS) {
            List<VAuthModelDTO> children = vAuthModelDTO.getChildren();
            if (id.equals(vAuthModelDTO.getId())){
                return children;
            } else {
                return getsTheCurrentTree(id,children);
            }
        }
        return null;
    }

    @ApiIgnore
    @PostMapping("/getScene/{id}")
    public DatasetGroup getScene(@PathVariable String id) {
        return dataSetGroupService.getScene(id);
    }

    @ApiIgnore
    @PostMapping("/isKettleRunning")
    public boolean isKettleRunning() {
        return kettleService.isKettleRunning();
    }
}
