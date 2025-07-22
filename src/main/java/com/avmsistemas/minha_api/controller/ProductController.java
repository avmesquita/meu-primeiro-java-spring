package com.avmsistemas.minha_api.controller;

import com.avmsistemas.minha_api.model.Product;
import com.avmsistemas.minha_api.service.ProductService; // Importe o serviço

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // Pode ser necessário

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produtos", description = "Operações para gerenciamento de produtos")
public class ProductController {

    @Autowired
    private ProductService productService; // Injeta o serviço

    @Operation(summary = "Lista todos os produtos", description = "Retorna uma lista de todos os produtos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    @GetMapping
    public List<Product> getAllProducts(@RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return productService.getProductsByCategoryId(categoryId);
        }
        List<Product> products = productService.getAllProducts(); // Delega para o serviço
        // Force lazy loading of priceHistory if they are to be included in the JSON response
        products.forEach(p -> p.getPriceHistory().size());
        return products;
    }

    @Operation(summary = "Busca um produto por ID", description = "Retorna os detalhes de um produto específico pelo seu ID, incluindo o histórico de preços.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@Parameter(description = "ID do produto a ser buscado", required = true) @PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id); // Delega para o serviço
        return product.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cria um novo produto", description = "Adiciona um novo produto ao sistema e registra seu preço inicial.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados do produto incompletos ou incorretos)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product); // Delega para o serviço
    }

    @Operation(summary = "Atualiza um produto existente", description = "Atualiza os detalhes de um produto existente pelo seu ID e registra a alteração de preço, se houver.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "ID do produto a ser atualizado", required = true) @PathVariable Long id,
            @RequestBody Product productDetails) {
        try {
            Product updatedProduct = productService.updateProduct(id, productDetails); // Delega para o serviço
            return ResponseEntity.ok(updatedProduct);
        } catch (ResponseStatusException e) {
            throw e; // Lança a exceção para que o Spring a trate
        }
    }

    @Operation(summary = "Exclui um produto", description = "Remove um produto do sistema pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID do produto a ser excluído", required = true) @PathVariable Long id) {
        try {
            productService.deleteProduct(id); // Delega para o serviço
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        }
    }
}