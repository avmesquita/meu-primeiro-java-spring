package com.avmsistemas.minha_api.repository;

import com.avmsistemas.minha_api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Você pode adicionar métodos personalizados aqui se precisar, por exemplo:
    // Optional<Category> findByName(String name);
}
