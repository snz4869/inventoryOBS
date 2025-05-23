package com.obssolution.controller;

import com.obssolution.dto.item.ItemRequestDTO;
import com.obssolution.dto.item.ItemResponseDTO;
import com.obssolution.dto.item.ItemUpdateRequestDTO;
import com.obssolution.dto.PageResponseDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.service.impl.ItemService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/item")
public class ItemController {

    @Autowired
    ItemService itemService;

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    @GetMapping
    public ResponseEntity<?> getAllItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            log.info("Fetching paginated items (page: {}, size: {})", page, size);
            PageResponseDTO<ItemResponseDTO> response = itemService.getAllItemsPaginated(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving paginated items: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Integer id) {
        try {
            log.info("Fetching item with ID: {}", id);
            ItemResponseDTO item = itemService.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid item ID request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid item ID: " + e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("Item not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching item with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error retrieving item: " + e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> createItem(@RequestBody @Valid ItemRequestDTO requestDTO) {
        try {
            ItemResponseDTO createdItem = itemService.createItem(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (Exception e) {
            log.error("Error creating item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create item: " + e.getMessage());
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> updateItem(@RequestBody @Valid ItemUpdateRequestDTO requestDTO) {
        try {
            ItemResponseDTO updatedItem = itemService.updateItem(requestDTO);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid update request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("Item not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update item: " + e.getMessage());
        }
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Integer id) {
        try {
            itemService.deleteItemById(id);
            return ResponseEntity.ok("Item successfully marked as deleted.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Delete failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("Item not found during delete: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during delete: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error deleting item: " + e.getMessage());
        }
    }

}

