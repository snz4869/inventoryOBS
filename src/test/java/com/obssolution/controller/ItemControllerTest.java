package com.obssolution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obssolution.dto.PageResponseDTO;
import com.obssolution.dto.item.ItemRequestDTO;
import com.obssolution.dto.item.ItemResponseDTO;
import com.obssolution.dto.item.ItemUpdateRequestDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.service.impl.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();
    }

    private ItemResponseDTO createSampleItemResponseDTO(Integer id, String name, double price) {
        ItemResponseDTO item = new ItemResponseDTO();
        item.setId(id);
        item.setName(name);
        item.setPrice(BigDecimal.valueOf(price));
        item.setCreateBy("SYSTEM");
        item.setCreateDate(LocalDateTime.now());
        item.setRemainingStock(10);
        return item;
    }

    @Test
    void getAllItems_ShouldReturnPaginatedItems() throws Exception {
        // Prepare test data
        ItemResponseDTO item1 = createSampleItemResponseDTO(1, "Pen", 5.00);
        ItemResponseDTO item2 = createSampleItemResponseDTO(2, "Book", 10.00);
        List<ItemResponseDTO> items = Arrays.asList(item1, item2);

        PageResponseDTO<ItemResponseDTO> pageResponse = new PageResponseDTO<>(
                items, 1, 1, items.size(), 5
        );

        given(itemService.getAllItemsPaginated(1, 5)).willReturn(pageResponse);

        // Execute & Verify
        mockMvc.perform(get("/api/item")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Pen"))
                .andExpect(jsonPath("$.content[0].price").value(5.00))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Book"))
                .andExpect(jsonPath("$.content[1].price").value(10.00));
    }

    @Test
    void getItemById_ShouldReturnItem() throws Exception {
        ItemResponseDTO item = createSampleItemResponseDTO(1, "Pen", 5.00);
        given(itemService.getItemById(1)).willReturn(item);

        mockMvc.perform(get("/api/item/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pen"))
                .andExpect(jsonPath("$.price").value(5.00))
                .andExpect(jsonPath("$.createBy").value("SYSTEM"));
    }

    @Test
    void createItem_ShouldReturnCreatedItem() throws Exception {
        ItemRequestDTO request = new ItemRequestDTO();
        request.setName("New Item");
        request.setPrice(BigDecimal.valueOf(15.00));

        ItemResponseDTO response = createSampleItemResponseDTO(8, "New Item", 15.00);
        given(itemService.createItem(any(ItemRequestDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/item/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.name").value("New Item"))
                .andExpect(jsonPath("$.price").value(15.00));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        ItemUpdateRequestDTO request = new ItemUpdateRequestDTO();
        request.setId(1);
        request.setName("Updated Pen");
        request.setPrice(BigDecimal.valueOf(6.00));

        ItemResponseDTO response = createSampleItemResponseDTO(1, "Updated Pen", 6.00);
        given(itemService.updateItem(any(ItemUpdateRequestDTO.class))).willReturn(response);

        mockMvc.perform(put("/api/item/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Pen"))
                .andExpect(jsonPath("$.price").value(6.00));
    }

    @Test
    void deleteItem_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(put("/api/item/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Item successfully marked as deleted."));
    }

    @Test
    void getItemById_ShouldReturnNotFound_WhenItemNotExists() throws Exception {
        given(itemService.getItemById(999)).willThrow(new ResourceNotFoundException("Item not found"));

        mockMvc.perform(get("/api/item/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Item not found"));
    }
}