package com.avmsistemas.minha_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime orderDate; // Data e hora que o pedido foi finalizado

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // Ex: PENDING, SHIPPED, DELIVERED, CANCELED

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // Valor total do pedido

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", unique = true, nullable = true) // 'nullable = true' se um Order pode ser criado sem um Cart original (menos comum)
    private Cart cart;    

    @Enumerated(EnumType.STRING) // Armazena o nome do enum como String no DB
    @Column(nullable = false)
    private PaymentMethod paymentMethod; // Método de pagamento utilizado

    @Column(unique = true, nullable = true, length = 255) // Pode ser nulo se o pagamento ainda não foi processado
    private String transactionId; // ID da transação no gateway de pagamento

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus; 

    // --- Campos de Endereço de Entrega (Copiados do Address do usuário) ---
    @Column(nullable = false)
    private String deliveryStreet;
    @Column(nullable = false)
    private String deliveryNumber;
    private String deliveryComplement;
    @Column(nullable = false)
    private String deliveryNeighborhood;
    @Column(nullable = false)
    private String deliveryCity;
    @Column(nullable = false)
    private String deliveryState;
    @Column(nullable = false)
    private String deliveryZipCode;
    @Column(nullable = false)
    private String deliveryCountry;

    // Construtor para facilitar a criação inicial
    public Order(User user, String deliveryStreet, String deliveryNumber, String deliveryComplement,
                 String deliveryNeighborhood, String deliveryCity, String deliveryState,
                 String deliveryZipCode, String deliveryCountry, BigDecimal totalAmount,
                 PaymentMethod paymentMethod) { // Adicionado paymentMethod
        this.user = user;
        this.orderDate = LocalDateTime.now(); // Definido automaticamente
        this.status = OrderStatus.PENDING; // Status inicial definido automaticamente
        this.deliveryStreet = deliveryStreet;
        this.deliveryNumber = deliveryNumber;
        this.deliveryComplement = deliveryComplement;
        this.deliveryNeighborhood = deliveryNeighborhood;
        this.deliveryCity = deliveryCity;
        this.deliveryState = deliveryState;
        this.deliveryZipCode = deliveryZipCode;
        this.deliveryCountry = deliveryCountry;
        this.totalAmount = totalAmount;        
        this.paymentStatus = PaymentStatus.PENDING; // Status de pagamento inicial definido automaticamente
        this.paymentMethod = paymentMethod; // Agora aceita o método de pagamento
    }

    // Métodos auxiliares para adicionar/remover OrderItems (bidirecional)
    public void addOrderItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        this.items.remove(item);
        item.setOrder(null);
    }
}