package io.dataease.service.authModel;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.request.authModel.VAuthModelPageRequest;
import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.dto.authModel.modelCacheEnum;
import io.dataease.ext.ExtVAuthModelMapper;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.DatasetGroup;
import io.dataease.plugins.common.base.mapper.DatasetGroupMapper;
import io.dataease.service.dataset.DataSetGroupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 * Date: 2021/11/24
 * Description:
 */
@Service
@Slf4j
public class VAuthModelService {
    @Resource
    private DataSetGroupService dataSetGroupService;

    @Resource
    private ExtVAuthModelMapper extVAuthModelMapper;

    @Resource
    private DatasetGroupMapper datasetGroupMapper;

    public List<VAuthModelDTO> queryAuthModelByIds(String modelType, List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModelByIds(String.valueOf(AuthUtils.getUser().getUserId()), modelType, ids);
        if (CollectionUtils.isEmpty(result)) {
            return new ArrayList<>();
        } else {
            return result;
        }
    }

    public List<VAuthModelDTO> queryAuthModel(VAuthModelRequest request) {
        request.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        ArrayList<String> ids = new ArrayList<>();
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModel(request);
        result.stream().forEach(vAuthModelDTO -> {
            if (vAuthModelDTO.getModelInnerType().equals("group")){
                DatasetGroup datasetGroup = datasetGroupMapper.selectByPrimaryKey(vAuthModelDTO.getId());
                vAuthModelDTO.setDirType(datasetGroup.getDirType());
                vAuthModelDTO.setUpDown(datasetGroup.getUpDown());
                if (datasetGroup.getUpDown() == 0){
                    ids.add(vAuthModelDTO.getId());
                }
            }
        });
        List<VAuthModelDTO> collect = result.stream().filter(r -> !ids.contains(r.getId()) && !ids.contains(r.getPid())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return new ArrayList<>();
        }
        if (request.getPrivileges() != null) {
            collect = filterPrivileges(request, collect);
        }
        if (request.isClearEmptyDir()) {
            List<VAuthModelDTO> vAuthModelDTOS = TreeUtils.mergeTree(collect);
            setAllLeafs(vAuthModelDTOS);
            removeEmptyDir(vAuthModelDTOS);
            return vAuthModelDTOS;
        }
        return TreeUtils.mergeTree(collect);
    }

    public List<VAuthModelDTO> queryAuthModelByUser(VAuthModelRequest request) {
        request.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        ArrayList<String> ids = new ArrayList<>();
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModelByUser(request);
        result.stream().forEach(vAuthModelDTO -> {
            if (vAuthModelDTO.getModelInnerType().equals("group")){
                //待优化
                DatasetGroup datasetGroup = datasetGroupMapper.selectByPrimaryKey(vAuthModelDTO.getId());
                vAuthModelDTO.setDirType(datasetGroup.getDirType());
                vAuthModelDTO.setUpDown(datasetGroup.getUpDown());
                if (datasetGroup.getUpDown() == 0){
                    ids.add(vAuthModelDTO.getId());
                }
            }
        });
        List<VAuthModelDTO> collect = result.stream().filter(r ->
                           !ids.contains(r.getId())
                        && !ids.contains(r.getPid())
                        && !(ObjectUtil.isNotEmpty(r.getDirType())&& ObjectUtil.isNotEmpty(r.getCreateBy()) && r.getDirType() == 1 && !Objects.equals(r.getCreateBy(), AuthUtils.getUser().getUsername()))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return new ArrayList<>();
        }
        if (request.getPrivileges() != null) {
            collect = filterPrivileges(request, collect);
        }
        if (request.isClearEmptyDir()) {
            List<VAuthModelDTO> vAuthModelDTOS = TreeUtils.mergeTree(collect);
            setAllLeafs(vAuthModelDTOS);
            removeEmptyDir(vAuthModelDTOS);
            return vAuthModelDTOS;
        }
        return TreeUtils.mergeTree(collect);
    }

    private List<VAuthModelDTO> filterPrivileges(VAuthModelRequest request, List<VAuthModelDTO> result) {
        if (AuthUtils.getUser().getIsAdmin()) {
            return result;
        }
        if (request.getPrivileges() != null) {
            result = result.stream().filter(vAuthModelDTO -> "spine".equalsIgnoreCase(vAuthModelDTO.getNodeType())
                    || ("leaf".equalsIgnoreCase(vAuthModelDTO.getNodeType())
                    && vAuthModelDTO.getPrivileges() != null
                    && vAuthModelDTO.getPrivileges().contains(request.getPrivileges()))).collect(Collectors.toList());
        }
        return result;
    }

    private void removeEmptyDir(List<VAuthModelDTO> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        Iterator<VAuthModelDTO> iterator = result.listIterator();
        while (iterator.hasNext()) {
            VAuthModelDTO tmp = iterator.next();
            if ("spine".equalsIgnoreCase(tmp.getNodeType()) && tmp.getAllLeafs() == 0) {
                iterator.remove();
            } else {
                removeEmptyDir(tmp.getChildren());
            }
        }
    }

    private void setAllLeafs(List<VAuthModelDTO> result) {
        for (VAuthModelDTO vAuthModelDTO : result) {
            if (CollectionUtils.isEmpty(vAuthModelDTO.getChildren())) {
                vAuthModelDTO.setAllLeafs(0);
                continue;
            }
            long leafs = 0L;
            for (VAuthModelDTO child : vAuthModelDTO.getChildren()) {
                if ("leaf".equalsIgnoreCase(child.getNodeType())) {
                    leafs = leafs + 1;
                } else {
                    leafs = +leafs + getLeafs(child);
                }
            }
            vAuthModelDTO.setAllLeafs(leafs);
        }
    }

    private long getLeafs(VAuthModelDTO child) {
        long leafs = 0L;
        if (CollectionUtils.isEmpty(child.getChildren())) {
            child.setAllLeafs(0);
            return leafs;
        }
        for (VAuthModelDTO childChild : child.getChildren()) {
            if ("leaf".equalsIgnoreCase(childChild.getNodeType())) {
                leafs = leafs + 1;
            } else {
                leafs = +leafs + getLeafs(childChild);
            }
        }
        child.setAllLeafs(leafs);
        return leafs;
    }

    public List<VAuthModelDTO> queryAuthModelByIdsAddObject(String modelType, List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModelByIds(String.valueOf(AuthUtils.getUser().getUserId()), modelType, ids);
        if (CollectionUtils.isEmpty(result)) {
            return new ArrayList<>();
        } else {
            return result;
        }
    }

    public List<VAuthModelDTO> queryGroup(VAuthModelRequest request) {
        long startTime = System.currentTimeMillis();
        System.out.println("[queryGroup] 开始");

        long userStartTime = System.currentTimeMillis();
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        request.setUserId(userId);
        System.out.println("[queryGroup] 获取并设置用户ID=" + userId + " 耗时: " + (System.currentTimeMillis() - userStartTime) + " ms");

        long dbStartTime = System.currentTimeMillis();
        System.out.println("[queryGroup] 开始数据库查询...");
        // TODO 查询时间最长
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModel(request);

        System.out.println("result:"+result);
        System.out.println("[queryGroup] 数据库查询完成，耗时: " + (System.currentTimeMillis() - dbStartTime) + " ms，查询结果数量: " + result.size());

        long dirTypeStartTime = System.currentTimeMillis();
        System.out.println("[queryGroup] 开始补充 dirType...");
        result.stream().forEach(vAuthModelDTO -> {
            String modelInnerType = vAuthModelDTO.getModelInnerType();
            if (modelInnerType == null){
                System.out.println("[queryGroup] modelInnerType 为空, id=" + vAuthModelDTO.getId());
            }
            if ("group".equals(vAuthModelDTO.getModelInnerType())){
                long dirTypeCallStart = System.currentTimeMillis();
                vAuthModelDTO.setDirType(dataSetGroupService.getDirTypeById(vAuthModelDTO.getId()));
                System.out.println("[queryGroup] 调用 getDirTypeById，id=" + vAuthModelDTO.getId() +
                        ", 耗时: " + (System.currentTimeMillis() - dirTypeCallStart) + " ms");
            }
        });
        System.out.println("[queryGroup] 补充 dirType 总耗时: " + (System.currentTimeMillis() - dirTypeStartTime) + " ms");

        long filterStartTime = System.currentTimeMillis();
        System.out.println("[queryGroup] 开始过滤 group + dirType=0...");
        List<VAuthModelDTO> collect = result.stream().filter(vAuthModelDTO ->
                "group".equalsIgnoreCase(vAuthModelDTO.getModelInnerType()) && vAuthModelDTO.getDirType() == 0
        ).collect(Collectors.toList());
        System.out.println("[queryGroup] 过滤完成，耗时: " + (System.currentTimeMillis() - filterStartTime) + " ms，过滤后数量: " + collect.size());

        if (CollectionUtils.isEmpty(collect)) {
            System.out.println("[queryGroup] 没有符合条件的数据，结束，总耗时: " + (System.currentTimeMillis() - startTime) + " ms");
            return new ArrayList<>();
        }

        if (request.getPrivileges() != null) {
            System.out.println("[queryGroup] 开始执行权限过滤...");
            long privilegeStartTime = System.currentTimeMillis();
            collect = filterPrivileges(request, collect);
            System.out.println("[queryGroup] 权限过滤完成，耗时: " + (System.currentTimeMillis() - privilegeStartTime) + " ms，过滤后数量: " + collect.size());
        } else {
            System.out.println("[queryGroup] 没有传入权限信息，跳过权限过滤");
        }

        if (request.isClearEmptyDir()) {
            System.out.println("[queryGroup] 请求清理空目录，走清理逻辑");
            long treeStartTime = System.currentTimeMillis();
            List<VAuthModelDTO> vAuthModelDTOS = TreeUtils.mergeTree(collect);
            System.out.println("[queryGroup] mergeTree 耗时: " + (System.currentTimeMillis() - treeStartTime) + " ms，合并后树节点数: " + vAuthModelDTOS.size());

            long leafStartTime = System.currentTimeMillis();
            setAllLeafs(vAuthModelDTOS);
            System.out.println("[queryGroup] setAllLeafs 耗时: " + (System.currentTimeMillis() - leafStartTime) + " ms");

            long removeEmptyStartTime = System.currentTimeMillis();
            removeEmptyDir(vAuthModelDTOS);
            System.out.println("[queryGroup] removeEmptyDir 耗时: " + (System.currentTimeMillis() - removeEmptyStartTime) + " ms");

            System.out.println("[queryGroup] 全部完成，总耗时: " + (System.currentTimeMillis() - startTime) + " ms");
            return vAuthModelDTOS;
        } else {
            System.out.println("[queryGroup] 不清理空目录，直接合并树");
            long treeStartTime = System.currentTimeMillis();
            List<VAuthModelDTO> merged = TreeUtils.mergeTree(collect);
            System.out.println("[queryGroup] mergeTree 耗时: " + (System.currentTimeMillis() - treeStartTime) + " ms，合并后树节点数: " + merged.size());

            System.out.println("[queryGroup] 全部完成，总耗时: " + (System.currentTimeMillis() - startTime) + " ms");
            return merged;
        }
    }


    public JSONObject queryModel(String id, Integer pageNo, Integer pageSize, String keyWord, String order, Long time) {
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
        //从dataset——group 查询 pid=id,dir_type=1的数据
        pageNo=(pageNo-1)*pageSize;
        order = "dg.create_time " + order;
        List<DatasetGroup> data = dataSetGroupService.page(AuthUtils.getUser().getUsername(), id,pageNo,pageSize,keyWord,order,time,plusOneTime);
        Long count = dataSetGroupService.count(AuthUtils.getUser().getUsername(), id,pageNo,pageSize,keyWord,order,time,plusOneTime);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",data);
        jsonObject.put("count",count);
        return jsonObject;
    }

    public List<VAuthModelDTO> detailChild(String id) {
        VAuthModelRequest request = new VAuthModelRequest();
        request.setModelType("dataset");
        request.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<VAuthModelDTO> endResult = new ArrayList<>();
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModel(request);
        result.stream().forEach(vAuthModelDTO -> {
            String id1 = vAuthModelDTO.getId();
            String pid = vAuthModelDTO.getPid();
            if (id1 != null && id1.equals(id)) {
                vAuthModelDTO.setPid("0");
                endResult.add(vAuthModelDTO);
            } else if (pid != null && pid.equals(id)) {
                endResult.add(vAuthModelDTO);
            }
        });
        return TreeUtils.mergeTree(endResult);
    }


    public Map<String,Object> page(VAuthModelPageRequest param){
        if(param==null || param.getPid()==null){
            return null;
        }
        try {
            int page = 1;
            if( param.getPage()!=null){
                page = param.getPage();
            }
            int pageSize = 10;
            if( param.getPageSize()!=null ){
                pageSize = param.getPageSize();
            }
            String limit = "limit "+ (page-1)*pageSize +","+pageSize;

            Long end_time = null;
            Long create_time = param.getCreate_time();
            if (create_time != null){
                //将当前的时间戳+1天 作为查询时间范围
                // 将时间戳转换为Instant对象
                ZoneId chinaZone = ZoneId.of("Asia/Shanghai");
                Instant instant = Instant.ofEpochMilli(create_time);
                // 将Instant对象转换为LocalDate对象
                LocalDate localDate = instant.atZone(chinaZone).toLocalDate();
                LocalDate newDate = localDate.plusDays(1);
                end_time = newDate.atStartOfDay(chinaZone).toInstant().toEpochMilli();
            }

            List<DatasetGroup> list = datasetGroupMapper.PageData(limit,param.getPid(), param.getKeyword(),param.getSort(), create_time, end_time);
            Long total = datasetGroupMapper.PageDataCount(param.getPid(),param.getKeyword(),create_time, end_time);

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);

            return result;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateModel(VAuthModelPageRequest param){
        if(param==null || param.getId()==null){
            return false;
        }
        try {
            int updateType = datasetGroupMapper.updateModel(param.getId(), param.getName(), param.getDesc());

            if(updateType>0){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}

