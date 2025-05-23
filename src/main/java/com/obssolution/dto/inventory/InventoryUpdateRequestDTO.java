package com.obssolution.dto.inventory;

import jakarta.validation.constraints.*;

public class InventoryUpdateRequestDTO {

    @NotNull
    private Integer id;

    @NotNull
    private Integer itemId;

    @NotNull
    @Min(0)
    private Integer qty;

    @NotBlank
    @Pattern(regexp = "[TW]")
    private String type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
