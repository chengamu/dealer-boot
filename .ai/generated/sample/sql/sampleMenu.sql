-- Menu SQL (PostgreSQL, tenant-aware, i18n-ready)
insert into sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values (900100, 0, '样本', 'demo.sample.menu', '3', '1', 'sample', 'demo/sample/index', '1', '0', 'C', '1', '1', 'demo:sample:list', '#', 'admin', now(), '', null, '样本 menu')
on conflict (menu_id) do update set menu_name = excluded.menu_name, i18n_key = excluded.i18n_key, parent_id = excluded.parent_id, path = excluded.path, component = excluded.component, perms = excluded.perms, update_by = 'admin', update_time = now();

-- Button SQL
insert into sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values (900101, 0, '样本 Query', 'demo.sample.query', 900100, '1', '#', '', '1', '0', 'F', '0', '0', 'demo:sample:query', '#', 'admin', now(), '', null, '')
on conflict (menu_id) do update set menu_name = excluded.menu_name, i18n_key = excluded.i18n_key, parent_id = excluded.parent_id, perms = excluded.perms, update_by = 'admin', update_time = now();

insert into sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values (900102, 0, '样本 Add', 'demo.sample.add', 900100, '2', '#', '', '1', '0', 'F', '0', '0', 'demo:sample:add', '#', 'admin', now(), '', null, '')
on conflict (menu_id) do update set menu_name = excluded.menu_name, i18n_key = excluded.i18n_key, parent_id = excluded.parent_id, perms = excluded.perms, update_by = 'admin', update_time = now();

insert into sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values (900103, 0, '样本 Edit', 'demo.sample.edit', 900100, '3', '#', '', '1', '0', 'F', '0', '0', 'demo:sample:edit', '#', 'admin', now(), '', null, '')
on conflict (menu_id) do update set menu_name = excluded.menu_name, i18n_key = excluded.i18n_key, parent_id = excluded.parent_id, perms = excluded.perms, update_by = 'admin', update_time = now();

insert into sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values (900104, 0, '样本 Delete', 'demo.sample.remove', 900100, '4', '#', '', '1', '0', 'F', '0', '0', 'demo:sample:remove', '#', 'admin', now(), '', null, '')
on conflict (menu_id) do update set menu_name = excluded.menu_name, i18n_key = excluded.i18n_key, parent_id = excluded.parent_id, perms = excluded.perms, update_by = 'admin', update_time = now();

insert into sys_menu (menu_id, tenant_id, menu_name, i18n_key, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values (900105, 0, '样本 Export', 'demo.sample.export', 900100, '5', '#', '', '1', '0', 'F', '0', '0', 'demo:sample:export', '#', 'admin', now(), '', null, '')
on conflict (menu_id) do update set menu_name = excluded.menu_name, i18n_key = excluded.i18n_key, parent_id = excluded.parent_id, perms = excluded.perms, update_by = 'admin', update_time = now();

-- Menu i18n messages
insert into sys_i18n_message (message_id, message_key, locale, message_value, create_by, create_time, remark) values
(90010001, 'demo.sample.menu', 'zh_CN', '样本', 'admin', now(), 'generated menu'),
(90010002, 'demo.sample.menu', 'en_US', 'Sample', 'admin', now(), 'generated menu'),
(90010101, 'demo.sample.query', 'zh_CN', '样本查询', 'admin', now(), 'generated button'),
(90010102, 'demo.sample.query', 'en_US', 'Sample Query', 'admin', now(), 'generated button'),
(90010201, 'demo.sample.add', 'zh_CN', '样本新增', 'admin', now(), 'generated button'),
(90010202, 'demo.sample.add', 'en_US', 'Sample Add', 'admin', now(), 'generated button'),
(90010301, 'demo.sample.edit', 'zh_CN', '样本修改', 'admin', now(), 'generated button'),
(90010302, 'demo.sample.edit', 'en_US', 'Sample Edit', 'admin', now(), 'generated button'),
(90010401, 'demo.sample.remove', 'zh_CN', '样本删除', 'admin', now(), 'generated button'),
(90010402, 'demo.sample.remove', 'en_US', 'Sample Delete', 'admin', now(), 'generated button'),
(90010501, 'demo.sample.export', 'zh_CN', '样本导出', 'admin', now(), 'generated button'),
(90010502, 'demo.sample.export', 'en_US', 'Sample Export', 'admin', now(), 'generated button')
on conflict (message_key, locale) do update set message_value = excluded.message_value, update_by = 'admin', update_time = now();
-- Generated page i18n messages
insert into sys_i18n_message (message_id, message_key, locale, message_value, create_by, create_time, remark) values
(900100 * 1000 + 1, 'demo.sample.title', 'zh_CN', '样本', 'admin', now(), 'generated page'),
(900100 * 1000 + 2, 'demo.sample.title', 'en_US', 'Sample', 'admin', now(), 'generated page'),
(900100 * 1000 + 3, 'demo.sample.addTitle', 'zh_CN', '添加样本', 'admin', now(), 'generated page'),
(900100 * 1000 + 4, 'demo.sample.addTitle', 'en_US', 'Add Sample', 'admin', now(), 'generated page'),
(900100 * 1000 + 5, 'demo.sample.editTitle', 'zh_CN', '修改样本', 'admin', now(), 'generated page'),
(900100 * 1000 + 6, 'demo.sample.editTitle', 'en_US', 'Edit Sample', 'admin', now(), 'generated page'),
(900100 * 1000 + 7, 'demo.sample.deleteConfirm', 'zh_CN', '是否确认删除样本编号为"{ids}"的数据项？', 'admin', now(), 'generated page'),
(900100 * 1000 + 8, 'demo.sample.deleteConfirm', 'en_US', 'Delete Sample IDs "{ids}"?', 'admin', now(), 'generated page'),
(900100 * 1000 + 9, 'demo.sample.subTitle', 'zh_CN', '样本明细', 'admin', now(), 'generated page'),
(900100 * 1000 + 10, 'demo.sample.subTitle', 'en_US', 'Sample Details', 'admin', now(), 'generated page'),
(900100 * 1000 + 11, 'demo.sample.selectSubToDelete', 'zh_CN', '请先选择要删除的明细数据', 'admin', now(), 'generated page'),
(900100 * 1000 + 12, 'demo.sample.selectSubToDelete', 'en_US', 'Select detail rows to delete', 'admin', now(), 'generated page'),
(900100 * 1000 + 13, 'demo.sample.topNode', 'zh_CN', '顶级节点', 'admin', now(), 'generated page'),
(900100 * 1000 + 14, 'demo.sample.topNode', 'en_US', 'Root node', 'admin', now(), 'generated page')
on conflict (message_key, locale) do update set message_value = excluded.message_value, update_by = 'admin', update_time = now();


insert into sys_i18n_message (message_id, message_key, locale, message_value, create_by, create_time, remark) values
(900100 * 1000 + 100 + 1 * 2 - 1, 'demo.sample.field.id', 'zh_CN', '样本ID', 'admin', now(), 'generated field'),
(900100 * 1000 + 100 + 1 * 2, 'demo.sample.field.id', 'en_US', 'id', 'admin', now(), 'generated field')
on conflict (message_key, locale) do update set message_value = excluded.message_value, update_by = 'admin', update_time = now();


insert into sys_i18n_message (message_id, message_key, locale, message_value, create_by, create_time, remark) values
(900100 * 1000 + 100 + 2 * 2 - 1, 'demo.sample.field.sampleName', 'zh_CN', '样本名称', 'admin', now(), 'generated field'),
(900100 * 1000 + 100 + 2 * 2, 'demo.sample.field.sampleName', 'en_US', 'sampleName', 'admin', now(), 'generated field')
on conflict (message_key, locale) do update set message_value = excluded.message_value, update_by = 'admin', update_time = now();


insert into sys_i18n_message (message_id, message_key, locale, message_value, create_by, create_time, remark) values
(900100 * 1000 + 100 + 3 * 2 - 1, 'demo.sample.field.status', 'zh_CN', '状态', 'admin', now(), 'generated field'),
(900100 * 1000 + 100 + 3 * 2, 'demo.sample.field.status', 'en_US', 'status', 'admin', now(), 'generated field')
on conflict (message_key, locale) do update set message_value = excluded.message_value, update_by = 'admin', update_time = now();


insert into sys_i18n_message (message_id, message_key, locale, message_value, create_by, create_time, remark) values
(900100 * 1000 + 100 + 4 * 2 - 1, 'demo.sample.field.createdAt', 'zh_CN', '创建时间', 'admin', now(), 'generated field'),
(900100 * 1000 + 100 + 4 * 2, 'demo.sample.field.createdAt', 'en_US', 'createdAt', 'admin', now(), 'generated field')
on conflict (message_key, locale) do update set message_value = excluded.message_value, update_by = 'admin', update_time = now();


insert into sys_i18n_message (message_id, message_key, locale, message_value, create_by, create_time, remark) values
(900100 * 1000 + 100 + 5 * 2 - 1, 'demo.sample.field.remark', 'zh_CN', '备注', 'admin', now(), 'generated field'),
(900100 * 1000 + 100 + 5 * 2, 'demo.sample.field.remark', 'en_US', 'remark', 'admin', now(), 'generated field')
on conflict (message_key, locale) do update set message_value = excluded.message_value, update_by = 'admin', update_time = now();
