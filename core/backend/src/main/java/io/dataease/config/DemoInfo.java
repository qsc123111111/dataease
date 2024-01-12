package io.dataease.config;

import com.alibaba.fastjson.JSONObject;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.mapper.DatasourceMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DemoInfo implements ApplicationRunner {
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    @Resource
    private DatasourceMapper datasourceMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Datasource datasource = datasourceMapper.selectByPrimaryKey("1550d758-5c51-4533-a12a-7c63c02d30fe");
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
            datasourceMapper.updateByPrimaryKeyConfig(datasource);
        }
    }
}
