package io.dataease.controller.datamodel;

import io.dataease.auth.annotation.DePermission;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
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

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

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
        datamodelService.checkmodelRefByView(datamodelRequest.getId());
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        //查询旧模型创建时间
        DatasetGroup datasetGroup = dataSetGroupService.selectById(datamodelRequest.getId());
        datamodelRequest.setCreateTime(datasetGroup.getCreateTime());
        //删除旧模型
        dataSetGroupService.deleteRef(datamodelRequest.getId());
        //删除旧模型关联数据集的顺序表
        //创建模型
        datamodelRequest.setId(null);
        return datamodelService.saveNew(datamodelRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("主题模型：删除")
    @PostMapping("/delete/{id}")
    public void delete(@PathVariable String id) throws Exception {

        long tStart = System.currentTimeMillis();
        System.out.println("【删除流程开始】时间: " + LocalDateTime.now().format(TIME_FORMATTER));

        // 1. 检查引用
        long stepStart = System.currentTimeMillis();
        System.out.println("开始检查模型引用...");
        datamodelService.checkmodelRefByView(id);
        System.out.println("【步骤1】检查模型引用耗时: " + (System.currentTimeMillis() - stepStart) + " ms");

        // 2. 获取 DatasetGroup
        stepStart = System.currentTimeMillis();
        System.out.println("开始获取 DatasetGroup...");
        DatasetGroup datasetGroup = dataSetGroupService.getScene(id);
        System.out.println("【步骤2】获取 DatasetGroup 耗时: " + (System.currentTimeMillis() - stepStart) + " ms");

        // 3. 构建日志
        stepStart = System.currentTimeMillis();
        System.out.println("开始构建日志对象...");
        SysLogDTO sysLogDTO = DeLogUtils.buildLog(
                SysLogConstants.OPERATE_TYPE.DELETE,
                SysLogConstants.SOURCE_TYPE.DATASET,
                id,
                datasetGroup.getPid(),
                null,
                null
        );
        System.out.println("【步骤3】构建日志对象耗时: " + (System.currentTimeMillis() - stepStart) + " ms");

        // 4. 异步删除引用
        CompletableFuture.runAsync(() -> {
            long asyncStepStart = System.currentTimeMillis();
            System.out.println("异步开始删除引用...");
            try {
                dataSetGroupService.deleteRef(id);
                System.out.println("异步删除引用成功");
            } catch (Exception e) {
                System.out.println("异步删除引用异常：" + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("异步删除引用耗时: " + (System.currentTimeMillis() - asyncStepStart) + " ms");
        });



        // 异步处理删除term_table操作
        CompletableFuture.runAsync(() -> {

            String startTime = LocalDateTime.now().format(TIME_FORMATTER);
            System.out.println("异步删除 term_table 开始时间: " + startTime);
            try {
                List<VAuthModelDTO> vAuthModelDTOS = vAuthModelService.detailChild(id);
                List<String> ids = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(vAuthModelDTOS)) {
                    VAuthModelDTO vAuthModelDTO = vAuthModelDTOS.get(0);
                    List<VAuthModelDTO> children = vAuthModelDTO.getChildren();
                    if (children != null && !children.isEmpty()) {
                        children.forEach(item -> ids.add(item.getId()));
                        termTableMapper.deleteByModelIds(ids);
                    }
                }
            } catch (Exception e) {
                // 记录异常日志
                System.out.println("删除term_table异常：" + e.getMessage());
            }
            String endTime = LocalDateTime.now().format(TIME_FORMATTER);
            System.out.println("异步删除 term_table 结束时间: " + endTime);
        });

        // 6. 清理缓存
        stepStart = System.currentTimeMillis();
        System.out.println("开始清理缓存...");
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        System.out.println("【步骤5】清理缓存耗时: " + (System.currentTimeMillis() - stepStart) + " ms");

        // 7. 保存日志
        stepStart = System.currentTimeMillis();
        System.out.println("开始保存操作日志...");
        DeLogUtils.save(sysLogDTO);
        System.out.println("【步骤6】保存操作日志耗时: " + (System.currentTimeMillis() - stepStart) + " ms");

        // 总耗时
        long tEnd = System.currentTimeMillis();
        System.out.println("【删除流程结束】时间: " + LocalDateTime.now().format(TIME_FORMATTER));
        System.out.println("【总耗时】共计: " + (tEnd - tStart) + " ms");
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
