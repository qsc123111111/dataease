package io.dataease.controller.datasource;

import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datasource.request.InsertDataSourceGroupRequest;
import io.dataease.controller.request.DatasourceUnionRequest;
import io.dataease.dto.DatasourceDTO;
import io.dataease.plugins.common.base.mapper.DatasourceGroupMapper;
import io.dataease.service.datasource.DatasourceGroupService;
import io.dataease.service.datasource.DatasourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "数据源：数据源分组管理")
@RequestMapping("/datasourceGroup")
@RestController
public class DatasourceGroupController {
    @Resource
    private DatasourceGroupService datasourceGroupService;

    @Resource
    private DatasourceService datasourceService;

    @Resource
    private DatasourceGroupMapper datasourceGroupMapper;

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

        //TODO 检查名称是否一致，不一致提示一下
        int count =  datasourceGroupMapper.queryByName(req.getName(),AuthUtils.getUser().getUserId().toString());
        if (count > 0) {
            return ResultHolder.error("该名称已存在");
        }

        return datasourceGroupService.update(req);
    }

    @ApiOperation("删除分组")
    @GetMapping("/delete")
    public ResultHolder delete(@RequestParam String id) throws Exception {
        if (StringUtils.isBlank(id)) {
            return ResultHolder.error("id不能为空");
        }

        DatasourceUnionRequest request = new DatasourceUnionRequest();
        request.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        request.setGroupId(id);

        List<DatasourceDTO> datasourceDTOS = datasourceService.getDatasourceList(request);
        if (ArrayUtils.isNotEmpty(datasourceDTOS.toArray())){
            return ResultHolder.error("分组下有数据源，请先删除数据源");
        }
        return datasourceGroupService.delete(id);
    }

    @ApiOperation("数据源列表")
    @GetMapping("/list")
    public ResultHolder list() {
        return datasourceGroupService.list();
    }

}
