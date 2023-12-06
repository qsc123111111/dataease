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
                             `expression` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '标签表达式',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

ALTER TABLE dataset_group ADD COLUMN `desc` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分组描述';

ALTER TABLE dataset_table ADD COLUMN `data_raw` varchar(10000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '新建数据集原始属性';

ALTER TABLE dataset_group ADD COLUMN `data_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主题数据名称';
ALTER TABLE dataset_group ADD COLUMN `data_desc` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主题数据描述';

ALTER TABLE dataset_group ADD COLUMN `dir_type` int DEFAULT '0' COMMENT '0:普通文件夹 1:主题模型';

CREATE TABLE `datalabel_ref` (
                                 `id` int NOT NULL  AUTO_INCREMENT COMMENT '主键id',
                                 `datamodel_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主题模型id',
                                 `dataset_field_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字段id',
                                 `datalabel_id` int DEFAULT NULL COMMENT '自定义标签id',
                                 `dataset_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数据集id（模型对象id）',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `datamodel` (
                             `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
                             `dataset_group_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主题模型id',
                             `map_raw` varchar(5000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'map原始字段',
                             `dataobject_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主题对象id',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;