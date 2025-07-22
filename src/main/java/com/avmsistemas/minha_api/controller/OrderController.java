package com.avmsistemas.minha_api.controller;

import com.avmsistemas.minha_api.model.Order;
import com.avmsistemas.minha_api.model.OrderStatus;
import com.avmsistemas.minha_api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users/{userId}/orders") // Pedidos aninhados a um usuário
@Tag(name = "Pedidos", description = "Operações para gerenciamento de pedidos (orders)")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Lista todos os pedidos de um usuário", description = "Retorna uma lista de todos os pedidos realizados por um usuário específico.")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso")
    @GetMapping
    public List<Order> getUserOrders(
            @Parameter(description = "ID do usuário") @PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @Operation(summary = "Busca um pedido por ID", description = "Retorna os detalhes de um pedido específico pelo seu ID e ID do usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado", content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @Parameter(description = "ID do pedido a ser buscado", required = true) @PathVariable Long orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        // Opcional: Adicionar validação se o pedido realmente pertence ao userId
        if (order.isPresent() && !order.get().getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado: Pedido não pertence a este usuário.");
        }
        return order.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualiza o status de um pedido", description = "Atualiza o status de um pedido existente (ex: para SHIPPED, DELIVERED).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do pedido atualizado com sucesso", content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "400", description = "Status inválido")
    })
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @Parameter(description = "ID do usuário") @PathVariable Long userId, // Manter o userId na rota para consistência
            @Parameter(description = "ID do pedido a ser atualizado", required = true) @PathVariable Long orderId,
            @Parameter(description = "Novo status do pedido", required = true) @RequestParam OrderStatus newStatus) {
        // Opcional: Adicionar validação se o pedido realmente pertence ao userId antes de atualizar
        Optional<Order> orderCheck = orderService.getOrderById(orderId);
        if (orderCheck.isPresent() && !orderCheck.get().getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado: Pedido não pertence a este usuário.");
        }
        Order updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }

    @Operation(summary = "Exclui um pedido", description = "Remove um pedido do sistema pelo seu ID. Geralmente, pedidos não são excluídos, mas cancelados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID do usuário") @PathVariable Long userId, // Manter o userId na rota
            @Parameter(description = "ID do pedido a ser excluído", required = true) @PathVariable Long orderId) {
        // Opcional: Adicionar validação se o pedido realmente pertence ao userId
        Optional<Order> orderCheck = orderService.getOrderById(orderId);
        if (orderCheck.isPresent() && !orderCheck.get().getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado: Pedido não pertence a este usuário.");
        }
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}