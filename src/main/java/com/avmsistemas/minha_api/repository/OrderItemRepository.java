package com.avmsistemas.minha_api.repository;

import com.avmsistemas.minha_api.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Métodos de busca específicos, se necessário
}