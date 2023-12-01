package io.dataease.controller.datalabel;

import cn.hutool.json.JSONObject;
import io.dataease.controller.datalabel.request.DatalabelRequest;
import io.dataease.plugins.common.base.domain.Datalabel;
import io.dataease.service.datalabel.DatalabelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "主题标签：标签管理")
@RequestMapping("/datalabel")
@RestController
public class DatalabelController {
    /**
     * 服务对象
     */
    @Resource
    private DatalabelService datalabelService;

    /**
     * 分页查询
     * @param pageNo
     * @param pageSize
     * @param keyWord
     * @return
     */
    @ApiOperation("主题标签：分页数据")
    @GetMapping("/querylabelByPage")
    public JSONObject querylabelByPage(@RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(required = false) String keyWord) {
        pageNo = (pageNo - 1) * pageSize;
        return datalabelService.queryByPage(pageNo, pageSize, keyWord);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @ApiOperation("主题标签：通过主键查询单条数据")
    @GetMapping("/{id}")
    public Datalabel queryById(@PathVariable("id") Integer id) {
        return datalabelService.queryById(id);
    }

    /**
     * 新增数据
     *
     * @param datalabel 实体
     * @return 新增结果
     */
    @ApiOperation("主题标签：新增数据")
    @PostMapping("/add")
    public Datalabel add(@RequestBody DatalabelRequest datalabel) {
        return datalabelService.insert(datalabel);
    }

    /**
     * 编辑数据
     *
     * @param datalabel 实体
     * @return 编辑结果
     */
    @ApiOperation("主题标签：编辑数据")
    @PostMapping("/edit")
    public Datalabel edit(@RequestBody DatalabelRequest datalabel) {
        return datalabelService.update(datalabel);
    }

    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除是否成功
     */
    @ApiOperation("主题标签：删除数据")
    @GetMapping("/delete/{id}")
    public Boolean deleteById(@PathVariable Integer id) {
        return datalabelService.deleteById(id);
    }

//    @ApiOperation("批量删除数据")
//    @PostMapping("/edit")
//    public Datalabel edit(@RequestBody List<Integer> ids) {
//        return datalabelService.deleteByIds(ids);
//    }
}
