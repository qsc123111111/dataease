package io.dataease.service.dataset;

import dm.jdbc.driver.DmdbNClob;
import io.dataease.auth.annotation.DeCleaner;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.ext.ExtDataSetGroupMapper;
import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.SysAuthConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.controller.request.dataset.DataSetGroupRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.i18n.Translator;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.DatamodelRef;
import io.dataease.plugins.common.base.domain.DatasetGroup;
import io.dataease.plugins.common.base.domain.DatasetGroupExample;
import io.dataease.plugins.common.base.domain.DatasetTableExample;
import io.dataease.plugins.common.base.mapper.*;
import io.dataease.service.sys.SysAuthService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author gin
 * @Date 2021/2/20 8:10 下午
 */
@Service
public class DataSetGroupService {
    @Resource
    private TermTableMapper termTableMapper;
    @Resource
    private DatasetGroupMapper datasetGroupMapper;
    @Resource
    @Lazy
    private DataSetTableService dataSetTableService;
    @Resource
    private ExtDataSetGroupMapper extDataSetGroupMapper;
    @Resource
    private SysAuthService sysAuthService;
    @Resource
    private DatamodelRefMapper datamodelRefMapper;
    @Resource
    private DatalabelRefMapper datalabelRefMapper;
    @Resource
    private DatamodelMapper datamodelMapper;

    @DeCleaner(value = DePermissionType.DATASET, key = "pid")
    public DataSetGroupDTO save(DatasetGroup datasetGroup) throws Exception {
        checkNameByUser(datasetGroup);
        if (StringUtils.isEmpty(datasetGroup.getId())) {
            if (StringUtils.isEmpty(datasetGroup.getType())) {
                throw new Exception("type can not be empty");
            }
            datasetGroup.setId(UUID.randomUUID().toString());
            datasetGroup.setCreateBy(AuthUtils.getUser().getUsername());
            if (datasetGroup.getCreateTime() == null){
                datasetGroup.setCreateTime(System.currentTimeMillis());
            }
            datasetGroupMapper.insert(datasetGroup);
            DeLogUtils.save(SysLogConstants.OPERATE_TYPE.CREATE, SysLogConstants.SOURCE_TYPE.DATASET, datasetGroup.getId(), datasetGroup.getPid(), null, null);
            String userName = AuthUtils.getUser().getUsername();
            // 清理权限缓存
            CacheUtils.removeAll(AuthConstants.USER_PERMISSION_CACHE_NAME);
            sysAuthService.copyAuth(datasetGroup.getId(), SysAuthConstants.AUTH_SOURCE_TYPE_DATASET);
        } else {
            datasetGroupMapper.updateByPrimaryKeySelective(datasetGroup);
            DeLogUtils.save(SysLogConstants.OPERATE_TYPE.MODIFY, SysLogConstants.SOURCE_TYPE.DATASET, datasetGroup.getId(), datasetGroup.getPid(), null, null);
        }
        DataSetGroupDTO dataSetGroupDTO = new DataSetGroupDTO();
        BeanUtils.copyBean(dataSetGroupDTO, datasetGroup);
        dataSetGroupDTO.setLabel(dataSetGroupDTO.getName());
        dataSetGroupDTO.setDesc(dataSetGroupDTO.getDesc());
        return dataSetGroupDTO;
    }

    public void delete(String id) throws Exception {

        Assert.notNull(id, "id cannot be null");
        sysAuthService.checkTreeNoManageCount("dataset", id);

        DatasetGroup dg = datasetGroupMapper.selectByPrimaryKey(id);
        DataSetGroupRequest datasetGroup = new DataSetGroupRequest();
        BeanUtils.copyBean(datasetGroup, dg);
        Map<String, String> stringStringMap = extDataSetGroupMapper.searchIds(id, "dataset");
        String[] split = stringStringMap.get("ids").split(",");
        List<String> ids = new ArrayList<>();
        for (String dsId : split) {
            if (StringUtils.isNotEmpty(dsId)) {
                ids.add(dsId);
            }
        }
        DatasetGroupExample datasetGroupExample = new DatasetGroupExample();
        datasetGroupExample.createCriteria().andIdIn(ids);
        datasetGroupMapper.deleteByExample(datasetGroupExample);
        // 删除场景下的表和字段
        deleteTableAndField(ids);
    }

    public void deleteRef(String id) throws Exception {

        Assert.notNull(id, "id cannot be null");
        sysAuthService.checkTreeNoManageCount("dataset", id);

        DatasetGroup dg = datasetGroupMapper.selectByPrimaryKey(id);
        DataSetGroupRequest datasetGroup = new DataSetGroupRequest();
        BeanUtils.copyBean(datasetGroup, dg);
        Map<String, String> stringStringMap = extDataSetGroupMapper.searchIds(id, "dataset");
        String[] split = stringStringMap.get("ids").split(",");
        List<String> ids = new ArrayList<>();
        for (String dsId : split) {
            if (StringUtils.isNotEmpty(dsId)) {
                ids.add(dsId);
            }
        }
        DatasetGroupExample datasetGroupExample = new DatasetGroupExample();
        datasetGroupExample.createCriteria().andIdIn(ids);
        datasetGroupMapper.deleteByExample(datasetGroupExample);
        // 删除场景下的表和字段
        deleteTableAndField(ids);
        //删除创建的数据集
        List<DatamodelRef> refs = datamodelRefMapper.selectByModeId(id);
        for (DatamodelRef ref : refs) {
            dataSetTableService.delete(ref.getTableId());
            datamodelRefMapper.deleteById(ref.getId());
        }
        //删除 datamodel_ref
        datamodelRefMapper.deleteByModelId(id);
        //删除 datalebel_ref
        datalabelRefMapper.deleteByModelId(id);
        //删除 datamodel
        datamodelMapper.deleteByModelId(id);
    }

    public DatasetGroup getScene(String id) {
        return datasetGroupMapper.selectByPrimaryKey(id);
    }

    public void deleteTableAndField(List<String> sceneIds) throws Exception {
        for (String sceneId : sceneIds) {
            DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
            dataSetTableRequest.setSceneId(sceneId);
            List<DataSetTableDTO> list = dataSetTableService.list(dataSetTableRequest);
            for (DataSetTableDTO table : list) {
                dataSetTableService.delete1(table.getId());
            }
        }
    }

    public List<DataSetGroupDTO> treeNode(DataSetGroupRequest datasetGroup) {
        datasetGroup.setLevel(null);
        datasetGroup.setPid("0");
        datasetGroup.setType("group");
        datasetGroup.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<DataSetGroupDTO> treeInfo = extDataSetGroupMapper.search(datasetGroup);
        return TreeUtils.mergeTree(treeInfo);
    }

    public List<DataSetGroupDTO> tree(DataSetGroupRequest datasetGroup) {
        datasetGroup.setLevel(null);
        datasetGroup.setPid(null);
        datasetGroup.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<DataSetGroupDTO> treeInfo = extDataSetGroupMapper.search(datasetGroup);
        return TreeUtils.mergeTree(treeInfo);
    }

    public List<String> getAllId(List<DataSetGroupDTO> list, List<String> ids) {
        for (DataSetGroupDTO dto : list) {
            ids.add(dto.getId());
            if (CollectionUtils.isNotEmpty(dto.getChildren())) {
                getAllId(dto.getChildren(), ids);
            }
        }
        return ids;
    }
    public void checkNameByUser(DatasetGroup datasetGroup) {
        DatasetGroupExample datasetGroupExample = new DatasetGroupExample();
        DatasetGroupExample.Criteria criteria = datasetGroupExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetGroup.getPid())) {
            criteria.andPidEqualTo(datasetGroup.getPid());
        }
        if (StringUtils.isNotEmpty(datasetGroup.getType())) {
            criteria.andTypeEqualTo(datasetGroup.getType());
        }
        if (StringUtils.isNotEmpty(datasetGroup.getName())) {
            criteria.andNameEqualTo(datasetGroup.getName());
        }
        if (StringUtils.isNotEmpty(datasetGroup.getId())) {
            criteria.andIdNotEqualTo(datasetGroup.getId());
        }
        criteria.andCreateByEqualTo(AuthUtils.getUser().getUsername());
        List<DatasetGroupExample.Criteria> oredCriteria = datasetGroupExample.getOredCriteria();
        for (DatasetGroupExample.Criteria oredCriterion : oredCriteria) {
            List<DatasetGroupExample.Criterion> newCriteria = oredCriterion.getCriteria();
            for (DatasetGroupExample.Criterion newCriterion : newCriteria) {
                String condition = newCriterion.getCondition();
                condition = condition.replaceAll("`","\"");
                newCriterion.setCondition(condition);
            }
        }
        List<DatasetGroup> list = datasetGroupMapper.selectByExample(datasetGroupExample);
        if (list.size() > 0) {
            throw new RuntimeException(Translator.get("I18N_DATASET_GROUP_EXIST"));
        }
    }

    public void checkName(DatasetGroup datasetGroup) {
        DatasetGroupExample datasetGroupExample = new DatasetGroupExample();
        DatasetGroupExample.Criteria criteria = datasetGroupExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetGroup.getPid())) {
            criteria.andPidEqualTo(datasetGroup.getPid());
        }
        if (StringUtils.isNotEmpty(datasetGroup.getType())) {
            criteria.andTypeEqualTo(datasetGroup.getType());
        }
        if (StringUtils.isNotEmpty(datasetGroup.getName())) {
            criteria.andNameEqualTo(datasetGroup.getName());
        }
        if (StringUtils.isNotEmpty(datasetGroup.getId())) {
            criteria.andIdNotEqualTo(datasetGroup.getId());
        }
        List<DatasetGroupExample.Criteria> oredCriteria = datasetGroupExample.getOredCriteria();
        for (DatasetGroupExample.Criteria oredCriterion : oredCriteria) {
            List<DatasetGroupExample.Criterion> newCriteria = oredCriterion.getCriteria();
            for (DatasetGroupExample.Criterion newCriterion : newCriteria) {
                String condition = newCriterion.getCondition();
                condition = condition.replaceAll("`","\"");
                newCriterion.setCondition(condition);
            }
        }
        List<DatasetGroup> list = datasetGroupMapper.selectByExample(datasetGroupExample);
        if (list.size() > 0) {
            throw new RuntimeException(Translator.get("I18N_DATASET_GROUP_EXIST"));
        }
    }

    public List<DatasetGroup> getParents(String id) {
        List<DatasetGroup> list = new ArrayList<>();
        DatasetGroup datasetGroup = datasetGroupMapper.selectByPrimaryKey(id);
        list.add(datasetGroup);
        getParent(list, datasetGroup);
        Collections.reverse(list);
        return list;
    }

    public void getParent(List<DatasetGroup> list, DatasetGroup datasetGroup) {
        if (ObjectUtils.isNotEmpty(datasetGroup)) {
            if (StringUtils.isNotEmpty(datasetGroup.getPid())) {
                DatasetGroup d = datasetGroupMapper.selectByPrimaryKey(datasetGroup.getPid());
                list.add(d);
                getParent(list, d);
            }
        }
    }

    public Integer getDirTypeById(String id) {
        return datasetGroupMapper.getDirTypeById(id);
    }

    public List<DatasetGroup> page(String createBy, String id, Integer pageNo, Integer pageSize, String keyWord, String order, Long time, Long plusOneTime) {
        return datasetGroupMapper.page(createBy, id,pageNo,pageSize,keyWord,order,time,plusOneTime);
    }

    public Long count(String createBy, String id, Integer pageNo, Integer pageSize, String keyWord, String order, Long time, Long plusOneTime) {
        return datasetGroupMapper.count(createBy, id,pageNo,pageSize,keyWord,order,time,plusOneTime);
    }

    public int update(DatasetGroup errorDatasetGroup) {
        return datasetGroupMapper.updateByPrimaryKeySelective(errorDatasetGroup);
    }

    public DatasetGroup selectById(String id) {
        return datasetGroupMapper.selectById(id);
    }
}
