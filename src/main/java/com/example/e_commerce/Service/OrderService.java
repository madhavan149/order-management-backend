package com.example.e_commerce.Service;

import com.example.e_commerce.DTO.Order.OrderRequest;
import com.example.e_commerce.DTO.Order.OrderResponse;
import com.example.e_commerce.Exception.ResourceNotFoundException;
import com.example.e_commerce.Model.Order;
import com.example.e_commerce.Model.OrderItem;
import com.example.e_commerce.Model.OrderStatus;
import com.example.e_commerce.Model.Product;
import com.example.e_commerce.Repository.OrderRepository;
import com.example.e_commerce.Repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;


    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }


    @Transactional
    public OrderResponse placeOrder(Long userId, String idemKey, OrderRequest request){

        if (idemKey != null) {
            Optional<Order> existing =
                    orderRepository.findByIdempotencyKeyAndUserId(idemKey, userId);
            if (existing.isPresent()) {
                return OrderResponse.from(existing.get());
            }
        }

        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (OrderRequest.Item line : request.items()) {

            Long productId = line.productId();
            Integer qyt = line.qty();

            if(merged.containsKey(productId)){
                merged.put(productId, merged.get(productId) + qyt);
            }
            else{
                merged.put(productId, qyt);
            }

        }

        Order order = new Order();
        order.setUserId(userId);

        BigDecimal total = BigDecimal.ZERO;

        for(Map.Entry<Long, Integer> e : merged.entrySet()){

            Product product = productRepository.findById(e.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("product not found"));

            product.reserve(e.getValue());

            OrderItem item = new OrderItem();

            item.setProductId(product.getId());
            item.setQty(e.getValue());
            item.setUnitPrice(product.getPrice());
            order.addItem(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(e.getValue())));


        }

        order.setIdempotencyKey(idemKey);
        order.setTotal(total);
        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    @Transactional()
    public Page<OrderResponse> getOrder(Long userId, Pageable pageable) {

        return orderRepository.findByUserId(userId, pageable)
                .map(OrderResponse::from);
    }

    public OrderResponse getMyOrder(Long id, Long userId) {

        return orderRepository.findByIdAndUserId(id, userId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));
    }

    @Transactional
    public OrderResponse changeStatus(Long id, OrderStatus status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));

        order.changeStatus(status);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.changeStatus(OrderStatus.CANCELLED);

        for (OrderItem item: order.getItems()){
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("product not found"));

            product.restock(item.getQty());
        }

        return OrderResponse.from(order);

    }

    @Transactional()
    public OrderResponse getByIdempotencyKey(String idemKey, Long userId) {
        return orderRepository.findByIdempotencyKeyAndUserId(idemKey, userId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));
    }



}
