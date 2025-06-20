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
     *
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

    public static String extractBracketsAndCommasReplace(String text, String fileId, String replaceName) {
        if (text.contains("(")) {
            text = text.replaceAll("IF\\(", "");
            text = text.replaceAll("\\[", "");
            text = text.replaceAll("]", "");
            text = text.replaceAll(fileId, replaceName);
            // 查找逗号的最后一次出现的位置
            int lastCommaIndex = text.lastIndexOf(',');

            // 如果找到逗号，则查找逗号之前的上一个逗号的位置
            if (lastCommaIndex != -1) {
                int secondLastCommaIndex = text.lastIndexOf(',', lastCommaIndex - 1);
                return text.substring(0, secondLastCommaIndex);
            } else {
                return null;
            }
            // 查找逗号的最后一次出现的位置
        } else {
            String pattern = "\\((.*?)(?=,)";

            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(text);
            if (matcher.find()) {
                String extractedContent = matcher.group(1);
                extractedContent = extractedContent.replaceAll("\\[", "");
                extractedContent = extractedContent.replaceAll("]", "");
                extractedContent = extractedContent.replaceAll(fileId, replaceName);
                return extractedContent;
            } else {
                return null;
            }
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
                field = field.replace("ds_", "");
                String[] split = field.split("\\.");
                String rawField = split[1];
                String[] splitAs = rawField.split(" AS ");
                ArrayList<String> list = new ArrayList<>();
                String tableId = split[0].replaceAll("_", "-");
                list.add(tableId);
                list.add(splitAs[0]);
                map.put(splitAs[1], list);
            }
        }
        return map;
    }


    public static void main(String[] args) {
        // String text = "IF([1a9ffd36-cab7-4222-98d2-26eea517ecbb]>=6,'下半年',null)";
        // String term = extractBracketsAndCommasReplace1(text,"1a9ffd36-cab7-4222-98d2-26eea517ecbb","id");
//        String text = "IF([2dce8664-69e2-49ba-b846-7785d30d453c] in ('张三','李四'),'zl',null)";
//        String term = extractBracketsAndCommasReplace(text,"2dce8664-69e2-49ba-b846-7785d30d453c","name");
//        System.out.println("term = " + term);
        String sql = "IF([43391cd2-767a-4181-8369-5923492e074f]>100000";
        getTerm(sql);

    }

    private static void extractOperatorsAndConditions(String condition) {
        // 匹配运算符和后面的条件的正则表达式
        String regex = "(=|>|<)\\s*([^,\\)]+)(?=(,|\\)))";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(condition);

        // 提取匹配的运算符和条件
        while (matcher.find()) {
            String operator = matcher.group(1);
            String conditionValue = matcher.group(2).trim();
            System.out.println("运算符: " + operator);
            System.out.println("条件: " + conditionValue);
        }
    }


    public static ArrayList<FilterItem> getTerm(String sql) {
        ArrayList<FilterItem> filterItems = new ArrayList<>();
        if (sql.contains("]")) {
            sql = sql.split("\\]")[1];
            if (sql.contains("=")) {
                if (sql.contains(">=")) {
                    sql = sql.replace(">=", "").trim();
                    FilterItem filterItem = new FilterItem();
                    filterItem.setTerm(">=");
                    filterItem.setValue(sql);
                    filterItems.add(filterItem);
                } else if (sql.contains("<=")) {
                    sql = sql.replace("<=", "").trim();
                    FilterItem filterItem = new FilterItem();
                    filterItem.setTerm("<=");
                    filterItem.setValue(sql);
                    filterItems.add(filterItem);
                } else if (sql.contains("!=")) {
                    sql = sql.replace("!=", "").trim();
                    FilterItem filterItem = new FilterItem();
                    filterItem.setTerm("!=");
                    filterItem.setValue(sql);
                    filterItems.add(filterItem);
                }
                if (sql.contains("=") && !(sql.contains(">") || sql.contains("<") || sql.contains("!"))) {
                    sql = sql.replace("=", "").trim().replaceAll("'","");
                    FilterItem filterItem = new FilterItem();
                    filterItem.setTerm("=");
                    filterItem.setValue(sql);
                    filterItems.add(filterItem);
                }

            } else if (sql.contains("in")) {
                if (sql.contains("not")) {
                    sql = sql.replace("not in", "").trim().replace("(","").replace(")","");
                    String[] split = sql.split(",");
                    for (String s : split) {
                        s=s.replaceAll("'","");
                        FilterItem filterItem = new FilterItem();
                        filterItem.setTerm("!=");
                        filterItem.setValue(s);
                        filterItems.add(filterItem);
                    }
                } else {
                    sql = sql.replace("in", "").trim().replace("(","").replace(")","");
                    String[] split = sql.split(",");
                    for (String s : split) {
                        s=s.replaceAll("'","");
                        FilterItem filterItem = new FilterItem();
                        filterItem.setTerm("=");
                        filterItem.setValue(s);
                        filterItems.add(filterItem);
                    }
                }
            } else {
                if (sql.contains(">")) {
                    sql = sql.replace(">", "").trim();
                    FilterItem filterItem = new FilterItem();
                    filterItem.setTerm(">");
                    filterItem.setValue(sql);
                    filterItems.add(filterItem);
                } else if (sql.contains("<")) {
                    sql = sql.replace("<", "").trim();
                    FilterItem filterItem = new FilterItem();
                    filterItem.setTerm("<");
                    filterItem.setValue(sql);
                    filterItems.add(filterItem);
                }

            }
            // //获取运算符和值
            // String regex = "(<=|<|=|>)\\s*([^,\\)]+)(?=(,|\\)))";
            // Pattern pattern = Pattern.compile(regex);
            // Matcher matcher = pattern.matcher(sql);
            // // 提取匹配的运算符和值
            // while (matcher.find()) {
            //     String operator = matcher.group(1);
            //     String value = matcher.group(2).trim();
            //     System.out.println("运算符: " + operator);
            //     System.out.println("值: " + value);
            // }
        }
        return filterItems;
        // String regex = "([^\\s]+)\\s*([<>]=?)\\s*([^\\s]+)";
        // Pattern pattern = Pattern.compile(regex);
        // Matcher matcher = pattern.matcher(sql);
        // ArrayList<FilterItem> filterItems = new ArrayList<>();
        // while (matcher.find()) {
        //     String operator = matcher.group(2);
        //     String value = matcher.group(3);
        //     FilterItem filterItem = new FilterItem();
        //     filterItem.setTerm(operator);
        //     filterItem.setValue(value);
        //     filterItems.add(filterItem);
        // }
        // return filterItems;
    }

    public static String getTable(String sql) {
        try {
            String tabel = sql.split("from")[1];
            if (tabel.contains(".")) {
                return tabel.split("\\.")[1].replaceAll("\"", "");
            }
            return tabel;
        } catch (Exception e) {
            return null;
        }
//        String pattern = "from\\s+(\\w+)";
//        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
//        Matcher matcher = regex.matcher(sql);
//        if (matcher.find()) {
//            String group = matcher.group(1);
//            if (group.contains(".")){
//                return group.split("\\.")[1].replaceAll("\"","");
//            }
//            return matcher.group(1);
//        } else{
//            return null;
//        }
    }
}
