package com.obssolution.repository;

import com.obssolution.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    Page<Item> findAllByIsDeletedFalse(Pageable pageable);
}