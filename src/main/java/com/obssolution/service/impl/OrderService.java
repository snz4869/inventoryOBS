package com.obssolution.service.impl;

import com.obssolution.dto.order.OrderRequestDTO;
import com.obssolution.dto.order.OrderResponseDTO;
import com.obssolution.dto.order.OrderUpdateRequestDTO;
import com.obssolution.dto.PageResponseDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.model.Inventory;
import com.obssolution.model.Item;
import com.obssolution.model.Order;
import com.obssolution.repository.CustomerOrderRepository;
import com.obssolution.repository.InventoryRepository;
import com.obssolution.repository.ItemRepository;
import com.obssolution.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService implements IOrderService {

    @Autowired
    private CustomerOrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public PageResponseDTO<OrderResponseDTO> getAllOrdersPaginated(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be 1 or higher.");
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Order> orderPage = orderRepository.findAllByIsDeletedFalse(pageable);

        List<OrderResponseDTO> dtoList = orderPage.getContent().stream()
                .map(this::toDTO)
                .toList();

        return new PageResponseDTO<>(
                dtoList,
                orderPage.getNumber() + 1,
                orderPage.getTotalPages(),
                orderPage.getTotalElements(),
                size
        );
    }

    @Override
    public OrderResponseDTO getOrderByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isEmpty()) {
            throw new IllegalArgumentException("OrderNo must not be empty");
        }

        Order order = orderRepository.findByOrderNo(orderNo);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with orderNo: " + orderNo);
        }

        return toDTO(order);
    }


    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("OrderRequestDTO must not be null");
        }

        if (orderRepository.existsById(requestDTO.getOrderNo())) {
            throw new IllegalArgumentException("Order with orderNo " + requestDTO.getOrderNo() + " already exists");
        }

        Item item = itemRepository.findById(requestDTO.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + requestDTO.getItemId()));

        validateStockAvailability(item.getId(), requestDTO.getQty());

        Order order = new Order();
        order.setOrderNo(requestDTO.getOrderNo());
        order.setItem(item);
        order.setQty(requestDTO.getQty());
        order.setPrice(item.getPrice());
        order.setCreateBy("system");
        Order savedOrder = orderRepository.save(order);

        Inventory inventory = new Inventory();
        inventory.setItem(item);
        inventory.setQty(requestDTO.getQty());
        inventory.setType("W");
        inventory.setCreateBy("system");
        inventoryRepository.save(inventory);
        return toDTO(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrder(OrderUpdateRequestDTO requestDTO) {
        if (requestDTO.getOrderNo() == null || requestDTO.getOrderNo().isEmpty()) {
            throw new IllegalArgumentException("OrderNo must not be empty");
        }

        Order order = orderRepository.findById(requestDTO.getOrderNo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with orderNo: " + requestDTO.getOrderNo()));

        if (Boolean.TRUE.equals(order.getIsDeleted())) {
            throw new ResourceNotFoundException("Order has been deleted");
        }

        Item item = itemRepository.findById(requestDTO.getItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item not found with id: " + requestDTO.getItemId()));

        int qtyDifference = requestDTO.getQty() - order.getQty();
        if (qtyDifference > 0) {
            validateStockAvailability(item.getId(), qtyDifference);
        }

        order.setItem(item);
        order.setQty(requestDTO.getQty());
        order.setPrice(requestDTO.getPrice());
        order.setUpdateBy("system");

        Order updatedOrder = orderRepository.save(order);
        return toDTO(updatedOrder);
    }


    @Override
    @Transactional
    public void deleteOrderByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isEmpty()) {
            throw new IllegalArgumentException("OrderNo must not be empty");
        }

        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with orderNo: " + orderNo));

        if (Boolean.TRUE.equals(order.getIsDeleted())) {
            throw new ResourceNotFoundException("Order has already been deleted");
        }

        order.setIsDeleted(true);
        order.setDeleteBy("system");
        order.setDeleteDate(LocalDateTime.now());

        orderRepository.save(order);
    }


    private OrderResponseDTO toDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderNo(order.getOrderNo());
        dto.setItemId(order.getItem().getId());
        dto.setItemName(order.getItem().getName());
        dto.setQty(order.getQty());
        dto.setPrice(order.getPrice());
        dto.setCreateBy(order.getCreateBy());
        dto.setCreateDate(order.getCreateDate());
        return dto;
    }

    public void validateStockAvailability(Integer itemId, Integer requiredQty) {
        int totalTopUp = inventoryRepository.sumQtyByItemIdAndType(itemId, "T").orElse(0);
        int totalWithdrawal = inventoryRepository.sumQtyByItemIdAndType(itemId, "W").orElse(0);
        int availableStock = totalTopUp - totalWithdrawal;

        if (availableStock < requiredQty) {
            throw new IllegalArgumentException("Insufficient stock for item ID " + itemId +
                    ". Available: " + availableStock + ", Required: " + requiredQty);
        }
    }
}