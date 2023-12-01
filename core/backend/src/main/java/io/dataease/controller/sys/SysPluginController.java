package io.dataease.controller.sys;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.auth.annotation.SqlInjectValidator;
import io.dataease.commons.utils.DeFileUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.plugins.common.base.domain.MyPlugin;
import io.dataease.plugins.common.request.KeywordRequest;
import io.dataease.plugins.common.request.PluginParam;
import io.dataease.service.sys.PluginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@ApiIgnore
@RestController
@Api(tags = "系统：插件管理")
@RequestMapping("/api/plugin")
public class SysPluginController {

    @Autowired
    private PluginService pluginService;

    @ApiOperation("查询已安装插件")
    @PostMapping("/pluginGrid/{goPage}/{pageSize}")
    @RequiresPermissions("plugin:read")
    @SqlInjectValidator(value = {"install_time"})
    public Pager<List<MyPlugin>> pluginGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody KeywordRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, pluginService.query(request));
    }

    @ApiOperation("安装插件")
    @PostMapping("upload")
    @RequiresPermissions("plugin:upload")
    public Map<String, Object> localUpload(@RequestParam("file") MultipartFile file) throws Exception {
        DeFileUtils.validateFile(file);
        return pluginService.localInstall(file);
    }

    @ApiOperation("卸载插件")
    @PostMapping("/uninstall/{pluginId}")
    @RequiresPermissions("plugin:uninstall")
    public Boolean unInstall(@PathVariable Long pluginId) {
        return pluginService.uninstall(pluginId);
    }

    @ApiOperation("更新插件")
    @PostMapping("/update/{pluginId}")
    @RequiresPermissions("plugin:upload")
    public Map<String, Object> update(@PathVariable("pluginId") Long pluginId, @RequestParam("file") MultipartFile file) throws Exception {
        DeFileUtils.validateFile(file);
        if (pluginService.uninstall(pluginId)) {
            return pluginService.localInstall(file);
        }
        return null;
    }

    @ApiOperation("分页查询插件")
    @PostMapping("/pluginPage/{goPage}/{pageSize}")
    @RequiresPermissions("plugin:read")
    public Pager<List<MyPlugin>> pluginPage(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody KeywordRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, pluginService.findList(request));
    }

    @ApiOperation("批量安装插件")
    @PostMapping("/uploadBatch")
    @RequiresPermissions("plugin:upload")
    public boolean uploadBatch(@RequestParam("files") MultipartFile[] files) throws Exception {
        for (MultipartFile file : files) {
            DeFileUtils.validateFile(file);
            pluginService.localInstall(file);
        }
        return true;
    }

    @ApiOperation("批量卸载插件")
    @PostMapping("/uninstallBatch")
    @RequiresPermissions("plugin:uninstall")
    public Boolean uninstallBatch(@RequestBody PluginParam pluginParam) {
        if(pluginParam.getIds()==null || pluginParam.getIds().size()<1){
            return false;
        }
        for (Long pluginId : pluginParam.getIds() ) {
            pluginService.uninstall(pluginId);
        }
        return true;
    }

    @ApiOperation("批量上架")
    @PostMapping("/showBatch")
    public Boolean showBatch(@RequestBody PluginParam pluginParam) {
        if(pluginParam.getIds()==null || pluginParam.getIds().size()<1 || pluginParam.getShowFlag()==null){
            return false;
        }
        int showFlag = 0;
        if(pluginParam.getShowFlag()>0){
            showFlag = 1;
        }
        for (Long pluginId : pluginParam.getIds() ) {
            pluginService.showBatch(pluginId,showFlag);
        }
        return true;
    }

    @ApiOperation("修改插件")
    @PostMapping("/updatePlugin")
    public Integer updatePlugin(@RequestBody PluginParam pluginParam) {
        if(pluginParam.getId()==null){
            return 0;
        }
        return pluginService.updatePlugin(pluginParam);
    }

    @ApiOperation("批量导出插件")
    @PostMapping("/downloadBatch")
    public String downloadBatch(@RequestBody PluginParam pluginParam) {
        if(pluginParam.getIds()==null || pluginParam.getIds().size()<1){
            return null;
        }
        return  pluginService.downloadBatch(pluginParam.getIds());
    }

}
