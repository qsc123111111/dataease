package io.dataease.dto.dataset;

import com.google.gson.Gson;
import io.dataease.dto.dataset.union.UnionDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Base64;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/23 8:47 下午
 */
@Setter
@Getter
public class DataTableInfoDTO {
    private String table;
    private String sql;
    private boolean isBase64Encryption = false;
    private List<ExcelSheetData> excelSheetDataList;
    private String data;// file path
    private List<DataTableInfoCustomUnion> list;// 自定义数据集
    private List<UnionDTO> union;// 关联数据集

    public static void main(String[] args) {
        DataTableInfoDTO dto = new DataTableInfoDTO();
        dto.setSql(Base64.getEncoder().encodeToString("select * from test".getBytes()));
        dto.setBase64Encryption(true);
        System.out.println(dto.getSql());
        String json = new Gson().toJson(dto);
        System.out.println("json = " + json);
    }
}
