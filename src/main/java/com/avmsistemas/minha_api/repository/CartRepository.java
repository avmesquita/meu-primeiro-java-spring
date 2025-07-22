package com.avmsistemas.minha_api.repository;

import com.avmsistemas.minha_api.model.Cart;
import com.avmsistemas.minha_api.model.CartStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Exemplo de método para encontrar um carrinho por status
    List<Cart> findByStatus(CartStatus status);

    // Exemplo para encontrar um carrinho PENDING para um usuário (se tivesse userId)
    // Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);
    // Novo método para encontrar um carrinho PENDING para um usuário
    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);    

    // Opcional: Listar carrinhos de um usuário
    Optional<Cart> findByUserId(Long userId);    
}