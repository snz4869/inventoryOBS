package com.obssolution.dto.item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ItemResponseDTO {
    private Integer id;
    private String name;
    private BigDecimal price;
    private LocalDateTime createDate;
    private String createBy;
    private Integer remainingStock;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }

    public Integer getRemainingStock() { return remainingStock; }
    public void setRemainingStock(Integer remainingStock) { this.remainingStock = remainingStock; }
}
