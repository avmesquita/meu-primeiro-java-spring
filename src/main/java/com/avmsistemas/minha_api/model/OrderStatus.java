package com.avmsistemas.minha_api.model;

public enum OrderStatus {
    PENDING,        // Pedido criado, aguardando pagamento/processamento
    PROCESSING,     // Em processamento (pagamento confirmado, separação)
    SHIPPED,        // Enviado para transporte
    DELIVERED,      // Entregue ao cliente
    CANCELED,       // Cancelado (pelo cliente ou sistema)
    RETURNED        // Devolvido
}