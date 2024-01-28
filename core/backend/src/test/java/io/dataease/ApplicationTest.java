package io.dataease;

import com.alibaba.fastjson.JSON;
import io.dataease.controller.ResultHolder;
import io.dataease.service.datasource.DatasourceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@SpringBootTest(classes = ApplicationTest.class)
@RunWith(SpringRunner.class)
@SpringBootApplication(exclude = {
        QuartzAutoConfiguration.class,
        LdapAutoConfiguration.class
})
@ServletComponentScan
@EnableScheduling
@PropertySource(value = {"file:/opt/dataease/conf/dataease.properties"}, encoding = "UTF-8", ignoreResourceNotFound = true)
public class ApplicationTest {
    @Resource
    private DatasourceService datasourceService;
//    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Test
    public void hello(){
        String id="1550d758-5c51-4533-a12a-7c63c02d30fe";
        ResultHolder validate = datasourceService.validate(id);
        System.out.println(JSON.toJSON(validate));
    }
}
