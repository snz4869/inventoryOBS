package com.obssolution.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ITEM")
public class Item extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "NAME", nullable = false)
    private String name;

    @DecimalMin(value = "0.01", inclusive = true)
    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public Item() {
    }

    public Item(Integer id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
