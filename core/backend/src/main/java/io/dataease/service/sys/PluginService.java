package io.dataease.service.sys;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.google.gson.Gson;
import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.*;
import io.dataease.dto.MyPluginDTO;
import io.dataease.ext.ExtSysPluginMapper;
import io.dataease.i18n.Translator;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.MyPlugin;
import io.dataease.plugins.common.base.mapper.MyPluginMapper;
import io.dataease.plugins.common.request.KeywordRequest;
import io.dataease.plugins.common.request.PluginParam;
import io.dataease.plugins.config.LoadjarUtil;
import io.dataease.plugins.entity.PluginOperate;
import io.dataease.service.datasource.DatasourceService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import static io.dataease.commons.constants.StaticResourceConstants.USER_HOME;


@Service
public class PluginService {

    @Value("${dataease.plugin.dir:/opt/dataease/plugins/}")
    private String pluginDir;

    @Value("${dataease.plugin.dir:/opt/dataease/data/plugin/data/}")
    private String pluginPath;

    private final static String pluginJsonName = "plugin.json";

    @Resource
    private ExtSysPluginMapper extSysPluginMapper;

    @Resource
    private MyPluginMapper myPluginMapper;

    @Resource
    private DatasourceService datasourceService;

    @Autowired
    private LoadjarUtil loadjarUtil;

    @Autowired(required = false)
    private DistributedPluginService distributedPluginService;

    @Value("${version}")
    private String version;


    public List<MyPlugin> query(KeywordRequest request) {
        return extSysPluginMapper.query(request);
    }

    public void systemUpgrade() {
        extSysPluginMapper.updateVersion(version);
    }

    /**
     * 从本地安装处插件
     *
     * @param file
     * @return
     */
    public Map<String, Object> localInstall(MultipartFile file) throws Exception {
        //0.文件存储指定路径
        String filename = DeFileUtils.uploadPlugin(file, pluginPath);
        if(filename==null){
            return null;
        }

        //1.上传文件到服务器pluginDir目录下
        File dest = DeFileUtils.upload(file, pluginDir + "temp/");
        //2.解压目标文件dest 得到plugin.json和jar
        String folder = pluginDir + "folder/";
        try {
            ZipUtil.unzip(dest.getAbsolutePath(), folder);
        } catch (Exception e) {
            DeFileUtils.deleteFile(pluginDir + "temp/");
            DeFileUtils.deleteFile(folder);
            // 需要删除文件
            LogUtil.error(e.getMessage(), e);
            DEException.throwException(e);
        }
        //3.解析plugin.json 失败则 直接返回错误 删除文件
        File folderFile = new File(folder);
        File[] jsonFiles = folderFile.listFiles(this::isPluginJson);
        if (ArrayUtils.isEmpty(jsonFiles)) {
            DeFileUtils.deleteFile(pluginDir + "temp/");
            DeFileUtils.deleteFile(folder);
            String msg = "缺少插件描述文件【plugin.json】";
            LogUtil.error(msg);
            DEException.throwException(msg);
        }
        MyPluginDTO myPlugin = formatJsonFile(jsonFiles[0]);
        myPlugin.setCreator(AuthUtils.getUser().getUsername());

        if (!versionMatch(myPlugin.getRequire())) {
            String msg = "当前插件要求系统版本最低为：" + myPlugin.getRequire();
            LogUtil.error(msg);
            DEException.throwException(msg);
        }
        //4.加载jar包 失败则 直接返回错误 删除文件
        File[] jarFiles = folderFile.listFiles(this::isPluginJar);
        if (ArrayUtils.isEmpty(jarFiles)) {
            DeFileUtils.deleteFile(pluginDir + "temp/");
            DeFileUtils.deleteFile(folder);
            String msg = "缺少插件jar文件";
            LogUtil.error(msg);
            DEException.throwException(msg);
        }

        if (pluginExist(myPlugin)) {
            String msg = "插件【" + myPlugin.getName() + "】已存在，请先卸载";
            LogUtil.error(msg);
            DEException.throwException(msg);
        }
        String targetDir = null;
        try {
            File jarFile = jarFiles[0];
            targetDir = makeTargetDir(myPlugin);
            String jarPath;
            jarPath = DeFileUtils.copy(jarFile, targetDir);
            if (myPlugin.getCategory().equalsIgnoreCase("datasource")) {
                DeFileUtils.copyFolder(folder + "/" + myPlugin.getDsType() + "Driver", targetDir + myPlugin.getDsType() + "Driver");
            }
            loadJar(jarPath, myPlugin);
            myPlugin.setUpdateTime(System.currentTimeMillis());
            myPlugin.setFilename(filename);
            myPluginMapper.insert(myPlugin);

            CacheUtils.removeAll(AuthConstants.USER_CACHE_NAME);
            CacheUtils.removeAll(AuthConstants.USER_ROLE_CACHE_NAME);
            CacheUtils.removeAll(AuthConstants.USER_PERMISSION_CACHE_NAME);
        } catch (Exception e) {
            if (StringUtils.isNotEmpty(targetDir)) {
                deleteJarFile(myPlugin);
            }
            LogUtil.error(e.getMessage(), e);
            DEException.throwException(e);
        } finally {
            DeFileUtils.deleteFile(pluginDir + "temp/");
            DeFileUtils.deleteFile(folder);
        }
        distributeOperate(myPlugin, "install");
        return null;
    }

    public void distributeOperate(MyPlugin plugin, String type) {

        if (ObjectUtils.isNotEmpty(distributedPluginService)) {
            PluginOperate operate = new PluginOperate();
            operate.setPlugin(plugin);
            operate.setSenderIp(IPUtils.domain());
            operate.setType(type);
            if (ObjectUtils.isEmpty(plugin) || StringUtils.isBlank(type) || StringUtils.isBlank(operate.getSenderIp()))
                return;
            distributedPluginService.pushBroadcast(operate);
        }
    }

    public void loadJar(String jarPath, MyPlugin myPlugin) throws Exception {
        loadjarUtil.loadJar(jarPath, myPlugin);
    }

    public void redisBroadcastInstall(MyPlugin plugin) {
        String path = getPath(plugin);
        try {
            if (FileUtil.exist(path)) {
                loadJar(path, plugin);
            } else {
                LogUtil.error("插件路径不存在 {} ", path);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public String getPath(MyPlugin plugin) {
        String store = plugin.getStore();
        String version = plugin.getVersion();
        String moduleName = plugin.getModuleName();
        String fileName = moduleName + "-" + version + ".jar";
        String path = pluginDir + store + "/" + fileName;
        return path;
    }

    private String makeTargetDir(MyPlugin myPlugin) {
        String store = myPlugin.getStore();
        String dir = pluginDir + store + "/";
        File fileDir = new File(dir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return dir;
    }

    /**
     * 检测插件是否已存在
     *
     * @param myPlugin
     * @return
     */
    public boolean pluginExist(MyPlugin myPlugin) {
        KeywordRequest request = new KeywordRequest();
        List<MyPlugin> plugins = extSysPluginMapper.query(request);
        return plugins.stream().anyMatch(plugin -> StringUtils.equals(myPlugin.getName(), plugin.getName()) || StringUtils.equals(myPlugin.getModuleName(), plugin.getModuleName()));
    }

    /**
     * 卸载插件
     *
     * @param pluginId
     * @return
     */
    public Boolean uninstall(Long pluginId) {
        MyPlugin myPlugin = myPluginMapper.selectByPrimaryKey(pluginId);
        if (ObjectUtils.isEmpty(myPlugin)) {
            String msg = "当前插件不存在";
            LogUtil.error(msg);
            DEException.throwException(msg);
        }
        myPlugin = deleteJarFile(myPlugin);
        CacheUtils.removeAll(AuthConstants.USER_CACHE_NAME);
        CacheUtils.removeAll(AuthConstants.USER_ROLE_CACHE_NAME);
        CacheUtils.removeAll(AuthConstants.USER_PERMISSION_CACHE_NAME);

        if (myPlugin.getCategory().equalsIgnoreCase("datasource")) {
            if (CollectionUtils.isNotEmpty(datasourceService.selectByType(myPlugin.getDsType()))) {
                DEException.throwException(Translator.get("i18n_plugin_not_allow_delete"));
            }
            loadjarUtil.deleteModule(myPlugin.getModuleName() + "-" + myPlugin.getVersion());
        }
        myPluginMapper.deleteByPrimaryKey(pluginId);
        distributeOperate(myPlugin, "uninstall");
        return true;
    }

    public Boolean redisBroadcastUnInstall(MyPlugin myPlugin) {
        CacheUtils.removeAll(AuthConstants.USER_ROLE_CACHE_NAME);
        CacheUtils.removeAll(AuthConstants.USER_CACHE_NAME);
        CacheUtils.removeAll(AuthConstants.USER_PERMISSION_CACHE_NAME);

        if (myPlugin.getCategory().equalsIgnoreCase("datasource")) {
            if (CollectionUtils.isNotEmpty(datasourceService.selectByType(myPlugin.getDsType()))) {
                DEException.throwException(Translator.get("i18n_plugin_not_allow_delete"));
            }
            loadjarUtil.deleteModule(myPlugin.getModuleName() + "-" + myPlugin.getVersion());
        }
        myPluginMapper.deleteByPrimaryKey(myPlugin.getPluginId());
        return true;
    }

    private MyPlugin deleteJarFile(MyPlugin plugin) {
        String version = plugin.getVersion();
        String moduleName = plugin.getModuleName();
        String fileName = moduleName + "-" + version + ".jar";
        String path = pluginDir + plugin.getStore() + "/" + fileName;
        File jarFile = new File(path);
        if (!StringUtils.equals("default", plugin.getStore()) && !jarFile.exists()) {
            version = "1.0-SNAPSHOT";
            fileName = moduleName + "-" + version + ".jar";
            path = pluginDir + plugin.getStore() + "/" + fileName;
            jarFile = new File(path);
            plugin.setVersion(version);
        }
        FileUtil.del(jarFile);

        if (plugin.getCategory().equalsIgnoreCase("datasource")) {
            File driverFile = new File(pluginDir + plugin.getStore() + "/" + plugin.getDsType() + "Driver");
            FileUtil.del(driverFile);
        }
        return plugin;
    }

    /**
     * 改变插件状态
     *
     * @param pluginId
     * @param status   true ？ 使用状态 : 禁用状态
     * @return
     */
    public Boolean changeStatus(Long pluginId, Boolean status) {
        CacheUtils.removeAll(AuthConstants.USER_CACHE_NAME);
        CacheUtils.removeAll(AuthConstants.USER_ROLE_CACHE_NAME);
        CacheUtils.removeAll(AuthConstants.USER_PERMISSION_CACHE_NAME);
        return false;
    }

    //判断当前文件是否实插件描述文件
    //文件名称必须plugin.json
    private boolean isPluginJson(File file) {
        return StringUtils.equals(file.getName(), pluginJsonName);
    }

    private boolean isPluginJar(File file) {
        String name = file.getName();
        return StringUtils.equals(DeFileUtils.getExtensionName(name), "jar");
    }

    /**
     * 从plugin.json文件反序列化为MyPlugin实例对象
     *
     * @return
     */
    private MyPluginDTO formatJsonFile(File file) {
        String str = DeFileUtils.readJson(file);
        Gson gson = new Gson();
        Map<String, Object> myPlugin = gson.fromJson(str, Map.class);
        myPlugin.put("free", (Double) myPlugin.get("free") > 0.0);
        myPlugin.put("loadMybatis", myPlugin.get("loadMybatis") == null ? false : (Double) myPlugin.get("loadMybatis") > 0.0);
        MyPluginDTO result = new MyPluginDTO();
        try {
            org.apache.commons.beanutils.BeanUtils.populate(result, myPlugin);
            result.setInstallTime(System.currentTimeMillis());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if (result.getCategory().equalsIgnoreCase("datasource") && (StringUtils.isEmpty(result.getStore()) || !result.getStore().equalsIgnoreCase("default"))) {
            result.setStore("thirdpart");
        }

        return result;
    }

    /**
     * 从插件商城远程安装插件
     * 2.0版本实现
     *
     * @param params
     * @return
     */
    public Map<String, Object> remoteInstall(Map<String, Object> params) {
        return null;
    }

    public boolean versionMatch(String pluginVersion) {
        List<Integer> versionLists = Arrays.stream(version.split("\\.")).map(CodingUtil::string2Integer).collect(Collectors.toList());
        List<Integer> requireVersionLists = Arrays.stream(pluginVersion.split("\\.")).map(CodingUtil::string2Integer).collect(Collectors.toList());
        int maxSize = Math.max(versionLists.size(), requireVersionLists.size());
        for (int i = 0; i < maxSize; i++) {
            Integer currentV = versionLists.size() == i ? 0 : versionLists.get(i);
            Integer requireV = requireVersionLists.size() == i ? 0 : requireVersionLists.get(i);
            if (requireV > currentV) return false;
        }
        return true;
    }

    /**
     * 查询插件列表
     */
    public List<MyPlugin> findList(KeywordRequest request){
        return myPluginMapper.findList(request);
    }

    /**
     * 批量上架
     */
    public int showBatch(Long pluginId,Integer showFlag){
        MyPlugin myPlugin = new MyPlugin();
        myPlugin.setPluginId(pluginId);
        myPlugin.setShowFlag(showFlag);
        myPlugin.setUpdateTime(System.currentTimeMillis());
        return myPluginMapper.updateByPrimaryKeySelective(myPlugin);
    }

    /**
     * 修改插件
     */
    public int updatePlugin(PluginParam pluginParam){
        MyPlugin myPlugin = new MyPlugin();
        myPlugin.setUpdateTime(System.currentTimeMillis());
        myPlugin.setPluginId(pluginParam.getId());
        if(pluginParam.getName()!=null){
            myPlugin.setName( pluginParam.getName() );
        }
        if(pluginParam.getPluginType()!=null){
            myPlugin.setPluginType( pluginParam.getPluginType() );
        }
        return myPluginMapper.updateByPrimaryKeySelective(myPlugin);
    }

//    public String downloadBatch(List<Long> ids){
//        try {
//            if(ids==null || ids.size()<1  ){
//                return "error";
//            }
//
//            List<String> pathList = new ArrayList<>();//获取文件
//
//            List<String> nameList = myPluginMapper.findNameList(ids);
//            for (String filename : nameList) {
//                String filePath = pluginPath + filename;
//                pathList.add(filePath);
//            }
//
//            Random random = new Random();
//            int randCount = random.nextInt(9000)+1000;
//
//            String zipPath = USER_HOME + "/static-resource/zip/" ;
//            if( ZipUtils.checkPath(zipPath)==false ){
//                System.out.println("路径创建有误！");
//                return "error";
//            }
//
//            String zipName = "temp"+ System.currentTimeMillis() + randCount + ".zip";
//            int zipCount = ZipUtils.compress(pathList, zipPath+zipName );
//            System.out.println("成功压缩"+zipCount+"个文件");
//            if(zipCount>0){
//                return "/static-resource/zip/"+zipName;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return "error";
//    }

    public String downloadBatchLocal(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return "error空";
            }
            // 确保目录存在
            String zipPath = USER_HOME + "/static-resource/zip/";
            // 生成唯一文件名
            String zipName = "temp" + System.currentTimeMillis() +
                    new Random().nextInt(9000) + 1000 + ".zip";
            String fullZipPath = zipPath + zipName;
            // 创建空ZIP文件
            new ZipOutputStream(new FileOutputStream(fullZipPath));
            // 直接返回路径（无论是否为空ZIP）
            return "/static-resource/zip/" + zipName;
        } catch (Exception e) {
            e.printStackTrace();
            return "error"+e.getMessage();
        }
    }

    public List<MyPlugin> queryAll() {
        return myPluginMapper.queryAll();
    }
}
