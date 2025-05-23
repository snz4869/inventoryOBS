package com.obssolution.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "INVENTORY")
public class Inventory extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID", nullable = false)
    private Item item;

    @Min(0)
    @Column(name = "QTY", nullable = false)
    private Integer qty;

    @NotBlank
    @Pattern(regexp = "[TW]")
    @Column(name = "TYPE", nullable = false, length = 1)
    private String type;

    // No-argument constructor
    public Inventory() {
    }

    // All-argument constructor
    public Inventory(Integer id, Item item, Integer qty, String type) {
        this.id = id;
        this.item = item;
        this.qty = qty;
        this.type = type;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
