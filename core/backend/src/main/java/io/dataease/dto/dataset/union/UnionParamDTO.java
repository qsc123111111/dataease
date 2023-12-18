package io.dataease.dto.dataset.union;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/12/1 3:53 下午
 */
@Data
public class UnionParamDTO implements Serializable {
    private String unionType;
    private List<UnionItemDTO> unionFields;
}
