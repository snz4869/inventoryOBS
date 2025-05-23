package com.obssolution.service;

import com.obssolution.dto.item.ItemRequestDTO;
import com.obssolution.dto.item.ItemResponseDTO;
import com.obssolution.dto.PageResponseDTO;

public interface IItemService {

    public PageResponseDTO<ItemResponseDTO> getAllItemsPaginated(int page, int size);

    public ItemResponseDTO getItemById(Integer id);

    public ItemResponseDTO createItem(ItemRequestDTO requestDTO);

    public void deleteItemById(Integer id);
}