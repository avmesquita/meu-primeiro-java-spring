package com.avmsistemas.minha_api.service;

import com.avmsistemas.minha_api.model.Product;
import com.avmsistemas.minha_api.model.PriceHistory;
import com.avmsistemas.minha_api.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        // Opcional: Forçar carregamento do histórico para um DTO, se necessário
        product.ifPresent(p -> p.getPriceHistory().size());
        return product;
    }

    @Transactional
    public Product createProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        PriceHistory initialPrice = new PriceHistory(savedProduct.getPrice(), savedProduct);
        savedProduct.addPriceHistory(initialPrice);
        return productRepository.save(savedProduct);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isPresent()) {
            Product existingProduct = productOptional.get();

            if (!existingProduct.getPrice().equals(productDetails.getPrice())) {
                PriceHistory newPriceEntry = new PriceHistory(productDetails.getPrice(), existingProduct);
                existingProduct.addPriceHistory(newPriceEntry);
            }

            existingProduct.setName(productDetails.getName());
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setPrice(productDetails.getPrice());
            existingProduct.setImageUrl(productDetails.getImageUrl());
            existingProduct.setCategory(productDetails.getCategory());

            return productRepository.save(existingProduct);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado.");
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado para exclusão.");
        }
        productRepository.deleteById(id);
    }

    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }    
}