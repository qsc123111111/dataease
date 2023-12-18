package io.dataease.plugins.common.util;

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
        String text = "IF([e7a56e36-d460-4105-a25b-210102dcd9cd]>=10,'合格',null)";
        String s = extractBracketsAndCommasReplace(text, "e7a56e36-d460-4105-a25b-210102dcd9cd", "id");
        System.out.println("s = " + s);
    }
}
