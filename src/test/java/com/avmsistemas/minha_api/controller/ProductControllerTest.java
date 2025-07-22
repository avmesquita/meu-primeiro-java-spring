package com.avmsistemas.minha_api.controller;

import com.avmsistemas.minha_api.model.Product;
import com.avmsistemas.minha_api.model.Category;
import com.avmsistemas.minha_api.model.PriceHistory; // Importar PriceHistory
import com.avmsistemas.minha_api.repository.ProductRepository;
import com.avmsistemas.minha_api.repository.CategoryRepository;
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
import java.math.BigDecimal;

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
    private CategoryRepository categoryRepository; // Injetar o repositório de histórico


    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        priceHistoryRepository.deleteAll(); // Limpar também o histórico de preços
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        Category electronics = new Category("Eletrônicos"); // Crie ou recupere sua categoria
        electronics = categoryRepository.save(electronics); 

        Product p1 = productRepository.save(new Product("Laptop", "Powerful laptop", new BigDecimal("1200.00"), "http://example.com/laptop.jpg", electronics));
        p1.addPriceHistory(new PriceHistory(new BigDecimal("1200.00"), p1));
        productRepository.save(p1);

        Product p2 = productRepository.save(new Product("Mouse", "Gaming mouse", new BigDecimal("50.00"), "http://example.com/mouse.jpg", electronics));
        p2.addPriceHistory(new PriceHistory(new BigDecimal("50.00"), p2));
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
        Category electronics = new Category("Eletrônicos"); // Crie ou recupere sua categoria
        electronics = categoryRepository.save(electronics); 

        Product savedProduct = productRepository.save(new Product("Keyboard", "Mechanical keyboard", new BigDecimal("100.00"), "http://example.com/keyboard.jpg", electronics));
        savedProduct.addPriceHistory(new PriceHistory(new BigDecimal("100.00"), savedProduct));
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
        Category electronics = new Category("Eletrônicos"); // Crie ou recupere sua categoria
        electronics = categoryRepository.save(electronics); 

        Product newProduct = new Product("Monitor", "4K Monitor", new BigDecimal("300.00"),"", electronics);

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
        Category electronics = new Category("Eletrônicos"); // Crie ou recupere sua categoria
        electronics = categoryRepository.save(electronics); 

        Product existingProduct = productRepository.save(new Product("Old Product", "Old description", new BigDecimal("10.00"), "", electronics));
        existingProduct.addPriceHistory(new PriceHistory(new BigDecimal("10.00"), existingProduct));
        productRepository.save(existingProduct); // Salva o produto com o histórico inicial

        Product updatedProductDetails = new Product("New Product", "New description", new BigDecimal("20.00"), "", electronics);

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
        Category electronics = new Category("Eletrônicos"); // Crie ou recupere sua categoria
        electronics = categoryRepository.save(electronics); 

        Product existingProduct = productRepository.save(new Product("Product No Change", "Description", new BigDecimal("100.00"), "", electronics));
        existingProduct.addPriceHistory(new PriceHistory(new BigDecimal("100.00"), existingProduct));
        productRepository.save(existingProduct);

        Product updatedProductDetails = new Product("Product No Change Updated", "New Description", new BigDecimal("100.00"), "",electronics); // Mesmo preço

        mockMvc.perform(put("/api/products/{id}", existingProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProductDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Product No Change Updated")))
                .andExpect(jsonPath("$.priceHistory", hasSize(1))); // Histórico não deve mudar
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingProduct() throws Exception {
        Category electronics = new Category("Eletrônicos"); // Crie ou recupere sua categoria
        electronics = categoryRepository.save(electronics); 

        Product nonExistingProduct = new Product("Non Existent", "N/A", new BigDecimal("1.00"), "", electronics);

        mockMvc.perform(put("/api/products/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistingProduct)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        Category electronics = new Category("Eletrônicos"); // Crie ou recupere sua categoria
        electronics = categoryRepository.save(electronics); 

        Product productToDelete = productRepository.save(new Product("To Be Deleted", "...", new BigDecimal("5.00"), "", electronics));
        productToDelete.addPriceHistory(new PriceHistory(new BigDecimal("5.00"), productToDelete));
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