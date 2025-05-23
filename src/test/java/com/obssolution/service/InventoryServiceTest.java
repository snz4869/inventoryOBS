package com.obssolution.service;

import com.obssolution.dto.PageResponseDTO;
import com.obssolution.dto.inventory.InventoryRequestDTO;
import com.obssolution.dto.inventory.InventoryResponseDTO;
import com.obssolution.dto.inventory.InventoryUpdateRequestDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.model.Inventory;
import com.obssolution.model.Item;
import com.obssolution.repository.InventoryRepository;
import com.obssolution.repository.ItemRepository;
import com.obssolution.service.impl.InventoryService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory testInventory;
    private Item testItem;
    private InventoryRequestDTO inventoryRequestDTO;
    private InventoryUpdateRequestDTO inventoryUpdateRequestDTO;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1);
        testItem.setName("Test Item");
        testItem.setPrice(BigDecimal.valueOf(100));

        testInventory = new Inventory();
        testInventory.setId(1);
        testInventory.setItem(testItem);
        testInventory.setQty(10);
        testInventory.setType("T");
        testInventory.setCreateBy("system");
        testInventory.setCreateDate(LocalDateTime.now());
        testInventory.setIsDeleted(false);

        inventoryRequestDTO = new InventoryRequestDTO();
        inventoryRequestDTO.setItemId(1);
        inventoryRequestDTO.setQty(5);
        inventoryRequestDTO.setType("W");

        inventoryUpdateRequestDTO = new InventoryUpdateRequestDTO();
        inventoryUpdateRequestDTO.setId(1);
        inventoryUpdateRequestDTO.setItemId(1);
        inventoryUpdateRequestDTO.setQty(15);
        inventoryUpdateRequestDTO.setType("T");
    }

    @Test
    void getAllInventoriesPaginated_ShouldReturnPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> inventoryPage = new PageImpl<>(Collections.singletonList(testInventory), pageable, 1);

        when(inventoryRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(inventoryPage);

        PageResponseDTO<InventoryResponseDTO> result = inventoryService.getAllInventoriesPaginated(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        InventoryResponseDTO dto = result.getContent().get(0);
        assertEquals(1, dto.getId());
        assertEquals(1, dto.getItemId());
        assertEquals("Test Item", dto.getItemName());
        assertEquals(10, dto.getQty());
        assertEquals("T", dto.getType());
    }

    @Test
    void getAllInventoriesPaginated_WithInvalidPage_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> inventoryService.getAllInventoriesPaginated(0, 10));
    }

    @Test
    void getInventoryById_ShouldReturnInventory() {
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(testInventory));

        InventoryResponseDTO result = inventoryService.getInventoryById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getItemId());
        assertEquals("Test Item", result.getItemName());
        assertEquals(10, result.getQty());
        assertEquals("T", result.getType());
    }

    @Test
    void getInventoryById_WithInvalidId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> inventoryService.getInventoryById(0));
        assertThrows(IllegalArgumentException.class, () -> inventoryService.getInventoryById(null));
    }

    @Test
    void getInventoryById_WithNonExistentId_ShouldThrowException() {
        when(inventoryRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getInventoryById(999));
    }

    @Test
    void createInventory_ShouldReturnCreatedInventory() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(100));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(50));

        InventoryResponseDTO result = inventoryService.createInventory(inventoryRequestDTO);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getItemId());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void createInventory_WithNullRequest_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> inventoryService.createInventory(null));
    }

    @Test
    void createInventory_WithInvalidData_ShouldThrowException() {
        InventoryRequestDTO invalidRequest = new InventoryRequestDTO();
        invalidRequest.setItemId(null);
        invalidRequest.setQty(-1);
        invalidRequest.setType("X");

        assertThrows(IllegalArgumentException.class, () -> inventoryService.createInventory(invalidRequest));
    }

    @Test
    void createInventory_WithInsufficientStock_ShouldThrowException() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(10));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(20));

        inventoryRequestDTO.setType("W");
        inventoryRequestDTO.setQty(5);

        assertThrows(IllegalArgumentException.class, () -> inventoryService.createInventory(inventoryRequestDTO));
    }

    @Test
    void updateInventory_ShouldReturnUpdatedInventory() {
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(testInventory));
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(100));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(50));

        InventoryResponseDTO result = inventoryService.updateInventory(inventoryUpdateRequestDTO);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void updateInventory_WithInvalidId_ShouldThrowException() {
        InventoryUpdateRequestDTO invalidRequest = new InventoryUpdateRequestDTO();
        invalidRequest.setId(0);

        assertThrows(IllegalArgumentException.class, () -> inventoryService.updateInventory(invalidRequest));
    }

    @Test
    void updateInventory_WithNonExistentId_ShouldThrowException() {
        inventoryUpdateRequestDTO.setId(999);
        when(inventoryRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.updateInventory(inventoryUpdateRequestDTO));
    }

    @Test
    void deleteInventoryById_ShouldMarkInventoryAsDeleted() {
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(testInventory));

        inventoryService.deleteInventoryById(1);

        assertTrue(testInventory.getIsDeleted());
        assertNotNull(testInventory.getDeleteDate());
        assertEquals("system", testInventory.getDeleteBy());
        verify(inventoryRepository, times(1)).save(testInventory);
    }

    @Test
    void deleteInventoryById_WithInvalidId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> inventoryService.deleteInventoryById(0));
    }

    @Test
    void deleteInventoryById_WithNonExistentId_ShouldThrowException() {
        when(inventoryRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.deleteInventoryById(999));
    }

    @Test
    void deleteInventoryById_WithAlreadyDeletedInventory_ShouldThrowException() {
        testInventory.setIsDeleted(true);
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(testInventory));

        assertThrows(IllegalStateException.class, () -> inventoryService.deleteInventoryById(1));
    }

    @Test
    void validateStock_ShouldAllowValidWithdrawal() {
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(100));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(50));

        assertDoesNotThrow(() -> inventoryService.validateStock(1, 30, "W", null));
    }

    @Test
    void validateStock_ShouldThrowForInvalidWithdrawal() {
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(100));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(50));

        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.validateStock(1, 60, "W", null));
    }
}