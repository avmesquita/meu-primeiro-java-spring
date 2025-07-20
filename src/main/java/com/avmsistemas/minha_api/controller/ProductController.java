package com.avmsistemas.minha_api.controller;

import com.avmsistemas.minha_api.model.Product;
import com.avmsistemas.minha_api.model.PriceHistory; // Importar PriceHistory
import com.avmsistemas.minha_api.repository.ProductRepository;
import com.avmsistemas.minha_api.repository.PriceHistoryRepository; // Importar PriceHistoryRepository
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

import java.time.LocalDateTime; // Importar LocalDateTime
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produtos", description = "Operações para gerenciamento de produtos")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository; // Injetar o repositório de histórico

    // GET all products - Pode precisar de DTO se o histórico for grande
    @Operation(summary = "Lista todos os produtos", description = "Retorna uma lista de todos os produtos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    @GetMapping
    public List<Product> getAllProducts() {
        // Ao retornar Product, o histórico será lazy-loaded se acessado,
        // mas será serializado para JSON se o Jackson estiver configurado para isso.
        return productRepository.findAll();
    }

    // GET product by ID
    @Operation(summary = "Busca um produto por ID", description = "Retorna os detalhes de um produto específico pelo seu ID, incluindo o histórico de preços.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@Parameter(description = "ID do produto a ser buscado", required = true) @PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        // Se você quiser garantir que o histórico seja carregado (e.g., para evitar LazyInitializationException
        // em um contexto fora da transação, ou para incluí-lo na resposta JSON), pode fazer:
        // product.ifPresent(p -> p.getPriceHistory().size()); // Força o carregamento
        return product.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    // POST create new product
    @Operation(summary = "Cria um novo produto", description = "Adiciona um novo produto ao sistema e registra seu preço inicial.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados do produto incompletos ou incorretos)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody Product product) {
        // Salva o produto primeiro para que ele tenha um ID
        Product savedProduct = productRepository.save(product);

        // Registra o preço inicial no histórico
        PriceHistory initialPrice = new PriceHistory(savedProduct.getPrice(), savedProduct);
        savedProduct.addPriceHistory(initialPrice); // Adiciona ao produto para que o cascade salve

        return productRepository.save(savedProduct); // Salva novamente para persistir o histórico
    }

    // PUT update product
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
        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isPresent()) {
            Product existingProduct = productOptional.get();

            // Verifica se o preço foi alterado
            if (!existingProduct.getPrice().equals(productDetails.getPrice())) {
                // Cria um novo registro de histórico de preço
                PriceHistory newPriceEntry = new PriceHistory(productDetails.getPrice(), existingProduct);
                existingProduct.addPriceHistory(newPriceEntry); // Adiciona ao produto
            }

            // Atualiza os outros campos do produto
            existingProduct.setName(productDetails.getName());
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setPrice(productDetails.getPrice()); // Atualiza o preço atual

            return ResponseEntity.ok(productRepository.save(existingProduct)); // Salva o produto e o novo histórico
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE product - Não precisa de alteração, cascade cuidará do histórico
    @Operation(summary = "Exclui um produto", description = "Remove um produto do sistema pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID do produto a ser excluído", required = true) @PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}