package com.avmsistemas.minha_api.service;

import com.avmsistemas.minha_api.model.*; // Importe todos os modelos necessários
import com.avmsistemas.minha_api.repository.OrderRepository;
import com.avmsistemas.minha_api.repository.UserRepository;
import com.avmsistemas.minha_api.repository.AddressRepository;
import com.avmsistemas.minha_api.repository.CartRepository; // Para deletar o carrinho após finalização
import com.avmsistemas.minha_api.repository.CartItemRepository; // Para deletar itens do carrinho

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    @Transactional
    public Order createOrderFromCart(Long userId, Long cartId, Long deliveryAddressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrinho não encontrado para este usuário."));

        if (!cart.getId().equals(cartId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do carrinho não corresponde ao carrinho do usuário.");
        }

        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrinho vazio. Não é possível finalizar um pedido sem itens.");
        }

        Address deliveryAddress = addressRepository.findById(deliveryAddressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço de entrega selecionado não encontrado."));

        // Garante que o endereço de entrega pertence ao usuário
        if (!deliveryAddress.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endereço de entrega não pertence ao usuário especificado.");
        }

        // 1. Criar o Objeto Order e copiar dados
        Order newOrder = new Order(
                user,
                deliveryAddress.getStreet(),
                deliveryAddress.getNumber(),
                deliveryAddress.getComplement(),
                deliveryAddress.getNeighborhood(),
                deliveryAddress.getCity(),
                deliveryAddress.getState(),
                deliveryAddress.getZipCode(),
                deliveryAddress.getCountry(),
                cart.getTotalAmount(), // Total do carrinho                
                PaymentMethod.OTHER
        );

        // 2. Copiar Itens do Carrinho para Itens do Pedido
        for (CartItem cartItem : cart.getItems()) {
            // Cria um novo OrderItem a partir do CartItem e do Product (que já deve estar no CartItem)
            OrderItem orderItem = new OrderItem(newOrder, cartItem.getProduct(), cartItem.getQuantity());
            newOrder.addOrderItem(orderItem); // Adiciona e seta a referência bidirecional
        }

        // Salva o novo pedido (que irá cascatar os OrderItems)
        Order savedOrder = orderRepository.save(newOrder);

        // Opcional: Associe o Order ao Cart para indicar que foi finalizado
        cart.setOrder(savedOrder);
        cartRepository.save(cart); // Persiste a associação no carrinho

        // 3. Limpar o Carrinho após a finalização
        // Delete os itens do carrinho primeiro para evitar problemas de foreign key
        cartItemRepository.deleteAll(cart.getItems()); // Ou cart.getItems().clear() e salvar o cart
        cart.setItems(new ArrayList<>()); // Limpa a lista em memória
        cart.setTotalAmount(BigDecimal.ZERO); // Zera o total
        cartRepository.save(cart); // Salva o carrinho agora vazio

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        orders.forEach(order -> order.getItems().size()); // Força carregamento dos itens
        return orders;
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        order.ifPresent(o -> o.getItems().size()); // Força carregamento dos itens
        return order;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado.");
        }
        orderRepository.deleteById(orderId);
    }
}