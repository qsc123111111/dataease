package io.dataease.controller.authModel;

import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.controller.request.authModel.VAuthModelPageRequest;
import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.service.authModel.VAuthModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2021/11/5
 * Description:
 */
@Api(tags = "（主题分类）授权树：授权树模型")
@ApiSupport(order = 80)
@RestController
@RequestMapping("authModel")
public class VAuthModelController {

    @Resource
    private VAuthModelService vAuthModelService;

    @PostMapping("/queryAuthModel")
    public List<VAuthModelDTO> queryAuthModel(@RequestBody VAuthModelRequest request){
        return vAuthModelService.queryAuthModel(request);
    }

    @ApiOperation("通过文件夹id查询下面的主题模型")
    @GetMapping("/queryModel")
    public JSONObject queryModel(@RequestParam String id,
                                 @RequestParam(defaultValue = "1") Integer pageNo,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String keyWord,
                                 @RequestParam(required = false) Long time,
                                 @RequestParam(defaultValue = "desc") String order){
        return vAuthModelService.queryModel(id,pageNo,pageSize,keyWord,order,time);
    }

    @ApiOperation("主题分类：文件夹")
    @PostMapping("/queryGroup")
    public List<VAuthModelDTO> queryGroup(@RequestBody VAuthModelRequest request){
        return vAuthModelService.queryGroup(request);
    }


    @ApiOperation("主题分类：分页查询")
    @PostMapping("/page")
    public Map<String,Object> page(@RequestBody VAuthModelPageRequest vAuthModelPageRequest){
        return vAuthModelService.page(vAuthModelPageRequest);
    }

    @ApiOperation("主题分类：分页查询")
    @PostMapping("/updateModel")
    public boolean updateModel(@RequestBody VAuthModelPageRequest vAuthModelPageRequest){
        return vAuthModelService.updateModel(vAuthModelPageRequest);
    }

}
