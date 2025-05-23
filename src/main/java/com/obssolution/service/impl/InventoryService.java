package com.obssolution.service.impl;

import com.obssolution.dto.inventory.InventoryRequestDTO;
import com.obssolution.dto.inventory.InventoryResponseDTO;
import com.obssolution.dto.inventory.InventoryUpdateRequestDTO;
import com.obssolution.dto.PageResponseDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.model.Inventory;
import com.obssolution.model.Item;
import com.obssolution.repository.InventoryRepository;
import com.obssolution.repository.ItemRepository;
import com.obssolution.service.IInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService implements IInventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public PageResponseDTO<InventoryResponseDTO> getAllInventoriesPaginated(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be 1 or higher.");
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Inventory> inventoryPage = inventoryRepository.findAllByIsDeletedFalse(pageable);

        List<InventoryResponseDTO> dtoList = inventoryPage.getContent().stream()
                .map(this::toDTO)
                .toList();

        return new PageResponseDTO<>(
                dtoList,
                inventoryPage.getNumber() + 1,
                inventoryPage.getTotalPages(),
                inventoryPage.getTotalElements(),
                size
        );
    }

    @Override
    public InventoryResponseDTO getInventoryById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Inventory ID must be a positive number");
        }

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        return toDTO(inventory);
    }

    @Override
    public InventoryResponseDTO createInventory(InventoryRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("InventoryRequestDTO must not be null");
        }

        Item item = itemRepository.findById(requestDTO.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + requestDTO.getItemId()));

        if (requestDTO.getQty() == null || requestDTO.getQty() < 0) {
            throw new IllegalArgumentException("Quantity must be zero or positive");
        }

        if (requestDTO.getType() == null || (!requestDTO.getType().equals("T") && !requestDTO.getType().equals("W"))) {
            throw new IllegalArgumentException("Type must be 'T' or 'W'");
        }

        validateStock(item.getId(), requestDTO.getQty(), requestDTO.getType(), null);

        Inventory inventory = new Inventory();
        inventory.setItem(item);
        inventory.setQty(requestDTO.getQty());
        inventory.setType(requestDTO.getType());
        inventory.setCreateBy("system");

        Inventory savedInventory = inventoryRepository.save(inventory);
        return toDTO(savedInventory);
    }

    @Override
    public InventoryResponseDTO updateInventory(InventoryUpdateRequestDTO requestDTO) {
        if (requestDTO.getId() == null || requestDTO.getId() <= 0) {
            throw new IllegalArgumentException("Inventory ID must be a positive number");
        }

        Inventory inventory = inventoryRepository.findById(requestDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + requestDTO.getId()));

        Item item = itemRepository.findById(requestDTO.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + requestDTO.getItemId()));

        if (requestDTO.getQty() == null || requestDTO.getQty() < 0) {
            throw new IllegalArgumentException("Quantity must be zero or positive");
        }

        if (requestDTO.getType() == null || (!requestDTO.getType().equals("T") && !requestDTO.getType().equals("W"))) {
            throw new IllegalArgumentException("Type must be 'T' or 'W'");
        }

        validateStock(item.getId(), requestDTO.getQty(), requestDTO.getType(), null); // <= tambahkan ini

        inventory.setItem(item);
        inventory.setQty(requestDTO.getQty());
        inventory.setType(requestDTO.getType());
        inventory.setUpdateBy("system");

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return toDTO(updatedInventory);
    }

    @Override
    public void deleteInventoryById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Inventory ID must be a positive number");
        }

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        if (Boolean.TRUE.equals(inventory.getIsDeleted())) {
            throw new IllegalStateException("Inventory with id " + id + " is already deleted.");
        }

        inventory.setIsDeleted(true);
        inventory.setDeleteBy("system");
        inventory.setDeleteDate(LocalDateTime.now());

        inventoryRepository.save(inventory);
    }


    private InventoryResponseDTO toDTO(Inventory inventory) {
        InventoryResponseDTO dto = new InventoryResponseDTO();
        dto.setId(inventory.getId());
        dto.setItemId(inventory.getItem().getId());
        dto.setItemName(inventory.getItem().getName());
        dto.setQty(inventory.getQty());
        dto.setType(inventory.getType());
        dto.setCreateBy(inventory.getCreateBy());
        dto.setCreateDate(inventory.getCreateDate());
        return dto;
    }

    public void validateStock(Integer itemId, Integer changeQty, String changeType, Integer inventoryIdToExclude) {
        int totalTopUp = inventoryRepository.sumQtyByItemIdAndType(itemId, "T").orElse(0);
        int totalWithdrawal = inventoryRepository.sumQtyByItemIdAndType(itemId, "W").orElse(0);

        if (inventoryIdToExclude != null) {
            Inventory existing = inventoryRepository.findById(inventoryIdToExclude).orElse(null);
            if (existing != null) {
                if ("T".equals(existing.getType())) {
                    totalTopUp -= existing.getQty();
                } else if ("W".equals(existing.getType())) {
                    totalWithdrawal -= existing.getQty();
                }
            }
        }

        if ("T".equals(changeType)) {
            totalTopUp += changeQty;
        } else if ("W".equals(changeType)) {
            totalWithdrawal += changeQty;
        }

        if (totalTopUp < totalWithdrawal) {
            throw new IllegalArgumentException("Insufficient top-up quantity. Withdrawal exceeds available stock.");
        }
    }

}
