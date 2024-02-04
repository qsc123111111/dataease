package io.dataease.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.dataease.controller.ResultHolder;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.mapper.DatasourceMapper;
import io.dataease.service.datasource.DatasourceService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class CommandLineRunnerImpl implements CommandLineRunner {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private DatasourceMapper datasourceMapper;

    @Override
    public void run(String... args) throws Exception {
        String datasourceId = "1550d758-5c51-4533-a12a-7c63c02d30fe";
        Datasource datasource = datasourceMapper.selectByPrimaryKey(datasourceId);
        String configuration = datasource.getConfiguration();
        JSONObject jsonObject = JSONObject.parseObject(configuration);
        // 定义IP和端口的正则表达式
        String regex = "//([\\d.]+):(\\d+)/";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String ipAddress = matcher.group(1);
            String port = matcher.group(2);
            jsonObject.put("host", ipAddress);
            jsonObject.put("port", port);
            jsonObject.put("username", username);
            jsonObject.put("password", password);
            datasource.setConfiguration(jsonObject.toJSONString());
            datasource.setStatus("Success");
            datasourceMapper.updateByPrimaryKeyConfig(datasource);
            //输出host、端口、用户名、密码
            LOGGER.info("====>>初始化demo数据集信息<<====");
            //输出当前时间
            LOGGER.info("当前时间: " + Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime());
            LOGGER.info("host: " + ipAddress + ", port: " + port + ", username: " + username + ", password: " + password);
            LOGGER.info("===========>>结束<<===========");
        }
        //校验demo数据源
        LOGGER.info("===========>>正在校验demo数据源<<===========");
        ResultHolder validate = datasourceService.validate(datasourceId);
        LOGGER.info("校验结果: " + JSON.toJSON(validate));
        LOGGER.info("===========>>校验demo数据源结束<<===========");
    }
}