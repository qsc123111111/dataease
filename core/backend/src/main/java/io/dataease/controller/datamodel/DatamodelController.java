package io.dataease.controller.datamodel;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datamodel.request.DatamodelRequest;
import io.dataease.service.datamodel.DatamodelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "主题模型")
@RestController
@RequestMapping("/datamodel")
public class DatamodelController {

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
}
