package io.dataease.controller.datasource;

import io.dataease.controller.ResultHolder;
import io.dataease.controller.datasource.request.InsertDataSourceGroupRequest;
import io.dataease.service.datasource.DatasourceGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "数据源：数据源分组管理")
@RequestMapping("/datasourceGroup")
@RestController
public class DatasourceGroupController {
    @Resource
    private DatasourceGroupService datasourceGroupService;

    // @NoResultHolder
    @ApiOperation("新增分组")
    @PostMapping("/save")
    public ResultHolder save(@RequestBody InsertDataSourceGroupRequest req) {
        if (StringUtils.isBlank(req.getName())) {
            return ResultHolder.error("名称不能为空");
        }
        return datasourceGroupService.save(req);
    }

    @ApiOperation("修改分组")
    @PostMapping("/update")
    public ResultHolder update(@RequestBody InsertDataSourceGroupRequest req) {
        if (StringUtils.isBlank(req.getName() ) || StringUtils.isBlank(req.getId())) {
            return ResultHolder.error("名称或id不能为空");
        }
        return datasourceGroupService.update(req);
    }

    @ApiOperation("删除分组")
    @GetMapping("/delete")
    public ResultHolder delete(@RequestParam String id) {
        if (StringUtils.isBlank(id)) {
            return ResultHolder.error("id不能为空");
        }
        return datasourceGroupService.delete(id);
    }

    @ApiOperation("数据源列表")
    @GetMapping("/list")
    public ResultHolder list() {
        return datasourceGroupService.list();
    }

}
