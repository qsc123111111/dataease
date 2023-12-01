UPDATE `my_plugin`
SET `version` = '1.18.10'
where `plugin_id` > 0
  and `version` = '1.18.9';

INSERT INTO `my_plugin`(`name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `install_type`, `creator`, `load_mybatis`, `release_time`, `install_time`, `module_name`, `icon`, `ds_type`) VALUES ('桑基图插件', 'default', 0, 0, 'view', 'AntV G2Plot 桑基图插件', '1.18.10', NULL, 'DATAEASE', 0, NULL, 1691046891296, 'view-sankey-backend', NULL, NULL);

INSERT INTO `my_plugin`(`name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `install_type`, `creator`, `load_mybatis`, `release_time`, `install_time`, `module_name`, `icon`, `ds_type`) VALUES ('AntV 组合图插件', 'default', 0, 0, 'view', 'AntV G2Plot 组合图插件', '1.18.10', NULL, 'DATAEASE', 0, NULL, 1691046891296, 'view-chartmix-backend', NULL, NULL);

INSERT INTO `my_plugin`(`name`, `store`, `free`, `cost`, `category`, `descript`, `version`, `install_type`, `creator`, `load_mybatis`, `release_time`, `install_time`, `module_name`, `icon`, `ds_type`) VALUES ('ECharts 动态排序图插件', 'default', 0, 0, 'view', 'ECharts 动态排序图插件', '1.18.10', NULL, 'DATAEASE', 0, NULL, 1691046891296, 'view-racebar-backend', NULL, NULL);

CREATE TABLE `datasource_group` (
                                    `id` varchar(50) NOT NULL COMMENT 'ID',
                                    `name` varchar(64) NOT NULL COMMENT '分组名称',
                                    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人ID',
                                    `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
                                    `update_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
                                    `desc` varchar(255) DEFAULT NULL COMMENT '分组描述',
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源文件夹';


ALTER TABLE dataset_table
    ADD COLUMN `desc` varchar(255) DEFAULT NULL COMMENT '描述',
ADD COLUMN `period` int(11) COMMENT '数据集的周期，1主题对象（只进行了关联） 2主题模型（进行了关联并进行了新建字段）';

ALTER TABLE dataset_table
    MODIFY COLUMN `scene_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '场景ID';
