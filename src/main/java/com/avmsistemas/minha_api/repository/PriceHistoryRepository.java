package com.avmsistemas.minha_api.repository;

import com.avmsistemas.minha_api.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    // Você pode adicionar métodos de consulta personalizados aqui, se precisar
    // Ex: List<PriceHistory> findByProductIdOrderByChangeDateDesc(Long productId);
}