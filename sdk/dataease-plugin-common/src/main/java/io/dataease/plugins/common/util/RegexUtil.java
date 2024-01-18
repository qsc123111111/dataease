package io.dataease.plugins.common.util;

import cn.hutool.json.JSON;
import io.dataease.plugins.common.entity.FilterItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    /**
     * 获取[]里的内容
     * @param text
     * @return
     */
    public static List<String> extractBracketContents(String text) {
        List<String> contents = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String content = matcher.group(1);
            contents.add(content);
        }
        return contents;
    }

    public static String extractBracketsAndCommas(String text) {
        String patternString = "(?<=\\]).*?(?=\\,)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String result = matcher.group().trim();
            return result;
        }
        return null;
    }

    public static String extractBracketsAndCommasReplace(String text, String fileId,String replaceName) {
        String pattern = "\\((.*?)(?=,)";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        if (matcher.find()) {
            String extractedContent = matcher.group(1);
            extractedContent = extractedContent.replaceAll("\\[","");
            extractedContent = extractedContent.replaceAll("]","");
            extractedContent = extractedContent.replaceAll(fileId,replaceName);
            return extractedContent;
        } else {
            return null;
        }
    }

    public static HashMap<String, ArrayList<String>> getFileds(String text) {
        // 使用正则表达式匹配字段名
        String pattern = "SELECT\\s+(.*?)\\s+FROM";
        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex.matcher(text);
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        if (matcher.find()) {
            String fields = matcher.group(1);
            String[] fieldArray = fields.split(",");
            for (String field : fieldArray) {
                field = field.trim();
                field = field.replace("ds_","");
                String[] split = field.split("\\.");
                String rawField = split[1];
                String[] splitAs = rawField.split(" AS ");
                ArrayList<String> list = new ArrayList<>();
                String tableId = split[0].replaceAll("_","-");
                list.add(tableId);
                list.add(splitAs[0]);
                map.put(splitAs[1], list);
            }
        }
        return map;
    }





    public static void main(String[] args) {
        String text = "IF([83637262-07f4-4e4c-8cc2-08b4f090b7cf]<10 and [83637262-07f4-4e4c-8cc2-08b4f090b7cf]>20";
        ArrayList<FilterItem> term = getTerm(text);
        System.out.println("term = " + term);
    }

    public static ArrayList<FilterItem> getTerm(String sql) {
        String regex = "([^\\s]+)\\s*([<>]=?)\\s*([^\\s]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);
        ArrayList<FilterItem> filterItems = new ArrayList<>();
        while (matcher.find()) {
            String operator = matcher.group(2);
            String value = matcher.group(3);
            FilterItem filterItem = new FilterItem();
            filterItem.setTerm(operator);
            filterItem.setValue(value);
            filterItems.add(filterItem);
        }
        return filterItems;
    }

    public static String getTable(String sql) {
        String pattern = "from\\s+(\\w+)";
        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1);
        } else{
            return null;
        }
    }
}
