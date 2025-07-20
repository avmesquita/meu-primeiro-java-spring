package com.avmsistemas.minha_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore; 

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double price;

    private LocalDateTime changeDate; // Data e hora da alteração do preço

    @ManyToOne(fetch = FetchType.LAZY) // Muitos registros de histórico para um produto
    @JoinColumn(name = "product_id", nullable = false) // Coluna de chave estrangeira
    @JsonIgnore
    private Product product; // Referência ao produto ao qual este histórico pertence

    // Construtor para facilitar a criação de novos registros de histórico
    public PriceHistory(Double price, Product product) {
        this.price = price;
        this.changeDate = LocalDateTime.now(); // Define a data/hora atual
        this.product = product;
    }
}