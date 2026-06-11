package com.awsome.shop.order.repository.mysql.po.exchange;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配送信息 持久化对象
 */
@Data
@TableName("shipping_info")
public class ShippingInfoPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long exchangeRecordId;

    private String recipient;

    private String address;

    private String phone;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}
