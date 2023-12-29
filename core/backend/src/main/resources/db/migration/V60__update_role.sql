INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (300, 1, 5, 1, '模板管理', 'system-template', 'system/template/index', 1004, 'template-new', 'plugin', b'0', b'0', b'0', 'template:read', NULL, NULL, NULL, 1620281952752);

INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (301, 300, 0, 2, '批量下架模板', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'template:down', NULL, NULL, 1614930862373, 1614930862373);

INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (302, 300, 0, 2, '批量上架模板', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'template:up', NULL, NULL, 1614930862373, 1614930862373);

INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (303, 300, 0, 2, '批量上传模板', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'template:upload', NULL, NULL, 1614930862373, 1614930862373);

INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (304, 300, 0, 2, '批量下载模板', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'template:download', NULL, NULL, 1614930862373, 1614930862373);

INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (305, 300, 0, 2, '删除模板', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'template:delete', NULL, NULL, 1614930862373, 1614930862373);

INSERT INTO `sys_menu` (`menu_id`, `pid`, `sub_count`, `type`, `title`, `name`, `component`, `menu_sort`, `icon`, `path`, `i_frame`, `cache`, `hidden`, `permission`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES (306, 300, 0, 2, '查询模板列表', NULL, NULL, 999, NULL, NULL, b'0', b'0', b'0', 'template:view', NULL, NULL, 1614930862373, 1614930862373);

ALTER TABLE dataset_group add COLUMN `up_down` int DEFAULT '1' COMMENT '0:下线 1上线';