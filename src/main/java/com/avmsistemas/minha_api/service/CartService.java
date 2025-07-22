package com.avmsistemas.minha_api.service;

import com.avmsistemas.minha_api.model.Cart;
import com.avmsistemas.minha_api.model.CartItem;
import com.avmsistemas.minha_api.model.Product;
import com.avmsistemas.minha_api.model.User;
import com.avmsistemas.minha_api.repository.CartItemRepository;
import com.avmsistemas.minha_api.repository.CartRepository;
import com.avmsistemas.minha_api.repository.ProductRepository;
import com.avmsistemas.minha_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    // Métodos existentes de CartService (adicionar, remover, etc.)
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public Cart addItemToCart(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantidade deve ser maior que zero.");
        }

        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));

        // Se o produto já existe no carrinho, atualiza a quantidade
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item); // Salva o item atualizado
        } else {
            // Adiciona um novo item ao carrinho
            CartItem newItem = new CartItem(cart, product, quantity);
            cart.addCartItem(newItem); // Garante a relação bidirecional
            cartItemRepository.save(newItem); // Salva o novo item
        }

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateCartItemQuantity(Long userId, Long productId, Integer newQuantity) {
        if (newQuantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantidade não pode ser negativa.");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrinho não encontrado para este usuário."));

        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado no carrinho."));

        if (newQuantity == 0) {
            cart.removeCartItem(itemToUpdate); // Remove da lista do carrinho
            cartItemRepository.delete(itemToUpdate); // Deleta do banco
        } else {
            itemToUpdate.setQuantity(newQuantity);
            itemToUpdate.setPrice(itemToUpdate.getProduct().getPrice()); // Atualiza o preço, caso o produto tenha mudado de preço
            cartItemRepository.save(itemToUpdate); // Salva o item atualizado
        }

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public void removeCartItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrinho não encontrado para este usuário."));

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado no carrinho."));

        cart.removeCartItem(itemToRemove);
        cartItemRepository.delete(itemToRemove);

        cart.calculateTotal();
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrinho não encontrado para este usuário."));

        cartItemRepository.deleteAll(cart.getItems()); // Deleta todos os itens
        cart.getItems().clear(); // Limpa a lista em memória
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public Optional<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }
}