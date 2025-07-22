package com.avmsistemas.minha_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Detalhes de um produto no sistema")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do produto", example = "1")
    private Long id;

    @Schema(description = "Nome do produto", example = "Smartphone X")
    private String name;

    @Schema(description = "Descrição detalhada do produto", example = "Smartphone de última geração com câmera de alta resolução.")
    private String description;

    @Schema(description = "Preço atual do produto", example = "799.99")
    private BigDecimal price; // Este continua sendo o preço atual

    @Schema(description = "URL da Imagem do Produto", example = "http://localhost/assets/image.jpg")
    @Column(length = 2048) // URL pode ser longa, ajuste o tamanho se necessário
    private String imageUrl; // URL para a imagem do produto

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "Histórico de preços do produto")
    private List<PriceHistory> priceHistory = new ArrayList<>(); // Inicializa a lista

    // Relacionamento muitos-para-um com Category (muitos produtos para uma categoria)
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading para otimização
    @JoinColumn(name = "category_id") // Coluna na tabela 'products' que referencia 'categories'
    @JsonBackReference // Importante para evitar recursão infinita no JSON
    private Category category;    

    public Product(String name, String description, BigDecimal price, String imageUrl, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
    }    

    // Método auxiliar para adicionar um registro ao histórico de preços
    public void addPriceHistory(PriceHistory history) {
        priceHistory.add(history);
        history.setProduct(this); // Garante a ligação bidirecional
    }
}