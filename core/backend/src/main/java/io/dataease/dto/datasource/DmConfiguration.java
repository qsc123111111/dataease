package io.dataease.dto.datasource;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;

public class DmConfiguration extends JdbcConfiguration {
    private String driver = "dm.jdbc.driver.DmDriver";
    private String extraParams = "characterEncoding=UTF-8";

    public String getJdbc() {
        return "jdbc:dm://HOSTNAME:PORT/DATABASE".replace("HOSTNAME", getHost().trim()).replace("PORT", getPort().toString().trim()).replace("DATABASE", getSchema().trim());
    }
}
