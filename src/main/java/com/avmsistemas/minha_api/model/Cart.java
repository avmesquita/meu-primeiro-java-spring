package com.avmsistemas.minha_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa um carrinho de compras de um usuário")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do carrinho", example = "1")
    private Long id;

    @Enumerated(EnumType.STRING) // Armazena o enum como String no DB
    @Schema(description = "Status atual do carrinho", example = "PENDING")
    private CartStatus status; // PENDING, COMPLETED, ABANDONED

    @Column(columnDefinition = "TEXT") // Para armazenar texto longo
    @Schema(description = "Instruções ou detalhes de pagamento", example = "Cartão de Crédito Visa ****1234")
    private String paymentInstructions;

    @Schema(description = "Data e hora da criação do carrinho")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da última atualização do carrinho")
    private LocalDateTime updatedAt;

    // Relacionamento Many-to-One com User
    // Um carrinho pertence a um usuário
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // user_id pode ser nulo para carrinhos de convidados
    @JsonIgnore // Importante para evitar loops de serialização JSON
    @Schema(description = "Usuário ao qual este carrinho pertence (opcional)")
    private User user; // Referência ao Usuário

    // Relacionamento One-to-Many com CartItem
    // Um carrinho pode ter vários itens (produtos com quantidades)
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "Lista de itens no carrinho")
    private List<CartItem> items = new ArrayList<>();

    // Opcional: Para associar um carrinho a um usuário (futuro)
    // private Long userId;

    @PrePersist // Executado antes de salvar a primeira vez
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = CartStatus.PENDING; // Define status padrão
        }
    }

    @PreUpdate // Executado antes de atualizar
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Método auxiliar para adicionar um item ao carrinho
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this); // Garante a ligação bidirecional
    }

    // Método auxiliar para remover um item do carrinho
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null); // Remove a ligação bidirecional
    }

    // Construtor para facilitar a criação de um carrinho para um usuário
    public Cart(User user) {
        this.user = user;
        this.status = CartStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }    
}