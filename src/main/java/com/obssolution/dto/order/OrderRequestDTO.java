package com.obssolution.dto.order;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class OrderRequestDTO {

    @NotBlank
    @Size(max = 10)
    private String orderNo;

    @NotNull
    private Integer itemId;

    @NotNull
    @Min(1)
    private Integer qty;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
}
