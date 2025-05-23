package com.obssolution.service;

import com.obssolution.dto.PageResponseDTO;
import com.obssolution.dto.order.OrderRequestDTO;
import com.obssolution.dto.order.OrderResponseDTO;
import com.obssolution.dto.order.OrderUpdateRequestDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.model.Inventory;
import com.obssolution.model.Item;
import com.obssolution.model.Order;
import com.obssolution.repository.CustomerOrderRepository;
import com.obssolution.repository.InventoryRepository;
import com.obssolution.repository.ItemRepository;
import com.obssolution.service.impl.OrderService;
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
class OrderServiceTest {

    @Mock
    private CustomerOrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private Item testItem;
    private OrderRequestDTO orderRequestDTO;
    private OrderUpdateRequestDTO orderUpdateRequestDTO;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1);
        testItem.setName("Test Item");
        testItem.setPrice(BigDecimal.valueOf(100.00));

        testOrder = new Order();
        testOrder.setOrderNo("ORD123");
        testOrder.setItem(testItem);
        testOrder.setQty(5);
        testOrder.setPrice(testItem.getPrice());
        testOrder.setCreateBy("system");
        testOrder.setCreateDate(LocalDateTime.now());
        testOrder.setIsDeleted(false);

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setOrderNo("ORD456");
        orderRequestDTO.setItemId(1);
        orderRequestDTO.setQty(3);

        orderUpdateRequestDTO = new OrderUpdateRequestDTO();
        orderUpdateRequestDTO.setOrderNo("ORD123");
        orderUpdateRequestDTO.setItemId(1);
        orderUpdateRequestDTO.setQty(10);
        orderUpdateRequestDTO.setPrice(BigDecimal.valueOf(120.00));
    }

    @Test
    void getAllOrdersPaginated_ShouldReturnPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(testOrder), pageable, 1);

        when(orderRepository.findAllByIsDeletedFalse(any(Pageable.class))).thenReturn(orderPage);

        PageResponseDTO<OrderResponseDTO> result = orderService.getAllOrdersPaginated(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getCurrentPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
        assertEquals(10, result.getPageSize());

        OrderResponseDTO dto = result.getContent().get(0);
        assertEquals("ORD123", dto.getOrderNo());
        assertEquals(1, dto.getItemId());
        assertEquals("Test Item", dto.getItemName());
        assertEquals(5, dto.getQty());
        assertEquals(BigDecimal.valueOf(100.00), dto.getPrice());
    }

    @Test
    void getAllOrdersPaginated_WithInvalidPage_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.getAllOrdersPaginated(0, 10));
    }

    @Test
    void getOrderByOrderNo_ShouldReturnOrder() {
        when(orderRepository.findByOrderNo("ORD123")).thenReturn(testOrder);

        OrderResponseDTO result = orderService.getOrderByOrderNo("ORD123");

        assertNotNull(result);
        assertEquals("ORD123", result.getOrderNo());
        assertEquals(1, result.getItemId());
        assertEquals("Test Item", result.getItemName());
        assertEquals(5, result.getQty());
    }

    @Test
    void getOrderByOrderNo_WithEmptyOrderNo_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderByOrderNo(""));
        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderByOrderNo(null));
    }

    @Test
    void getOrderByOrderNo_WithNonExistentOrderNo_ShouldThrowException() {
        when(orderRepository.findByOrderNo("NONEXISTENT")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderByOrderNo("NONEXISTENT"));
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(orderRepository.existsById("ORD456")).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(100));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(50));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(new Inventory());

        OrderResponseDTO result = orderService.createOrder(orderRequestDTO);

        assertNotNull(result);
        assertEquals("ORD123", result.getOrderNo());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void createOrder_WithNullRequest_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(null));
    }

    @Test
    void createOrder_WithExistingOrderNo_ShouldThrowException() {
        when(orderRepository.existsById("ORD456")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(orderRequestDTO));
    }

    @Test
    void createOrder_WithInvalidItem_ShouldThrowException() {
        when(orderRepository.existsById("ORD456")).thenReturn(false);
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(orderRequestDTO));
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrowException() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(orderRepository.existsById("ORD456")).thenReturn(false);
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(10));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(20));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(orderRequestDTO));
    }

    @Test
    void updateOrder_ShouldReturnUpdatedOrder() {
        when(orderRepository.findById("ORD123")).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(100));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(50));

        OrderResponseDTO result = orderService.updateOrder(orderUpdateRequestDTO);

        assertNotNull(result);
        assertEquals("ORD123", result.getOrderNo());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrder_WithEmptyOrderNo_ShouldThrowException() {
        OrderUpdateRequestDTO invalidRequest = new OrderUpdateRequestDTO();
        invalidRequest.setOrderNo("");

        assertThrows(IllegalArgumentException.class, () -> orderService.updateOrder(invalidRequest));
    }

    @Test
    void updateOrder_WithNonExistentOrder_ShouldThrowException() {

        orderUpdateRequestDTO.setOrderNo("NONEXISTENT");
        when(orderRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(orderUpdateRequestDTO));
    }

    @Test
    void updateOrder_WithDeletedOrder_ShouldThrowException() {
        testOrder.setIsDeleted(true);
        when(orderRepository.findById("ORD123")).thenReturn(Optional.of(testOrder));

        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(orderUpdateRequestDTO));
    }

    @Test
    void updateOrder_WithInvalidItem_ShouldThrowException() {
        when(orderRepository.findById("ORD123")).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.updateOrder(orderUpdateRequestDTO));
    }

    @Test
    void deleteOrderByOrderNo_ShouldMarkOrderAsDeleted() {
        when(orderRepository.findById("ORD123")).thenReturn(Optional.of(testOrder));

        orderService.deleteOrderByOrderNo("ORD123");

        assertTrue(testOrder.getIsDeleted());
        assertNotNull(testOrder.getDeleteDate());
        assertEquals("system", testOrder.getDeleteBy());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void deleteOrderByOrderNo_WithEmptyOrderNo_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.deleteOrderByOrderNo(""));
    }

    @Test
    void deleteOrderByOrderNo_WithNonExistentOrder_ShouldThrowException() {
        when(orderRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrderByOrderNo("NONEXISTENT"));
    }

    @Test
    void deleteOrderByOrderNo_WithAlreadyDeletedOrder_ShouldThrowException() {
        testOrder.setIsDeleted(true);
        when(orderRepository.findById("ORD123")).thenReturn(Optional.of(testOrder));

        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrderByOrderNo("ORD123"));
    }

    @Test
    void validateStockAvailability_ShouldAllowValidOrder() {
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(100));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(50));

        assertDoesNotThrow(() -> orderService.validateStockAvailability(1, 30));
    }

    @Test
    void validateStockAvailability_ShouldThrowForInsufficientStock() {
        when(inventoryRepository.sumQtyByItemIdAndType(1, "T")).thenReturn(Optional.of(100));
        when(inventoryRepository.sumQtyByItemIdAndType(1, "W")).thenReturn(Optional.of(50));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.validateStockAvailability(1, 60));
    }
}