package com.obssolution.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "CUSTOMER_ORDER")
public class Order extends BaseAuditEntity {

    @Id
    @NotBlank
    @Size(max = 10)
    @Column(name = "ORDER_NO", nullable = false, unique = true)
    private String orderNo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID", nullable = false)
    private Item item;

    @NotNull
    @Min(1)
    @Column(name = "QTY", nullable = false)
    private Integer qty;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public Order() {
    }

    public Order(String orderNo, Item item, Integer qty, BigDecimal price) {
        this.orderNo = orderNo;
        this.item = item;
        this.qty = qty;
        this.price = price;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
