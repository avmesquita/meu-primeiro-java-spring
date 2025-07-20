package com.avmsistemas.minha_api.controller;

import com.avmsistemas.minha_api.model.Product;
import com.avmsistemas.minha_api.model.PriceHistory; // Importar PriceHistory
import com.avmsistemas.minha_api.repository.ProductRepository;
import com.avmsistemas.minha_api.repository.PriceHistoryRepository; // Importar PriceHistoryRepository
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository; // Injetar o repositório de histórico

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        priceHistoryRepository.deleteAll(); // Limpar também o histórico de preços
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        Product p1 = productRepository.save(new Product(null, "Laptop", "Powerful laptop", 1200.00));
        p1.addPriceHistory(new PriceHistory(1200.00, p1));
        productRepository.save(p1);

        Product p2 = productRepository.save(new Product(null, "Mouse", "Gaming mouse", 50.00));
        p2.addPriceHistory(new PriceHistory(50.00, p2));
        productRepository.save(p2);

        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Laptop")))
                .andExpect(jsonPath("$[0].priceHistory", hasSize(1))) // Verifica que há 1 entrada no histórico
                .andExpect(jsonPath("$[1].name", is("Mouse")))
                .andExpect(jsonPath("$[1].priceHistory", hasSize(1)));
    }

    @Test
    void shouldGetProductById() throws Exception {
        Product savedProduct = productRepository.save(new Product(null, "Keyboard", "Mechanical keyboard", 100.00));
        savedProduct.addPriceHistory(new PriceHistory(100.00, savedProduct));
        productRepository.save(savedProduct);

        mockMvc.perform(get("/api/products/{id}", savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedProduct.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Keyboard")))
                .andExpect(jsonPath("$.priceHistory", hasSize(1))) // Verifica o histórico
                .andExpect(jsonPath("$.priceHistory[0].price", is(100.00))); // Verifica o preço no histórico
    }

    @Test
    void shouldCreateProduct() throws Exception {
        Product newProduct = new Product(null, "Monitor", "4K Monitor", 300.00);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Monitor")))
                .andExpect(jsonPath("$.price", is(300.00)))
                .andExpect(jsonPath("$.priceHistory", hasSize(1))) // Espera 1 entrada no histórico
                .andExpect(jsonPath("$.priceHistory[0].price", is(300.00))); // Verifica o preço no histórico

        assertThat(productRepository.findAll()).hasSize(1);
        assertThat(priceHistoryRepository.findAll()).hasSize(1); // Verifica se o histórico foi salvo
    }

    @Test
    void shouldUpdateProductPrice() throws Exception {
        Product existingProduct = productRepository.save(new Product(null, "Old Product", "Old description", 10.00));
        existingProduct.addPriceHistory(new PriceHistory(10.00, existingProduct));
        productRepository.save(existingProduct); // Salva o produto com o histórico inicial

        Product updatedProductDetails = new Product(null, "New Product", "New description", 20.00);

     mockMvc.perform(put("/api/products/{id}", existingProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProductDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingProduct.getId().intValue())))
                .andExpect(jsonPath("$.name", is("New Product")))
                .andExpect(jsonPath("$.description", is("New description")))
                .andExpect(jsonPath("$.price", is(20.00)))
                .andExpect(jsonPath("$.priceHistory", hasSize(2)))
                .andExpect(jsonPath("$.priceHistory[1].price", is(20.00)));

        // Agora, o findById() e as asserções de getPriceHistory() estarão dentro da transação
        Optional<Product> foundProduct = productRepository.findById(existingProduct.getId());
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("New Product");
        // O acesso a getPriceHistory() aqui acontecerá dentro da transação do teste
        assertThat(foundProduct.get().getPriceHistory()).hasSize(2);
        assertThat(foundProduct.get().getPriceHistory().get(1).getPrice()).isEqualTo(20.00);
    }
    
    @Test
    void shouldUpdateProductWithoutPriceChange() throws Exception {
        Product existingProduct = productRepository.save(new Product(null, "Product No Change", "Description", 100.00));
        existingProduct.addPriceHistory(new PriceHistory(100.00, existingProduct));
        productRepository.save(existingProduct);

        Product updatedProductDetails = new Product(null, "Product No Change Updated", "New Description", 100.00); // Mesmo preço

        mockMvc.perform(put("/api/products/{id}", existingProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProductDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Product No Change Updated")))
                .andExpect(jsonPath("$.priceHistory", hasSize(1))); // Histórico não deve mudar
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingProduct() throws Exception {
        Product nonExistingProduct = new Product(null, "Non Existent", "N/A", 1.00);

        mockMvc.perform(put("/api/products/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistingProduct)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        Product productToDelete = productRepository.save(new Product(null, "To Be Deleted", "...", 5.00));
        productToDelete.addPriceHistory(new PriceHistory(5.00, productToDelete));
        productRepository.save(productToDelete); // Garante que há histórico para ser deletado em cascata

        mockMvc.perform(delete("/api/products/{id}", productToDelete.getId()))
                .andExpect(status().isNoContent());

        assertThat(productRepository.findById(productToDelete.getId())).isEmpty();
        assertThat(priceHistoryRepository.findAll()).isEmpty(); // Verifica se o histórico também foi deletado
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingProduct() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}