package io.dataease.service.themeModelPortal;

import cn.hutool.json.JSONObject;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.dto.panel.PanelShareDto;
import io.dataease.plugins.common.base.domain.DatalabelGroup;
import io.dataease.plugins.common.base.domain.PanelShare;
import io.dataease.plugins.common.base.mapper.PanelShareMapper;
import io.dataease.service.panel.ShareService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service("themeModelPortalService")
public class ThemeModelPortalService {
    @Value("${upload.img.path}")
    private String UPLOAD_DIR;
    @Resource
    private ShareService shareService;
    public List<PanelShareDto> queryByPage(Long time, String keyWord) {
        Long plusOneTime = null;
        if (time != null){
            //将当前的时间戳+1天 作为查询时间范围
            // 将时间戳转换为Instant对象
            ZoneId chinaZone = ZoneId.of("Asia/Shanghai");
            Instant instant = Instant.ofEpochMilli(time);
            // 将Instant对象转换为LocalDate对象
            LocalDate localDate = instant.atZone(chinaZone).toLocalDate();
            LocalDate newDate = localDate.plusDays(1);
            plusOneTime = newDate.atStartOfDay(chinaZone).toInstant().toEpochMilli();
        }
//        long total = panelShareMapper.count(keyWord,time,plusOneTime);
        List<PanelShareDto> panelShareDtos = shareService.queryTreeLimit(time,plusOneTime,keyWord);
        List<PanelShareDto> allChildren = new ArrayList<>();
        panelShareDtos.stream().forEach(panelShareDto -> {
            if (panelShareDto.getChildren() != null){
                allChildren.addAll(panelShareDto.getChildren());
            }
        });
        return allChildren;
    }

    public ResponseEntity<FileSystemResource> getImage(String sourceId) {
        // 构建目标路径
        String os = System.getProperty("os.name");
        String path = UPLOAD_DIR;
        if (os.toLowerCase().startsWith("win")) {
            path = "D:" + path;
        }
        Path targetPath = Paths.get(path, sourceId + ".png");
        //获取Path的File对象
        //判断文件是否存在 存在就删除
        File imageFile = targetPath.toFile();
        if (imageFile.exists()) {
            FileSystemResource resource = new FileSystemResource(imageFile);
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .contentLength(imageFile.length())
                    .body(resource);
        } else {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }
}
