-- ============================================================
-- 下单幂等与订单号唯一约束（OR-2 / OR-8）
-- request_id：客户端幂等键，同一键重复提交只创建一个订单
-- ============================================================
ALTER TABLE `exchange_record`
    ADD COLUMN `request_id` VARCHAR(64) DEFAULT NULL COMMENT '下单幂等键' AFTER `order_no`;

-- order_no 升级为唯一索引（原为普通索引 idx_order_no）
ALTER TABLE `exchange_record` DROP INDEX `idx_order_no`;
ALTER TABLE `exchange_record` ADD UNIQUE INDEX `uk_order_no` (`order_no`);

-- request_id 唯一约束（NULL 不受约束，允许未携带幂等键的历史/兼容请求）
ALTER TABLE `exchange_record` ADD UNIQUE INDEX `uk_request_id` (`request_id`);
