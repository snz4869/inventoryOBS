package com.obssolution.service.impl;

import com.obssolution.dto.item.ItemRequestDTO;
import com.obssolution.dto.item.ItemResponseDTO;
import com.obssolution.dto.item.ItemUpdateRequestDTO;
import com.obssolution.dto.PageResponseDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.model.Item;
import com.obssolution.repository.InventoryRepository;
import com.obssolution.repository.ItemRepository;
import com.obssolution.service.IItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemService implements IItemService {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    public PageResponseDTO<ItemResponseDTO> getAllItemsPaginated(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be 1 or higher.");
        }

        Pageable pageable = PageRequest.of(page - 1, size); // page-1 karena index 0
        Page<Item> itemPage = itemRepository.findAllByIsDeletedFalse(pageable); // hanya ambil yg belum dihapus

        List<ItemResponseDTO> dtoList = itemPage.getContent().stream()
                .map(this::toDTO)
                .toList();

        return new PageResponseDTO<>(
                dtoList,
                itemPage.getNumber() + 1,
                itemPage.getTotalPages(),
                itemPage.getTotalElements(),
                size
        );
    }



    @Override
    public ItemResponseDTO getItemById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Item ID must be a positive number");
        }

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        return toDTO(item);
    }

    public ItemResponseDTO createItem(ItemRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("ItemRequestDTO must not be null");
        }

        if (requestDTO.getName() == null || requestDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name must not be blank");
        }

        if (requestDTO.getPrice() == null || requestDTO.getPrice().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            throw new IllegalArgumentException("Price must be at least 0.01");
        }

        try {
            Item item = new Item();
            item.setName(requestDTO.getName());
            item.setPrice(requestDTO.getPrice());
            item.setCreateBy("system");

            Item savedItem = itemRepository.save(item);
            return toDTO(savedItem);

        } catch (Exception e) {
            throw new RuntimeException("Failed to save item: " + e.getMessage(), e);
        }
    }


    public ItemResponseDTO updateItem(ItemUpdateRequestDTO requestDTO) {
        if (requestDTO.getId() == null || requestDTO.getId() <= 0) {
            throw new IllegalArgumentException("Item ID must be a positive number");
        }

        Item item = itemRepository.findById(requestDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + requestDTO.getId()));

        item.setName(requestDTO.getName());
        item.setPrice(requestDTO.getPrice());
        item.setUpdateBy("system");

        Item updatedItem = itemRepository.save(item);
        return toDTO(updatedItem);
    }

    public void deleteItemById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Item ID must be a positive number");
        }

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        if (Boolean.TRUE.equals(item.getIsDeleted())) {
            throw new IllegalStateException("Item with id " + id + " is already deleted.");
        }

        item.setIsDeleted(true);
        item.setDeleteBy("system");
        item.setDeleteDate(LocalDateTime.now());

        itemRepository.save(item);
    }


    public ItemResponseDTO toDTO(Item item) {
        ItemResponseDTO dto = new ItemResponseDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        dto.setCreateBy(item.getCreateBy());
        dto.setCreateDate(item.getCreateDate());

        int topUp = inventoryRepository.sumQtyByItemIdAndType(item.getId(), "T").orElse(0);
        int withdrawal = inventoryRepository.sumQtyByItemIdAndType(item.getId(), "W").orElse(0);
        dto.setRemainingStock(topUp - withdrawal);

        return dto;
    }
}
