package com.avmsistemas.minha_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Informações do produto (copiadas) - para manter o histórico
    @Column(nullable = false)
    private Long productId; // ID original do produto (para referência)
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasedPrice; // Preço do produto no momento da compra
    private String productDescription;
    private String productImageUrl;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal; // purchasedPrice * quantity

    // Construtor
    public OrderItem(Order order, Product product, Integer quantity) {
        this.order = order;
        this.productId = product.getId();
        this.productName = product.getName();
        this.purchasedPrice = product.getPrice(); // Copia o preço atual
        this.productDescription = product.getDescription();
        this.productImageUrl = product.getImageUrl();
        this.quantity = quantity;
        this.subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}