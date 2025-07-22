package com.avmsistemas.minha_api.model;

public enum PaymentStatus {
    PENDING,       // Pagamento iniciado, aguardando confirmação
    PAID,          // Pagamento confirmado e bem-sucedido
    FAILED,        // Pagamento recusado ou com erro
    REFUNDED,      // Pagamento estornado
    CANCELLED      // Pagamento cancelado antes da conclusão
}
