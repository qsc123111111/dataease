package io.dataease.dto.datasource;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class KingbaseConfiguration extends JdbcConfiguration {

    private String driver = "com.kingbase8.Driver";
    private String extraParams = "";
    private List<String> illegalParameters = Arrays.asList("autoDeserialize", "queryInterceptors", "statementInterceptors", "detectCustomCollations", "allowloadlocalinfile", "allowUrlInLocalInfile", "allowLoadLocalInfileInPath");

    public String getJdbc() {
        if (StringUtils.isEmpty(extraParams.trim())) {
            return "jdbc:kingbase8://HOSTNAME:PORT/DATABASE".replace("HOSTNAME", getHost().trim()).replace("PORT", getPort().toString().trim()).replace("DATABASE", getDataBase().trim());
        } else {
            for (String illegalParameter : getIllegalParameters()) {
                if (getExtraParams().toLowerCase().contains(illegalParameter.toLowerCase())) {
                    throw new RuntimeException("Illegal parameter: " + illegalParameter);
                }
            }

            return "jdbc:kingbase8://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS".replace("HOSTNAME", getHost().trim()).replace("PORT", getPort().toString().trim()).replace("DATABASE", getDataBase().trim()).replace("EXTRA_PARAMS", getExtraParams().trim());
        }
    }

    public List<String> getIllegalParameters(){
        List<String> newIllegalParameters = new ArrayList<>();
        newIllegalParameters.addAll(illegalParameters);
        newIllegalParameters.addAll(Arrays.asList("allowloadlocalinfile", "allowUrlInLocalInfile", "allowLoadLocalInfileInPath"));
        return newIllegalParameters;
    }

}