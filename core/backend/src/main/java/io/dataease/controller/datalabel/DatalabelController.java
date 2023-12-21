package io.dataease.controller.datalabel;

import cn.hutool.json.JSONObject;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datalabel.request.DatalabelGroupRequest;
import io.dataease.controller.datalabel.request.DatalabelRequest;
import io.dataease.plugins.common.base.domain.Datalabel;
import io.dataease.plugins.common.base.domain.DatalabelGroup;
import io.dataease.service.datalabel.DatalabelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    public JSONObject querylabelByPage(@RequestParam(defaultValue = "1") Integer pageNo,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) Long time,
                                       @RequestParam(required = false) String keyWord,
                                       @RequestParam(required = false) String numSort,
                                       @RequestParam(required = false) String timeSort) {
        pageNo = (pageNo - 1) * pageSize;
        return datalabelService.queryByPage(pageNo, pageSize,time, keyWord, numSort, timeSort);
    }

    @ApiOperation("主题标签：不分页数据")
    @GetMapping("/queryAll")
    public List<DatalabelGroup> querylabelByPage(@RequestParam(required = false) String keyWord) {
        return datalabelService.querylabelByPage(keyWord);
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
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @ApiOperation("主题标签：通过分组id查询标签")
    @GetMapping("/getLables/{id}")
    public List<Datalabel> getLables(@PathVariable("id") Integer id) {
        return datalabelService.getLables(id);
    }

    /**
     * 新增数据
     *
     * @param datalabel 实体
     * @return 新增结果
     */
    @ApiOperation("主题标签：新增数据")
    @PostMapping("/add")
    public ResultHolder add(@RequestBody DatalabelGroupRequest datalabel) throws Exception {
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
    public DatalabelGroup edit(@RequestBody DatalabelGroupRequest datalabel) {
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
    public Boolean deleteById(@PathVariable Integer id) throws Exception {
        if (id == null) {
            throw new Exception("id不能为空");
        }
        return datalabelService.deleteById(id);
    }

    @ApiOperation("批量删除数据")
    @PostMapping("/deleteBatch")
    public ResultHolder edit(@RequestBody List<Integer> ids) {
        if (ids.size() == 0) {
            return ResultHolder.error("请选择要删除的数据");
        }
        return datalabelService.deleteBatch(ids);
    }
}
