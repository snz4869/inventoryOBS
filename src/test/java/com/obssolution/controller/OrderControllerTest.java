package com.obssolution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obssolution.dto.PageResponseDTO;
import com.obssolution.dto.order.OrderRequestDTO;
import com.obssolution.dto.order.OrderResponseDTO;
import com.obssolution.dto.order.OrderUpdateRequestDTO;
import com.obssolution.exceptions.ResourceNotFoundException;
import com.obssolution.service.impl.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    private OrderResponseDTO createSampleOrderResponseDTO(String orderNo, Integer itemId, String itemName,
                                                          Integer qty, BigDecimal price) {
        OrderResponseDTO order = new OrderResponseDTO();
        order.setOrderNo(orderNo);
        order.setItemId(itemId);
        order.setItemName(itemName);
        order.setQty(qty);
        order.setPrice(price);
        order.setCreateBy("SYSTEM");
        order.setCreateDate(LocalDateTime.now());
        return order;
    }

    @Test
    void getAllOrders_ShouldReturnPaginatedOrders() throws Exception {
        OrderResponseDTO order1 = createSampleOrderResponseDTO("ORD001", 1, "Pen", 5, BigDecimal.valueOf(5.00));
        OrderResponseDTO order2 = createSampleOrderResponseDTO("ORD002", 2, "Book", 10, BigDecimal.valueOf(10.00));
        List<OrderResponseDTO> orders = Arrays.asList(order1, order2);

        PageResponseDTO<OrderResponseDTO> pageResponse = new PageResponseDTO<>(
                orders, 1, 1, orders.size(), 10
        );

        given(orderService.getAllOrdersPaginated(1, 10)).willReturn(pageResponse);

        mockMvc.perform(get("/api/orders")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNo").value("ORD001"))
                .andExpect(jsonPath("$.content[0].itemId").value(1))
                .andExpect(jsonPath("$.content[0].itemName").value("Pen"))
                .andExpect(jsonPath("$.content[0].qty").value(5))
                .andExpect(jsonPath("$.content[0].price").value(5.00))
                .andExpect(jsonPath("$.content[1].orderNo").value("ORD002"))
                .andExpect(jsonPath("$.content[1].itemId").value(2))
                .andExpect(jsonPath("$.content[1].itemName").value("Book"))
                .andExpect(jsonPath("$.content[1].qty").value(10))
                .andExpect(jsonPath("$.content[1].price").value(10.00));
    }

    @Test
    void getOrderByOrderNo_ShouldReturnOrder() throws Exception {
        OrderResponseDTO order = createSampleOrderResponseDTO("ORD001", 1, "Pen", 5, BigDecimal.valueOf(5.00));
        given(orderService.getOrderByOrderNo("ORD001")).willReturn(order);

        mockMvc.perform(get("/api/orders/ORD001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("ORD001"))
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.itemName").value("Pen"))
                .andExpect(jsonPath("$.qty").value(5))
                .andExpect(jsonPath("$.price").value(5.00))
                .andExpect(jsonPath("$.createBy").value("SYSTEM"));
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setOrderNo("ORD003");
        request.setItemId(3);
        request.setQty(15);

        OrderResponseDTO response = createSampleOrderResponseDTO("ORD003", 3, "Notebook", 15, BigDecimal.valueOf(15.00));
        given(orderService.createOrder(any(OrderRequestDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/orders/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNo").value("ORD003"))
                .andExpect(jsonPath("$.itemId").value(3))
                .andExpect(jsonPath("$.itemName").value("Notebook"))
                .andExpect(jsonPath("$.qty").value(15))
                .andExpect(jsonPath("$.price").value(15.00));
    }

    @Test
    void updateOrder_ShouldReturnUpdatedOrder() throws Exception {
        OrderUpdateRequestDTO request = new OrderUpdateRequestDTO();
        request.setOrderNo("ORD001");
        request.setItemId(1);
        request.setQty(10);
        request.setPrice(BigDecimal.valueOf(6.00));

        OrderResponseDTO response = createSampleOrderResponseDTO("ORD001", 1, "Pen", 10, BigDecimal.valueOf(6.00));
        given(orderService.updateOrder(any(OrderUpdateRequestDTO.class))).willReturn(response);

        mockMvc.perform(put("/api/orders/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("ORD001"))
                .andExpect(jsonPath("$.itemId").value(1))
                .andExpect(jsonPath("$.itemName").value("Pen"))
                .andExpect(jsonPath("$.qty").value(10))
                .andExpect(jsonPath("$.price").value(6.00));
    }

    @Test
    void deleteOrder_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(put("/api/orders/delete/ORD001"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order successfully marked as deleted."));
    }

    @Test
    void getOrderByOrderNo_ShouldReturnNotFound_WhenOrderNotExists() throws Exception {
        given(orderService.getOrderByOrderNo("ORD999")).willThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/ORD999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));
    }

    @Test
    void createOrder_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setOrderNo("");
        request.setItemId(null);
        request.setQty(0);

        mockMvc.perform(post("/api/orders/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateOrder_ShouldReturnNotFound_WhenOrderNotExists() throws Exception {
        OrderUpdateRequestDTO request = new OrderUpdateRequestDTO();
        request.setOrderNo("ORD999");
        request.setItemId(1);
        request.setQty(10);
        request.setPrice(BigDecimal.valueOf(5.00));

        given(orderService.updateOrder(any(OrderUpdateRequestDTO.class)))
                .willThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(put("/api/orders/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));
    }

    @Test
    void deleteOrder_ShouldReturnNotFound_WhenOrderNotExists() throws Exception {
        doThrow(new ResourceNotFoundException("Order not found"))
                .when(orderService).deleteOrderByOrderNo("ORD999");

        mockMvc.perform(put("/api/orders/delete/ORD999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));
    }
}