package com.example.e_commerce.Controller;

import com.example.e_commerce.DTO.Order.OrderRequest;
import com.example.e_commerce.DTO.Order.OrderResponse;
import com.example.e_commerce.Exception.UnauthorizedException;
import com.example.e_commerce.Repository.UserRepository;
import com.example.e_commerce.Service.OrderFacade;
import com.example.e_commerce.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderFacade orderFacade;

    @PostMapping
    public OrderResponse placeOrder(@Valid @RequestBody OrderRequest request,
                                    @RequestHeader(value = "Idempotency-Key") String idemKey,
                                    Authentication auth){

        if (idemKey != null && idemKey.length() > 64) {
            throw new IllegalArgumentException("Idempotency-Key must be at most 64 characters");
        }

        return orderFacade.placeOrder(UserId(auth), idemKey, request);
    }

    @GetMapping
    public Page<OrderResponse> myOrder(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10")int size,
                                       Authentication auth){

        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        Pageable pageable = PageRequest.of(page, Math.min(size, 30),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return orderService.getOrder(UserId(auth), pageable);
    }

    @GetMapping("/{id}")
   public OrderResponse getMyOrder(@PathVariable Long id, Authentication auth){
        return orderService.getMyOrder(id, UserId(auth));
   }

   private Long UserId(Authentication auth){
           return userRepository.findByName(auth.getName())
                   .orElseThrow(() -> new UnauthorizedException("User not found. Please log in again."))
                   .getId();
   }

   @PatchMapping("/{id}/cancel")
   public OrderResponse cancleOrder(@PathVariable Long id, Authentication auth){
        return orderFacade.cancelOrder(id, UserId(auth));
   }


}
