package io.dataease.controller.themeModelPortal;

import cn.hutool.json.JSONObject;
import io.dataease.dto.panel.PanelShareDto;
import io.dataease.service.themeModelPortal.ThemeModelPortalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "主题模型门户")
@RequestMapping("/themeModelPortal")
@RestController
public class ThemeModelPortalController {
    @Resource
    private ThemeModelPortalService themeModelPortalService;
    @ApiOperation("主题模型门户：分页数据")
    @GetMapping("/queryPortal")
    public List<PanelShareDto> queryPortal(@RequestParam(required = false) Long time,
                                           @RequestParam(required = false) String keyWord) {
        return themeModelPortalService.queryByPage(time, keyWord);
    }
    @ApiOperation("主题模型门户:获取图片")
    @GetMapping("/getImage/{sourceId}")
    public ResponseEntity<FileSystemResource> getImage(@PathVariable String sourceId) {
        return themeModelPortalService.getImage(sourceId);
    }
}
