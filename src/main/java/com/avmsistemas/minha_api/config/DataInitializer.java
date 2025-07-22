package com.avmsistemas.minha_api.config;

import com.avmsistemas.minha_api.model.Category;
import com.avmsistemas.minha_api.model.Product;
import com.avmsistemas.minha_api.repository.CategoryRepository;
import com.avmsistemas.minha_api.repository.ProductRepository;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(CategoryRepository categoryRepository, ProductRepository productRepository) {
        return args -> {
            // Limpar tabelas (apenas para desenvolvimento)
            productRepository.deleteAll();
            categoryRepository.deleteAll();

            // Criar Categorias
            Category electronics = new Category("Eletrônicos");
            Category books = new Category("Livros");
            Category fashion = new Category("Moda");

            electronics = categoryRepository.save(electronics);
            books = categoryRepository.save(books);
            fashion = categoryRepository.save(fashion);

            // Criar Produtos e associá-los a categorias            
            Product tv = new Product("Smart TV 50", "Uma TV 4K de alta qualidade.", new BigDecimal("2500.00"), "/assets/samples/tv-50.png", electronics);
            Product laptop = new Product("Notebook Gamer", "Laptop poderoso para jogos.", new BigDecimal("5000.00"), "/assets/samples/notebook-gamer.png", electronics);
            Product novel = new Product("O Senhor dos Anéis", "Um clássico da fantasia.", new BigDecimal("45.00"), "/assets/samples/senhor-dos-aneis.jpg", books);
            Product programmingBook = new Product("Clean Code", "Um guia para bons programadores.", new BigDecimal("70.00"), "/assets/samples/clean-code.jpeg", books);
            Product tShirt = new Product("Camiseta Casual", "Camiseta de algodão 100%.", new BigDecimal("30.00"), "/assets/samples/camiseta-casual.png", fashion);

            productRepository.save(tv);
            productRepository.save(laptop);
            productRepository.save(novel);
            productRepository.save(programmingBook);
            productRepository.save(tShirt);

            System.out.println("Dados iniciais carregados: Categorias e Produtos.");
        };
    }
}