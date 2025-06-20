package io.dataease.service.templateMarket;

import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import io.dataease.commons.utils.HttpClientConfig;
import io.dataease.commons.utils.HttpClientUtil;
import io.dataease.controller.request.templateMarket.TemplateMarketSearchRequest;
import io.dataease.controller.sys.response.BasicInfo;
import io.dataease.dto.panel.PanelTemplateFileDTO;
import io.dataease.dto.templateMarket.MarketBaseResponse;
import io.dataease.dto.templateMarket.TemplateCategory;
import io.dataease.dto.templateMarket.TemplateMarketDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.service.system.SystemParameterService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.ResourceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 * Date: 2022/7/14
 * Description: ${userName}
 */
@Service
public class TemplateMarketService {
//    @Value("${template.market.url}")
//    private String templateMarketUrl;

    private final static String POSTS_API = "/api/content/posts?page=0&size=2000";
    private final static String CATEGORIES_API = "/api/content/categories";

    @Resource
    private SystemParameterService systemParameterService;

    /**
     * @param templateUrl template url
     * @Description Get template file from template market
     */
    public PanelTemplateFileDTO getTemplateFromMarket(String templateUrl) {
        if (StringUtils.isNotEmpty(templateUrl)) {
            String sufUrl = systemParameterService.templateMarketInfo().getTemplateMarketUlr();
            Gson gson = new Gson();
            String templateInfo = HttpClientUtil.get(sufUrl + templateUrl, null);
            return gson.fromJson(templateInfo, PanelTemplateFileDTO.class);
        } else {
            return null;
        }
    }

    /**
     * @param url content api url
     * @Description Get info from template market content api
     */
    public String marketGet(String url, String accessKey) {
        HttpClientConfig config = new HttpClientConfig();
        config.addHeader("API-Authorization", accessKey);
        return HttpClientUtil.get(url, config);
    }

    public MarketBaseResponse searchTemplate(TemplateMarketSearchRequest request) {
        try {
            //读取resources里的json文件
            ClassPathResource classPathResource = new ClassPathResource("json/model-market.json");
            InputStream inputStream = classPathResource.getStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            String json = new String(bytes, StandardCharsets.UTF_8);
//            String json = FileUtils.readFileToString(ResourceUtils.getFile("classpath:json/model-market.json"), "UTF-8");
//            BasicInfo basicInfo = systemParameterService.templateMarketInfo();
//            String result = marketGet(basicInfo.getTemplateMarketUlr() + POSTS_API, basicInfo.getTemplateAccessKey());
//            List<TemplateMarketDTO> postsResult = JSONObject.parseObject(result).getJSONObject("data").getJSONArray("content").toJavaList(TemplateMarketDTO.class);
//            MarketBaseResponse marketBaseResponse = new MarketBaseResponse(basicInfo.getTemplateMarketUlr(), postsResult);
//            String jsonString = JSON.toJSONString(marketBaseResponse);
//            return marketBaseResponse;
            MarketBaseResponse marketBaseResponse = JSON.parseObject(json, MarketBaseResponse.class);
//            marketBaseResponse.setBaseUrl(templateMarketUrl);
            return marketBaseResponse;
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
        return null;
    }

    public List<String> getCategories() {
//        BasicInfo basicInfo = systemParameterService.templateMarketInfo();
//        String resultStr1 = marketGet(basicInfo.getTemplateMarketUlr() + CATEGORIES_API, basicInfo.getTemplateAccessKey());
        try {
            ClassPathResource classPathResource = new ClassPathResource("json/category.json");
            InputStream inputStream = classPathResource.getStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            String resultStr = new String(bytes, StandardCharsets.UTF_8);
//            File file = ResourceUtils.getFile("classpath:json/category.json");
//            String resultStr = FileUtils.readFileToString(ResourceUtils.getFile("classpath:json/category.json"),"UTF-8");
            List<TemplateCategory> categories = JSONObject.parseObject(resultStr).getJSONArray("data").toJavaList(TemplateCategory.class);
            if (CollectionUtils.isNotEmpty(categories)) {
                return categories.stream().filter(item -> !"应用系列".equals(item.getName())).sorted(Comparator.comparing(TemplateCategory::getPriority)).map(TemplateCategory::getName).collect(Collectors.toList());
            } else {
                return null;
            }
        } catch (IOException e) {
            DataEaseException.throwException(e);
        }
        return null;
    }
}
