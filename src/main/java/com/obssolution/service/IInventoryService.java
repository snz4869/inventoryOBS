package com.obssolution.service;

import com.obssolution.dto.inventory.InventoryRequestDTO;
import com.obssolution.dto.inventory.InventoryResponseDTO;
import com.obssolution.dto.inventory.InventoryUpdateRequestDTO;
import com.obssolution.dto.PageResponseDTO;

public interface IInventoryService {

    PageResponseDTO<InventoryResponseDTO> getAllInventoriesPaginated(int page, int size);

    InventoryResponseDTO getInventoryById(Integer id);

    InventoryResponseDTO createInventory(InventoryRequestDTO requestDTO);

    InventoryResponseDTO updateInventory(InventoryUpdateRequestDTO requestDTO);

    void deleteInventoryById(Integer id);
}
