package io.dataease.dto.dataset.union;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author gin
 * @Date 2021/12/1 3:54 下午
 */
@Data
public class UnionItemDTO  implements Serializable {
    private DatasetTableField parentField;
    private DatasetTableField currentField;
}
