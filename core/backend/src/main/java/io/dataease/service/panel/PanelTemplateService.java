package io.dataease.service.panel;

import com.alibaba.fastjson.JSONObject;
import io.dataease.commons.utils.ZipUtils;
import io.dataease.controller.request.panel.PanelTemplatePageRequest;
import io.dataease.controller.request.panel.PanelTemplateParam;
import io.dataease.controller.request.panel.PanelTemplateReq;
import io.dataease.ext.ExtPanelTemplateMapper;
import io.dataease.commons.constants.CommonConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.panel.PanelTemplateRequest;
import io.dataease.dto.panel.PanelTemplateDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.PanelTemplate;
import io.dataease.plugins.common.base.domain.PanelTemplateExample;
import io.dataease.plugins.common.base.domain.PanelTemplateWithBLOBs;
import io.dataease.plugins.common.base.mapper.PanelTemplateMapper;
import io.dataease.service.staticResource.StaticResourceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static io.dataease.commons.constants.StaticResourceConstants.UPLOAD_URL_PREFIX;
import static io.dataease.commons.constants.StaticResourceConstants.USER_HOME;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Service
public class PanelTemplateService {

    @Resource
    private PanelTemplateMapper panelTemplateMapper;
    @Resource
    private ExtPanelTemplateMapper extPanelTemplateMapper;
    @Resource
    private StaticResourceService staticResourceService;

//    @Value("${template.resource.path}")
//    private String templateResourcePath;

    public List<PanelTemplateDTO> templateList(PanelTemplateRequest panelTemplateRequest) {
        panelTemplateRequest.setWithBlobs("N");
        List<PanelTemplateDTO> panelTemplateList = extPanelTemplateMapper.panelTemplateList(panelTemplateRequest);
        if (panelTemplateRequest.getWithChildren()) {
            getTreeChildren(panelTemplateList);
        }
        return panelTemplateList;
    }

    public void getTreeChildren(List<PanelTemplateDTO> parentPanelTemplateDTO) {
        Optional.ofNullable(parentPanelTemplateDTO).ifPresent(parent -> parent.forEach(panelTemplateDTO -> {
            List<PanelTemplateDTO> panelTemplateDTOChildren = extPanelTemplateMapper.panelTemplateList(new PanelTemplateRequest(panelTemplateDTO.getId()));
            panelTemplateDTO.setChildren(panelTemplateDTOChildren);
            getTreeChildren(panelTemplateDTOChildren);
        }));
    }

    public List<PanelTemplateDTO> getSystemTemplateType(PanelTemplateRequest panelTemplateRequest) {
        return extPanelTemplateMapper.panelTemplateList(panelTemplateRequest);
    }

    @Transactional
    public PanelTemplateDTO save(PanelTemplateRequest request) {
        if (StringUtils.isEmpty(request.getId())) {
            request.setId(UUID.randomUUID().toString());
            request.setCreateTime(System.currentTimeMillis());
            request.setCreateBy(AuthUtils.getUser().getUsername());
            //如果level 是0（第一级）指的是分类目录 设置父级为对应的templateType
            if (request.getLevel() == 0) {
                request.setPid(request.getTemplateType());
                String nameCheckResult = this.nameCheck(CommonConstants.OPT_TYPE.INSERT, request.getName(), request.getPid(), null);
                if (CommonConstants.CHECK_RESULT.EXIST_ALL.equals(nameCheckResult)) {
                    DataEaseException.throwException(Translator.get("i18n_same_folder_can_not_repeat"));
                }
            } else {//模板插入 相同文件夹同名的模板进行覆盖(先删除)
                PanelTemplateExample exampleDelete = new PanelTemplateExample();
                exampleDelete.createCriteria().andPidEqualTo(request.getPid()).andNameEqualTo(request.getName());
                panelTemplateMapper.deleteByExample(exampleDelete);
            }
            if ("template".equals(request.getNodeType())) {
                //Store static resource into the server
                staticResourceService.saveFilesToServe(request.getStaticResource());
                String snapshotName = "template-" + request.getId() + ".jpeg";
                staticResourceService.saveSingleFileToServe(snapshotName, request.getSnapshot().replace("data:image/jpeg;base64,", ""));
                request.setSnapshot("/" + UPLOAD_URL_PREFIX + '/' + snapshotName);
            }

            panelTemplateMapper.insert(request);
        } else {
            String nameCheckResult = this.nameCheck(CommonConstants.OPT_TYPE.UPDATE, request.getName(), request.getPid(), request.getId());
            if (CommonConstants.CHECK_RESULT.EXIST_ALL.equals(nameCheckResult)) {
                DataEaseException.throwException(Translator.get("i18n_same_folder_can_not_repeat"));
            }
            panelTemplateMapper.updateByPrimaryKeySelective(request);
        }
        PanelTemplateDTO panelTemplateDTO = new PanelTemplateDTO();
        BeanUtils.copyBean(panelTemplateDTO, request);
        panelTemplateDTO.setLabel(request.getName());
        return panelTemplateDTO;
    }

    //名称检查
    public String nameCheck(String optType, String name, String pid, String id) {
        PanelTemplateExample example = new PanelTemplateExample();
        if (CommonConstants.OPT_TYPE.INSERT.equals(optType)) {
            example.createCriteria().andPidEqualTo(pid).andNameEqualTo(name);

        } else if (CommonConstants.OPT_TYPE.UPDATE.equals(optType)) {
            example.createCriteria().andPidEqualTo(pid).andNameEqualTo(name).andIdNotEqualTo(id);
        }
        List<PanelTemplate> panelTemplates = panelTemplateMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(panelTemplates)) {
            return CommonConstants.CHECK_RESULT.NONE;
        } else {
            return CommonConstants.CHECK_RESULT.EXIST_ALL;
        }
    }

    public String nameCheck(PanelTemplateRequest request) {
        return nameCheck(request.getOptType(), request.getName(), request.getPid(), request.getId());

    }

    public void delete(String id) {
        Assert.notNull(id, "id cannot be null");
        panelTemplateMapper.deleteByPrimaryKey(id);
    }

    public PanelTemplateWithBLOBs findOne(String panelId) {
        return panelTemplateMapper.selectByPrimaryKey(panelId);
    }

    public List<PanelTemplateDTO> find(PanelTemplateRequest panelTemplateRequest) {
        return extPanelTemplateMapper.panelTemplateList(panelTemplateRequest);
    }


    public Map<String,Object> pageList(PanelTemplatePageRequest request){
        int page = 1;
        int pageSize = 10;
        String name = null;
        if(request!=null){
            if(request.getPage()!=null && request.getPage()>0){
                page = request.getPage();
            }
            if(request.getPageSize()!=null && request.getPageSize()>0){
                pageSize = request.getPageSize();
            }
            if( !StringUtils.isEmpty(request.getName()) ){
                name = request.getName();
            }
        }
        String limit = "limit " + (page-1)*pageSize + "," + pageSize;

        List<PanelTemplate> data =  panelTemplateMapper.pageList(limit, name);
        int total = panelTemplateMapper.pageCount(name);
        Map<String,Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total);
        return result;
    }


    public int templateShow(PanelTemplateParam request){
        if(request ==null || request.getIds()==null || request.getIds().size()<1 || request.getShowFlag()==null){
            return 0;
        }
        int count = 0;
        try {

            List<String> ids = request.getIds();
            for (String id : ids ) {
                int updateType =  panelTemplateMapper.updateStatus( id, request.getShowFlag());
                if(updateType>0){
                    count++;
                }
            }
        }catch (Exception e){}
        return count;
    }

    public int deleteBatch(PanelTemplateParam request) {
        if(request ==null || request.getIds()==null || request.getIds().size()<1  ){
            return 0;
        }
        int count = 0;
        try {

            List<String> ids = request.getIds();
            for (String id : ids ) {
                int removeType =  panelTemplateMapper.deleteByPrimaryKey(id);
                if(removeType>0){
                    count++;
                }
            }
        }catch (Exception e){}

        return count;
    }

    public String downloadBatch(PanelTemplateParam request){
        try {
            if(request ==null || request.getIds()==null || request.getIds().size()<1  ){
                return "error";
            }

            List<String> pathList = new ArrayList<>();//获取文件

            List<String> ids = request.getIds();
            for (String id : ids) {
                String filePath = USER_HOME + "/template/data/" + id + ".DET";
                pathList.add(filePath);
            }

            Random random = new Random();
            int randCount = random.nextInt(9000)+1000;

            String zipPath = USER_HOME + "/static-resource/zip/" ;
            if( ZipUtils.checkPath(zipPath)==false ){
                System.out.println("路径创建有误！");
                return "error";
            }

            String zipName = "temp"+ System.currentTimeMillis() + randCount + ".zip";
            int zipCount = ZipUtils.compress(pathList, zipPath+zipName );
            System.out.println("成功压缩"+zipCount+"个文件");
            if(zipCount>0){
                return "/static-resource/zip/"+zipName;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "error";
    }
    @Transactional
    public Integer uploadBatch(MultipartFile[] templateFiles,String templateType){
        String tempPath = USER_HOME+"/template/data/";
        if( ZipUtils.checkPath(tempPath)==false ){
            System.out.println("路径创建有误！");
            return 0;
        }

        int count = 0;
        for ( MultipartFile templateFile  : templateFiles) {
            try {
                if( templateFile==null){
                    System.out.println("模板文件为空,跳过!");
                    continue;//跳过
                }
                String originalName = templateFile.getOriginalFilename();
                if( originalName==null || originalName.indexOf(".")==-1 ){
                    System.out.println("文件名不可用,跳过!");
                    continue;//跳过
                }
                String ext = originalName.substring( originalName.lastIndexOf(".") );
                if( ".DET".equals(ext)==false ){
                    System.out.println("文件格式有误,跳过!");
                    continue;//跳过
                }

                InputStream inputStream = templateFile.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);

                String body = "";
                String line;
                while ( (line=reader.readLine())!=null ){
                    body += line;
                }

                if( StringUtils.isEmpty(body) || JSONObject.parseObject(body)==null){
                    System.out.println("文件内容为空,跳过!");
                    continue;//跳过
                }

                JSONObject bodyJson = JSONObject.parseObject(body);
                if( bodyJson.getString("name")==null || bodyJson.getString("templateType")==null || bodyJson.getString("snapshot")==null || bodyJson.getString("panelStyle")==null ||
                        bodyJson.getString("panelData")==null || bodyJson.getString("dynamicData")==null || bodyJson.getString("staticResource")==null ){
                    //name, templateType, snapshot-snapshot, panelStyle-template_style, panelData-template_data, dynamicData-dynamic_data, staticResource
                    System.out.println("内容json格式有误,跳过!");
                    continue;
                }

                //保存
                String uuid = UUID.randomUUID().toString();

                PanelTemplateRequest template = new PanelTemplateRequest();
                template.setPid(panelTemplateMapper.findIdByType(templateType));
                template.setLevel(1);
                template.setNodeType("template");
                template.setTemplateType(templateType);
                template.setName( bodyJson.getString("name") );
                template.setSnapshot( bodyJson.getString("snapshot") );
                template.setTemplateStyle( bodyJson.getString("panelStyle") );
                template.setTemplateData( bodyJson.getString("panelData") );
                template.setDynamicData( bodyJson.getString("dynamicData") );
                template.setStaticResource( bodyJson.getString("staticResource"));

                template.setId(uuid);
                template.setCreateTime(System.currentTimeMillis());
                template.setCreateBy(AuthUtils.getUser().getUsername());

                staticResourceService.saveFilesToServe(template.getStaticResource());
                String snapshotName = "template-" + template.getId() + ".jpeg";
                staticResourceService.saveSingleFileToServe(snapshotName, template.getSnapshot().replace("data:image/jpeg;base64,", ""));
                template.setSnapshot("/" + UPLOAD_URL_PREFIX + '/' + snapshotName);

                int saveType = panelTemplateMapper.insert(template);
                if(saveType<1){
                    System.out.println("数据未入库,跳过!");
                    continue;
                }

                String filename = uuid + ext;
                File file = new File(tempPath + filename);
                try {
                    templateFile.transferTo(file);
                }catch (Exception file_e){
                    System.out.println("文件保存本地异常!");
                    file.delete();
                    panelTemplateMapper.deleteByPrimaryKey(uuid);
                    continue;
                }
                count++;

            }catch (Exception e){ e.printStackTrace(); }
        }
        return count;
    }

    public Integer templateEdit(PanelTemplateReq templateEditReq) {
        return panelTemplateMapper.updateNameAndFlagById(templateEditReq.getId(),templateEditReq.getName(),templateEditReq.getTemplateType());
    }

}
