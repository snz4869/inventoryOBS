package com.obssolution.repository;

import com.obssolution.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<Order, String> {
    Page<Order> findAllByIsDeletedFalse(Pageable pageable);

    Order findByOrderNo(String orderNo);
}
