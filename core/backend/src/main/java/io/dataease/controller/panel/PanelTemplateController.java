package io.dataease.controller.panel;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.controller.request.panel.PanelTemplatePageRequest;
import io.dataease.controller.request.panel.PanelTemplateParam;
import io.dataease.plugins.common.base.domain.PanelTemplateWithBLOBs;
import io.dataease.controller.handler.annotation.I18n;
import io.dataease.controller.request.panel.PanelTemplateRequest;
import io.dataease.dto.panel.PanelTemplateDTO;
import io.dataease.service.panel.PanelTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Api(tags = "仪表板：模版")
@ApiSupport(order = 170)
@RestController
@RequestMapping("template")
public class PanelTemplateController {

    @Resource
    private PanelTemplateService panelTemplateService;

    @ApiOperation("查询树")
    @PostMapping("/templateList")
    @I18n
    public List<PanelTemplateDTO> templateList(@RequestBody PanelTemplateRequest request) {
        return panelTemplateService.templateList(request);
    }

    @ApiOperation("保存")
    @PostMapping("/save")
    public PanelTemplateDTO save(@RequestBody PanelTemplateRequest request) {
        return panelTemplateService.save(request);
    }

    @ApiOperation("删除")
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        panelTemplateService.delete(id);
    }

    @ApiOperation("详细信息")
    @GetMapping("/findOne/{id}")
    public PanelTemplateWithBLOBs findOne(@PathVariable String id) throws Exception {
        return panelTemplateService.findOne(id);
    }

    @ApiOperation("查询")
    @PostMapping("/find")
    public List<PanelTemplateDTO> find(@RequestBody PanelTemplateRequest request) {
        return panelTemplateService.find(request);
    }

    @ApiOperation("名称校验")
    @PostMapping("/nameCheck")
    public String nameCheck(@RequestBody PanelTemplateRequest request) {
        return panelTemplateService.nameCheck(request);
    }

    @ApiOperation("查询模板列表")
    @PostMapping("/templatePage")
    @RequiresPermissions("template:view")
    @I18n
    public Map<String,Object> templatePage(@RequestBody PanelTemplatePageRequest request){
        return panelTemplateService.pageList(request);
    }

    @ApiOperation("模板上架")
    @PostMapping("/templateShow")
    @RequiresPermissions("template:up")
    @I18n
    public Integer templateShow(@RequestBody PanelTemplateParam request){
        return panelTemplateService.templateShow(request);
    }

    @ApiOperation("批量删除")
    @DeleteMapping("/deleteBatch")
    @RequiresPermissions("template:delete")
    public Integer deleteBatch(@RequestBody PanelTemplateParam request) {
        return panelTemplateService.deleteBatch(request);
    }

    @ApiOperation("批量下载")
    @PostMapping("/downloadBatch")
    @RequiresPermissions("template:download")
    public String downloadBatch(@RequestBody  PanelTemplateParam request){
       return panelTemplateService.downloadBatch(request);
    }

    @ApiOperation("批量上传")
    @PostMapping("/uploadBatch")
    @RequiresPermissions("template:upload")
    public Integer uploadBatch(@RequestParam("templateFiles") MultipartFile[] templateFiles,
                              @RequestParam(value = "templateType" , required = false ,defaultValue = "system")String templateType ){
        if(templateFiles==null || templateType==null ){
            return 0;
        }
        if(templateType.equals("system")==false){
            templateType = "self";
        }
        return panelTemplateService.uploadBatch(templateFiles,templateType);
    }

}
