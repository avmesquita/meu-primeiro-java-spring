package com.avmsistemas.minha_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalhes de um produto no sistema")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do produto", example = "1")
    private Long id;

    @Schema(description = "Nome do produto", example = "Smartphone X")
    private String name;

    @Schema(description = "Descrição detalhada do produto", example = "Um smartphone de última geração com câmera de alta resolução.")
    private String description;

    @Schema(description = "Preço atual do produto", example = "799.99")
    private Double price; // Este continua sendo o preço atual

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "Histórico de preços do produto")
    private List<PriceHistory> priceHistory = new ArrayList<>(); // Inicializa a lista

    // Construtor para criação de produto, sem o histórico inicialmente
    public Product(Long id, String name, String description, Double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.priceHistory = new ArrayList<>(); // Garante que a lista não seja nula
    }

    // Método auxiliar para adicionar um registro ao histórico de preços
    public void addPriceHistory(PriceHistory history) {
        priceHistory.add(history);
        history.setProduct(this); // Garante a ligação bidirecional
    }
}