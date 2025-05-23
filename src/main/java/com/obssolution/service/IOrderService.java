package com.obssolution.service;

import com.obssolution.dto.order.OrderRequestDTO;
import com.obssolution.dto.order.OrderResponseDTO;
import com.obssolution.dto.order.OrderUpdateRequestDTO;
import com.obssolution.dto.PageResponseDTO;

public interface IOrderService {
    PageResponseDTO<OrderResponseDTO> getAllOrdersPaginated(int page, int size);
    OrderResponseDTO getOrderByOrderNo(String orderNo);
    OrderResponseDTO createOrder(OrderRequestDTO requestDTO);
    OrderResponseDTO updateOrder(OrderUpdateRequestDTO requestDTO);
    void deleteOrderByOrderNo(String orderNo);
}