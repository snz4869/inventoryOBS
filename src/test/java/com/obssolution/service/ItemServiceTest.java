package com.obssolution.service;

import com.obssolution.dto.PageResponseDTO;
import com.obssolution.dto.item.ItemRequestDTO;
import com.obssolution.dto.item.ItemResponseDTO;
import com.obssolution.dto.item.ItemUpdateRequestDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.model.Item;
import com.obssolution.repository.InventoryRepository;
import com.obssolution.repository.ItemRepository;
import com.obssolution.service.impl.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;
    private ItemRequestDTO itemRequestDTO;
    private ItemUpdateRequestDTO itemUpdateRequestDTO;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1);
        testItem.setName("Test Item");
        testItem.setPrice(BigDecimal.valueOf(10.99));
        testItem.setCreateBy("testUser");
        testItem.setCreateDate(LocalDateTime.now());
        testItem.setIsDeleted(false);

        itemRequestDTO = new ItemRequestDTO();
        itemRequestDTO.setName("New Item");
        itemRequestDTO.setPrice(BigDecimal.valueOf(15.99));

        itemUpdateRequestDTO = new ItemUpdateRequestDTO();
        itemUpdateRequestDTO.setId(1);
        itemUpdateRequestDTO.setName("Updated Item");
        itemUpdateRequestDTO.setPrice(BigDecimal.valueOf(20.99));
    }

    @Test
    void getAllItemsPaginated_ShouldReturnPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemPage = new PageImpl<>(Collections.singletonList(testItem), pageable, 1);

        when(itemRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(itemPage);
        when(inventoryRepository.sumQtyByItemIdAndType(anyInt(), eq("T"))).thenReturn(Optional.of(100));
        when(inventoryRepository.sumQtyByItemIdAndType(anyInt(), eq("W"))).thenReturn(Optional.of(50));

        PageResponseDTO<ItemResponseDTO> result = itemService.getAllItemsPaginated(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getCurrentPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
        assertEquals(10, result.getPageSize());

        ItemResponseDTO dto = result.getContent().get(0);
        assertEquals(1, dto.getId());
        assertEquals("Test Item", dto.getName());
        assertEquals(BigDecimal.valueOf(10.99), dto.getPrice());
        assertEquals(50, dto.getRemainingStock());
    }

    @Test
    void getAllItemsPaginated_WithInvalidPage_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> itemService.getAllItemsPaginated(0, 10));
    }

    @Test
    void getItemById_ShouldReturnItem() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(100));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(30));

        ItemResponseDTO result = itemService.getItemById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals(BigDecimal.valueOf(10.99), result.getPrice());
        assertEquals(70, result.getRemainingStock());
    }

    @Test
    void getItemById_WithInvalidId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> itemService.getItemById(0));
        assertThrows(IllegalArgumentException.class, () -> itemService.getItemById(null));
    }

    @Test
    void getItemById_WithNonExistentId_ShouldThrowException() {
        when(itemRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.getItemById(999));
    }

    @Test
    void createItem_ShouldReturnCreatedItem() {
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        ItemResponseDTO result = itemService.createItem(itemRequestDTO);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Item", result.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_WithNullRequest_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> itemService.createItem(null));
    }

    @Test
    void createItem_WithInvalidData_ShouldThrowException() {
        ItemRequestDTO invalidRequest = new ItemRequestDTO();
        invalidRequest.setName("");
        invalidRequest.setPrice(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> itemService.createItem(invalidRequest));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        ItemResponseDTO result = itemService.updateItem(itemUpdateRequestDTO);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_WithInvalidId_ShouldThrowException() {
        ItemUpdateRequestDTO invalidRequest = new ItemUpdateRequestDTO();
        invalidRequest.setId(0);

        assertThrows(IllegalArgumentException.class, () -> itemService.updateItem(invalidRequest));
    }

    @Test
    void updateItem_WithNonExistentId_ShouldThrowException() {
        itemUpdateRequestDTO.setId(999);
        when(itemRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.updateItem(itemUpdateRequestDTO));
    }

    @Test
    void deleteItemById_ShouldMarkItemAsDeleted() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));

        itemService.deleteItemById(1);

        assertTrue(testItem.getIsDeleted());
        assertNotNull(testItem.getDeleteDate());
        assertEquals("system", testItem.getDeleteBy());
        verify(itemRepository, times(1)).save(testItem);
    }

    @Test
    void deleteItemById_WithInvalidId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> itemService.deleteItemById(0));
    }

    @Test
    void deleteItemById_WithNonExistentId_ShouldThrowException() {
        when(itemRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.deleteItemById(999));
    }

    @Test
    void deleteItemById_WithAlreadyDeletedItem_ShouldThrowException() {
        testItem.setIsDeleted(true);
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));

        assertThrows(IllegalStateException.class, () -> itemService.deleteItemById(1));
    }

    @Test
    void toDTO_ShouldConvertItemToDTO() {
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(200));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(50));

        ItemResponseDTO result = itemService.toDTO(testItem);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals(BigDecimal.valueOf(10.99), result.getPrice());
        assertEquals(150, result.getRemainingStock());
    }

    @Test
    void toDTO_WithNoInventoryRecords_ShouldReturnZeroStock() {
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(0));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(0));

        ItemResponseDTO result = itemService.toDTO(testItem);

        assertEquals(0, result.getRemainingStock());
    }
}
