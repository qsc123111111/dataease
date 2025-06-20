package io.dataease.dto.datasource;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsConfiguration {
    private String url;
    private String username;
    private String password;
    private String version;
    private String uri;
    private String dataSourceType = "es";
}
