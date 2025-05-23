package com.obssolution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obssolution.dto.PageResponseDTO;
import com.obssolution.dto.inventory.InventoryRequestDTO;
import com.obssolution.dto.inventory.InventoryResponseDTO;
import com.obssolution.dto.inventory.InventoryUpdateRequestDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.service.impl.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
    }

    private InventoryResponseDTO createSampleInventoryResponseDTO(Integer id, Integer itemId, String itemName, Integer qty, String type) {
        InventoryResponseDTO inventory = new InventoryResponseDTO();
        inventory.setId(id);
        inventory.setItemId(itemId);
        inventory.setItemName(itemName);
        inventory.setQty(qty);
        inventory.setType(type);
        inventory.setCreateBy("SYSTEM");
        inventory.setCreateDate(LocalDateTime.now());
        return inventory;
    }

    @Test
    void getAllInventories_ShouldReturnPaginatedInventories() throws Exception {
        // Prepare test data
        InventoryResponseDTO inventory1 = createSampleInventoryResponseDTO(1, 1, "Pen", 5, "T");
        InventoryResponseDTO inventory2 = createSampleInventoryResponseDTO(2, 2, "Book", 10, "T");
        List<InventoryResponseDTO> inventories = Arrays.asList(inventory1, inventory2);

        PageResponseDTO<InventoryResponseDTO> pageResponse = new PageResponseDTO<>(
                inventories, 1, 1, inventories.size(), 10
        );

        given(inventoryService.getAllInventoriesPaginated(1, 10)).willReturn(pageResponse);

        // Execute & Verify
        mockMvc.perform(get("/api/inventory")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].itemId").value(1))
                .andExpect(jsonPath("$.content[0].itemName").value("Pen"))
                .andExpect(jsonPath("$.content[0].qty").value(5))
                .andExpect(jsonPath("$.content[0].type").value("T"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].itemId").value(2))
                .andExpect(jsonPath("$.content[1].itemName").value("Book"))
                .andExpect(jsonPath("$.content[1].qty").value(10))
                .andExpect(jsonPath("$.content[1].type").value("T"));
    }

    @Test
    void getInventoryById_ShouldReturnInventory() throws Exception {
        InventoryResponseDTO inventory = createSampleInventoryResponseDTO(1, 1, "Pen", 5, "T");
        given(inventoryService.getInventoryById(1)).willReturn(inventory);

        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.itemName").value("Pen"))
                .andExpect(jsonPath("$.qty").value(5))
                .andExpect(jsonPath("$.type").value("T"))
                .andExpect(jsonPath("$.createBy").value("SYSTEM"));
    }

    @Test
    void createInventory_ShouldReturnCreatedInventory() throws Exception {
        InventoryRequestDTO request = new InventoryRequestDTO();
        request.setItemId(1);
        request.setQty(5);
        request.setType("T");

        InventoryResponseDTO response = createSampleInventoryResponseDTO(10, 1, "Pen", 5, "T");
        given(inventoryService.createInventory(any(InventoryRequestDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/inventory/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.itemName").value("Pen"))
                .andExpect(jsonPath("$.qty").value(5))
                .andExpect(jsonPath("$.type").value("T"));
    }

    @Test
    void updateInventory_ShouldReturnUpdatedInventory() throws Exception {
        InventoryUpdateRequestDTO request = new InventoryUpdateRequestDTO();
        request.setId(1);
        request.setItemId(1);
        request.setQty(10);
        request.setType("W");

        InventoryResponseDTO response = createSampleInventoryResponseDTO(1, 1, "Pen", 10, "W");
        given(inventoryService.updateInventory(any(InventoryUpdateRequestDTO.class))).willReturn(response);

        mockMvc.perform(put("/api/inventory/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.itemName").value("Pen"))
                .andExpect(jsonPath("$.qty").value(10))
                .andExpect(jsonPath("$.type").value("W"));
    }

    @Test
    void deleteInventory_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(put("/api/inventory/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Inventory successfully marked as deleted."));
    }

    @Test
    void getInventoryById_ShouldReturnNotFound_WhenInventoryNotExists() throws Exception {
        given(inventoryService.getInventoryById(999)).willThrow(new ResourceNotFoundException("Inventory not found"));

        mockMvc.perform(get("/api/inventory/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Inventory not found"));
    }

    @Test
    void createInventory_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        InventoryRequestDTO request = new InventoryRequestDTO();
        request.setItemId(null); // invalid
        request.setQty(-1); // invalid
        request.setType("X"); // invalid

        mockMvc.perform(post("/api/inventory/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateInventory_ShouldReturnNotFound_WhenInventoryNotExists() throws Exception {
        InventoryUpdateRequestDTO request = new InventoryUpdateRequestDTO();
        request.setId(999);
        request.setItemId(1);
        request.setQty(10);
        request.setType("T");

        given(inventoryService.updateInventory(any(InventoryUpdateRequestDTO.class)))
                .willThrow(new ResourceNotFoundException("Inventory not found"));

        mockMvc.perform(put("/api/inventory/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Inventory not found"));
    }
}