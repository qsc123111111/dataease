package io.dataease.controller.panel.server;

import com.alibaba.fastjson.JSON;
import io.dataease.commons.model.AuthURD;
import io.dataease.controller.panel.api.ShareApi;
import io.dataease.controller.request.panel.PanelShareFineDto;
import io.dataease.controller.request.panel.PanelShareRemoveRequest;
import io.dataease.controller.request.panel.PanelShareSearchRequest;
import io.dataease.dto.panel.PanelShareDto;
import io.dataease.dto.panel.PanelShareOutDTO;
import io.dataease.dto.panel.PanelSharePo;
import io.dataease.plugins.common.base.domain.PanelGroupWithBLOBs;
import io.dataease.plugins.common.base.domain.PanelShare;
import io.dataease.plugins.common.base.mapper.PanelGroupMapper;
import io.dataease.service.panel.ShareService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class ShareServer implements ShareApi {
    @Value("${upload.img.path}")
    private String UPLOAD_DIR;
    @Resource
    private PanelGroupMapper panelGroupMapper;

    @Resource
    private ShareService shareService;

    @Override
    public List<PanelShareDto> treeList() {
        return shareService.queryTree();
    }

    @Override
    public List<PanelSharePo> shareOut() {
        return shareService.queryShareOut();
    }

    @Override
    public List<PanelShare> queryWithResourceId(@RequestBody PanelShareSearchRequest request) {
        return shareService.queryWithResource(request);
    }

    @Override
    public List<PanelShareOutDTO> queryTargets(@PathVariable("panelId") String panelId) {
        return shareService.queryTargets(panelId);
    }

    @Override
    public void fineSave(@RequestBody PanelShareFineDto panelShareFineDto) {
        shareService.fineSave(panelShareFineDto);
    }

    @Override
    public void fineSaveImg(MultipartFile file,String resourceId) throws IOException {
        if (StringUtils.isEmpty(resourceId)){
            throw new RuntimeException("资源id不能为空");
        }
        //判断客户端是windows还是linux
        String os = System.getProperty("os.name");
        String path = UPLOAD_DIR;
        if (os.toLowerCase().startsWith("win")) {
            path = "D:" + path;
        }
        //判断路径是否存在 不存在就创建
        File existFile = new File(path);
        if (!existFile.exists()) {
            existFile.mkdirs();
        }
        // 获取文件名
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        //获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        fileName = resourceId + suffix;
        // 构建目标路径
        Path targetPath = Paths.get(UPLOAD_DIR, fileName);
        //判断文件是否存在 存在就删除
        if (Files.exists(targetPath)) {
            Files.delete(targetPath);
        }
        // 保存文件
        Files.copy(file.getInputStream(), targetPath);
        PanelShareFineDto panelShareFine = new PanelShareFineDto();
        AuthURD authURD = new AuthURD();
        authURD.setDeptIds(new ArrayList<>());
        authURD.setRoleIds(Arrays.asList(2L, 1L));
        panelShareFine.setAuthURD(authURD);
        panelShareFine.setResourceId(resourceId);
        shareService.fineSave(panelShareFine);
        //将模板进行发布
        PanelGroupWithBLOBs panelGroup = new PanelGroupWithBLOBs();
        panelGroup.setId(resourceId);
        panelGroup.setStatus("publish");
        panelGroupMapper.updateByPrimaryKeySelective(panelGroup);
    }

    @Override
    public void removeShares(@RequestBody PanelShareRemoveRequest request) {
        shareService.removeShares(request);
    }

    @Override
    public void removePanelShares(String panelId) {
        shareService.removeSharesyPanel(panelId);
    }
}
