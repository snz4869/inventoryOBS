package com.obssolution.repository;

import com.obssolution.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Page<Inventory> findAllByIsDeletedFalse(Pageable pageable);

    @Query("SELECT SUM(i.qty) FROM Inventory i WHERE i.item.id = :itemId AND i.type = :type AND i.isDeleted = false")
    Optional<Integer> sumQtyByItemIdAndType(@Param("itemId") Integer itemId, @Param("type") String type);

}
