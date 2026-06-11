-- FR-O1/O3/O5: 扩展兑换记录为订单主表，新增配送信息表
-- 状态机对齐设计（components.md §4）：PENDING_SHIPMENT → SHIPPED → COMPLETED / CANCELLED（虚拟商品直接 COMPLETED）

ALTER TABLE `exchange_record`
    ADD COLUMN `user_id`        BIGINT      DEFAULT NULL COMMENT '兑换用户ID（网关 X-User-Id）' AFTER `order_no`,
    ADD COLUMN `product_id`     BIGINT      DEFAULT NULL COMMENT '商品ID' AFTER `user_id`,
    ADD COLUMN `quantity`       INT         NOT NULL DEFAULT 1 COMMENT '兑换数量' AFTER `product_desc`,
    ADD COLUMN `product_type`   VARCHAR(16) NOT NULL DEFAULT 'PHYSICAL' COMMENT '商品类型: PHYSICAL-实物 / VIRTUAL-虚拟' AFTER `quantity`,
    ADD COLUMN `reservation_id` VARCHAR(64) DEFAULT NULL COMMENT '库存预占凭据（Saga 补偿/发货扣减用）' AFTER `product_type`,
    MODIFY COLUMN `status` VARCHAR(32) NOT NULL COMMENT '状态: PENDING_SHIPMENT/SHIPPED/COMPLETED/CANCELLED',
    ADD INDEX `idx_user_id` (`user_id`);

-- 旧状态值迁移到设计状态机
UPDATE `exchange_record` SET `status` = 'PENDING_SHIPMENT' WHERE `status` = 'PENDING_DELIVERY';
UPDATE `exchange_record` SET `status` = 'SHIPPED' WHERE `status` = 'DELIVERING';

-- FR-O5: 实物商品配送信息
CREATE TABLE `shipping_info` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `exchange_record_id` BIGINT       NOT NULL COMMENT '关联兑换记录ID',
    `recipient`          VARCHAR(100) NOT NULL COMMENT '收件人',
    `address`            VARCHAR(500) NOT NULL COMMENT '配送地址',
    `phone`              VARCHAR(32)  NOT NULL COMMENT '联系方式',
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by`         BIGINT                DEFAULT NULL COMMENT '创建人',
    `updated_by`         BIGINT                DEFAULT NULL COMMENT '更新人',
    `deleted`            TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `version`            INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_exchange_record_id` (`exchange_record_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '兑换订单配送信息表';
