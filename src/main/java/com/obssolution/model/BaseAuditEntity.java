package com.obssolution.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseAuditEntity {

    @Column(name = "CREATE_BY", nullable = false, updatable = false)
    private String createBy;

    @CreationTimestamp
    @Column(name = "CREATE_DATE", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "UPDATE_BY")
    private String updateBy;

    @UpdateTimestamp
    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    @Column(name = "DELETE_BY")
    private String deleteBy;

    @Column(name = "DELETE_DATE")
    private LocalDateTime deleteDate;

    @Column(name = "IS_DELETED", columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    // Getters and Setters

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public String getDeleteBy() {
        return deleteBy;
    }

    public void setDeleteBy(String deleteBy) {
        this.deleteBy = deleteBy;
    }

    public LocalDateTime getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(LocalDateTime deleteDate) {
        this.deleteDate = deleteDate;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
