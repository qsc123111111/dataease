package io.dataease.service.authModel;

import com.alibaba.fastjson.JSONObject;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.request.authModel.VAuthModelRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.ext.ExtVAuthModelMapper;
import io.dataease.plugins.common.base.domain.DatasetGroup;
import io.dataease.service.dataset.DataSetGroupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModel(request);
        result.stream().forEach(vAuthModelDTO -> {
            if (vAuthModelDTO.getModelInnerType().equals("group")){
                vAuthModelDTO.setDirType(dataSetGroupService.getDirTypeById(vAuthModelDTO.getId()));
            }
        });
        if (CollectionUtils.isEmpty(result)) {
            return new ArrayList<>();
        }
        if (request.getPrivileges() != null) {
            result = filterPrivileges(request, result);
        }
        if (request.isClearEmptyDir()) {
            List<VAuthModelDTO> vAuthModelDTOS = TreeUtils.mergeTree(result);
            setAllLeafs(vAuthModelDTOS);
            removeEmptyDir(vAuthModelDTOS);
            return vAuthModelDTOS;
        }
        return TreeUtils.mergeTree(result);
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
        request.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<VAuthModelDTO> result = extVAuthModelMapper.queryAuthModel(request);
        result.stream().forEach(vAuthModelDTO -> {
            if (vAuthModelDTO.getModelInnerType().equals("group")){
                vAuthModelDTO.setDirType(dataSetGroupService.getDirTypeById(vAuthModelDTO.getId()));
            }
        });
        result.forEach(vAuthModelDTO -> {
            try {
                if ("group".equalsIgnoreCase(vAuthModelDTO.getModelInnerType()) && vAuthModelDTO.getDirType() == 0) {
                    System.out.println("vAuthModelDTO = " + vAuthModelDTO);
                }
            } catch (Exception e) {
                log.error("error", vAuthModelDTO.toString());
            }
        });
        List<VAuthModelDTO> collect = result.stream().filter(vAuthModelDTO ->
                "group".equalsIgnoreCase(vAuthModelDTO.getModelInnerType()) && vAuthModelDTO.getDirType() == 0
        ).collect(Collectors.toList());
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
        order = "`" + order + "`";
        List<DatasetGroup> data = dataSetGroupService.page(id,pageNo,pageSize,keyWord,order,time,plusOneTime);
        Long count = dataSetGroupService.count(id,pageNo,pageSize,keyWord,order,time,plusOneTime);
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
}

