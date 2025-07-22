package com.avmsistemas.minha_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore; // Para evitar loops de serialização

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa um item dentro de um carrinho de compras")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do item do carrinho", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Muitos itens podem se referir a um produto
    @JoinColumn(name = "product_id", nullable = false)
    @Schema(description = "Produto associado a este item do carrinho")
    private Product product; // Referência ao Produto

    @Schema(description = "Quantidade do produto no carrinho", example = "2")
    private Integer quantity;

    /*
    @Schema(description = "Preço unitário do produto no momento da adição/compra", example = "799.99")
    private Double unitPrice; // Para registrar o preço no momento da adição ao carrinho
    */

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Preço do item no momento da adição ao carrinho

    @ManyToOne(fetch = FetchType.LAZY) // Muitos itens podem pertencer a um carrinho
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore // Importante para evitar loops de serialização JSON
    @Schema(description = "Carrinho ao qual este item pertence")
    private Cart cart; // Referência ao Carrinho

    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice(); // Define o preço do item com base no preço atual do produto
    }

    public void updatePrice() {
        if (this.product != null && this.product.getPrice() != null) {
            this.price = this.product.getPrice();
        }
    }    
}