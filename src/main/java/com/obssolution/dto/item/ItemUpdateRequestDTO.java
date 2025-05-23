package com.obssolution.dto.item;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ItemUpdateRequestDTO {

    @NotNull(message = "ID is required")
    private Integer id;

    @NotBlank(message = "Item name is required")
    @Size(max = 50, message = "Item name must be less than 50 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private BigDecimal price;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
