package io.dataease.plugins.common.constants;

import java.util.List;

public enum SelfDatasourceTypes {
    dm("dameng", "Generic database", "\"", "\"", "", "", "characterEncoding=utf-8", true, DatasourceCalculationMode.DIRECT_AND_SYNC, null,null, true, DatabaseClassification.OLTP);

    private String type;
    private String name;
    private String keywordPrefix;
    private String keywordSuffix;
    private String aliasPrefix;
    private String aliasSuffix;
    private String extraParams;
    private boolean isDatasource;
    private boolean isJdbc;
    private DatasourceCalculationMode calculationMode;
    private DatabaseClassification databaseClassification;
    private List<String> charset;
    private List<String> targetCharset;

    SelfDatasourceTypes(String type, String name, String keywordPrefix, String keywordSuffix, String aliasPrefix, String aliasSuffix, String extraParams, boolean isDatasource, DatasourceCalculationMode calculationMode, List<String> charset, List<String> targetCharset, boolean isJdbc, DatabaseClassification databaseClassification) {
        this.type = type;
        this.name = name;
        this.keywordPrefix = keywordPrefix;
        this.keywordSuffix = keywordSuffix;
        this.aliasPrefix = aliasPrefix;
        this.aliasSuffix = aliasSuffix;
        this.extraParams = extraParams;
        this.isDatasource = isDatasource;
        this.calculationMode = calculationMode;
        this.charset = charset;
        this.targetCharset = targetCharset;
        this.isJdbc = isJdbc;
        this.databaseClassification = databaseClassification;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getKeywordPrefix() {
        return keywordPrefix;
    }

    public String getKeywordSuffix() {
        return keywordSuffix;
    }

    public String getAliasPrefix() {
        return aliasPrefix;
    }

    public String getAliasSuffix() {
        return aliasSuffix;
    }

    public String getExtraParams() {
        return extraParams;
    }

    public List<String> getCharset() {
        return charset;
    }
    public List<String> getTargetCharset() {
        return targetCharset;
    }

    public DatasourceCalculationMode getCalculationMode() {
        return calculationMode;
    }

    public boolean isDatasource() {
        return isDatasource;
    }

    public boolean isJdbc() {
        return isJdbc;
    }

    public DatabaseClassification getDatabaseClassification() {
        return databaseClassification;
    }
}
