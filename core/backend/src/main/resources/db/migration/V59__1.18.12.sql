ALTER TABLE dataset_table ADD COLUMN `group_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数据集分组id';
ALTER TABLE datasource ADD COLUMN `group_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数据源分组id';

CREATE TABLE `datalabel` (
                             `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                             `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签名称',
                             `desc` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '标签描述',
                             `exp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签表达式',
                             `create_time` bigint DEFAULT NULL,
                             `update_time` bigint DEFAULT NULL,
                             `create_by` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人ID',
                             `involve` int DEFAULT '0' COMMENT '涉及到的模型数量',
                             `is_delete` tinyint(1) DEFAULT '0' COMMENT '逻辑删除0正常 1删除',
                             `field_type` int DEFAULT '1' COMMENT '字段类型：1文本 2数值',
                             `data_type` int DEFAULT '2' COMMENT '数据类型：1维度 2指标',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;