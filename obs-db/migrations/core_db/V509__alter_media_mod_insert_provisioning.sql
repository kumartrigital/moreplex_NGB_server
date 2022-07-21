ALTER TABLE b_media_asset
ADD COLUMN `media_sequence` BIGINT(8);

ALTER TABLE b_mod_master
ADD COLUMN `event_start_time` DATETIME after event_start_date,
ADD COLUMN `event_duration` INT(4) after event_end_date,
ADD COLUMN `network_system_code` VARCHAR(120) after event_duration;

ALTER TABLE b_modorder
ADD COLUMN `network_system_code` VARCHAR(120);

ALTER TABLE b_provisioning_request
ADD COLUMN `is_event_order_req` CHAR(1) DEFAULT 'N';

INSERT INTO `obstenant-default`.`job` (`name`, `display_name`, `cron_expression`, `cron_description`, `create_time`, `task_priority`, `next_run_time`, `job_key`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`, `user_id`) VALUES ('EVENT_ORDER_AUTO_EXPIRY', 'Event Order Auto Expiry', '0 0/5 * 1/1 * ? *', 'For every Five minutes', '2022-04-19 06:30:00', '5', '2022-04-19 09:30:00', 'EVENT_ORDER_AUTO_EXPIRY _ DEFAULT', '1', '1', '1', '0', '0', '1');

INSERT INTO `obstenant-default`.`job_parameters` (`job_id`, `param_name`, `param_type`, `param_default_value`, `param_value`, `is_dynamic`) VALUES ('22', 'exipirydatetime', 'DATE', 'NOW()', '03 March 2014', 'Y');

INSERT INTO `obstenant-default`.`job_parameters` (`job_id`, `param_name`, `param_type`, `param_value`, `is_dynamic`) VALUES ('22', 'reportName', 'String', 'EventAutoExpiry', 'Y');

INSERT INTO `obstenant-default`.`stretchy_report` (`report_name`, `report_type`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('EventAutoExpiry', 'Table', 'Scheduling Job', 'select distinct client_id as clientId from b_modorder where event_validtill < now() and event_status=1', 'EventAutoExpiry', '0', '0');

INSERT INTO `obstenant-default`.`job` (`name`, `display_name`, `cron_expression`, `cron_description`, `create_time`, `task_priority`, `next_run_time`, `job_key`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`, `user_id`) VALUES ('EVENTORDER_AUTO_ACTIVATION', 'Event Order Auto Activation', '0 0/5 * 1/1 * ? *', 'For every Five minutes','2022-04-29 17:00:00' , '5', '2022-04-29 17:05:00', 'EVENTORDER_AUTO_ACTIVATION _ DEFAULT', '1', '1', '1', '0', '0', '1');

INSERT INTO `obstenant-default`.`job_parameters` (`job_id`, `param_name`, `param_type`, `param_default_value`, `param_value`, `is_dynamic`) VALUES ('23', 'activationdatetime', 'DATE', 'NOW()', '03 March 2014', 'Y');

INSERT INTO `obstenant-default`.`job_parameters` (`job_id`, `param_name`, `param_type`, `param_value`, `is_dynamic`) VALUES ('23', 'reportName', 'String', 'EventOrderAutoActivation', 'Y');

INSERT INTO `obstenant-default`.`stretchy_report` (`report_name`, `report_type`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('EventOrderAutoActivation', 'Table', 'Scheduling Job', 'select distinct mo.client_id as clientId from b_modorder mo, b_mod_master mm where mo.event_status=9 and mo.event_id = mm.id and mm.event_start_time <= (NOW() + INTERVAL 10 MINUTE)', 'EventOrderAutoActivation', '0', '0');
