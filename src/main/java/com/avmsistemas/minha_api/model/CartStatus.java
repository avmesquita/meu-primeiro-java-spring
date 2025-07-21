package com.avmsistemas.minha_api.model;

public enum CartStatus {
    PENDING,    // Carrinho em andamento (ainda não finalizado)
    COMPLETED,  // Compra finalizada/pedido realizado
    ABANDONED,  // Carrinho descartado ou inativo por um longo tempo
    CANCELLED   // Pedido cancelado após finalização (opcional, para gestão de pedidos)
}