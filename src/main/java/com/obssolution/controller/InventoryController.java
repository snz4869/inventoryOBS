package com.obssolution.controller;

import com.obssolution.dto.inventory.InventoryRequestDTO;
import com.obssolution.dto.inventory.InventoryResponseDTO;
import com.obssolution.dto.inventory.InventoryUpdateRequestDTO;
import com.obssolution.dto.PageResponseDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.service.impl.InventoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<?> getAllInventories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Fetching paginated inventories (page: {}, size: {})", page, size);
            PageResponseDTO<InventoryResponseDTO> response = inventoryService.getAllInventoriesPaginated(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving paginated inventories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving inventories: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInventoryById(@PathVariable Integer id) {
        try {
            log.info("Fetching inventory with ID: {}", id);
            InventoryResponseDTO inventory = inventoryService.getInventoryById(id);
            return ResponseEntity.ok(inventory);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid inventory ID request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid inventory ID: " + e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("Inventory not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching inventory with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error retrieving inventory: " + e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> createInventory(@RequestBody @Valid InventoryRequestDTO requestDTO) {
        try {
            InventoryResponseDTO createdInventory = inventoryService.createInventory(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInventory);
        } catch (Exception e) {
            log.error("Error creating inventory: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create inventory: " + e.getMessage());
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> updateInventory(@RequestBody @Valid InventoryUpdateRequestDTO requestDTO) {
        try {
            InventoryResponseDTO updatedInventory = inventoryService.updateInventory(requestDTO);
            return ResponseEntity.ok(updatedInventory);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid update request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("Inventory not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating inventory: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update inventory: " + e.getMessage());
        }
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<?> deleteInventory(@PathVariable Integer id) {
        try {
            inventoryService.deleteInventoryById(id);
            return ResponseEntity.ok("Inventory successfully marked as deleted.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Delete failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            log.warn("Inventory not found during delete: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during delete: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error deleting inventory: " + e.getMessage());
        }
    }
}

