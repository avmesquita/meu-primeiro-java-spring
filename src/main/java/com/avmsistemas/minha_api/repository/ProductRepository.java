package com.avmsistemas.minha_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.avmsistemas.minha_api.model.Product;

@Repository // Indica que esta interface é um repositório
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository já fornece métodos CRUD prontos para a entidade Product e tipo de ID Long
}