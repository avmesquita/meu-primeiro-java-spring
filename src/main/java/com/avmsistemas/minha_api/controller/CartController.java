package com.avmsistemas.minha_api.controller;

import com.avmsistemas.minha_api.model.Cart;
import com.avmsistemas.minha_api.model.Order; // Importe
import com.avmsistemas.minha_api.service.CartService;
import com.avmsistemas.minha_api.service.OrderService; // Importe o OrderService
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

import java.util.Optional;

@RestController
@RequestMapping("/api/users/{userId}/cart")
@Tag(name = "Carrinhos", description = "Operações para gerenciamento do carrinho de compras do usuário")
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService; // Injeta o OrderService

    @Operation(summary = "Adiciona um item ao carrinho", description = "Adiciona ou atualiza a quantidade de um produto no carrinho de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item adicionado/atualizado com sucesso", content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "404", description = "Usuário ou produto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida")
    })
    @PostMapping("/items")
    public ResponseEntity<Cart> addItemToCart(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @Parameter(description = "ID do produto") @RequestParam Long productId,
            @Parameter(description = "Quantidade a ser adicionada/atualizada") @RequestParam Integer quantity) {
        Cart updatedCart = cartService.addItemToCart(userId, productId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    @Operation(summary = "Atualiza a quantidade de um item no carrinho", description = "Atualiza a quantidade de um produto específico no carrinho de um usuário. Se a quantidade for 0, o item é removido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantidade do item atualizada com sucesso", content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "404", description = "Usuário, carrinho ou item não encontrado"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida")
    })
    @PutMapping("/items/{productId}")
    public ResponseEntity<Cart> updateCartItemQuantity(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @Parameter(description = "ID do produto no carrinho") @PathVariable Long productId,
            @Parameter(description = "Nova quantidade do produto") @RequestParam Integer newQuantity) {
        Cart updatedCart = cartService.updateCartItemQuantity(userId, productId, newQuantity);
        return ResponseEntity.ok(updatedCart);
    }

    @Operation(summary = "Remove um item do carrinho", description = "Remove um produto específico do carrinho de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário, carrinho ou item não encontrado")
    })
    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeCartItem(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @Parameter(description = "ID do produto a ser removido") @PathVariable Long productId) {
        cartService.removeCartItem(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Limpa o carrinho", description = "Remove todos os itens do carrinho de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Carrinho limpo com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário ou carrinho não encontrado")
    })
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> clearCart(
            @Parameter(description = "ID do usuário") @PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtém o carrinho de um usuário", description = "Retorna os detalhes do carrinho de compras de um usuário, criando um se não existir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrinho retornado com sucesso", content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping
    public ResponseEntity<Cart> getCartByUserId(
            @Parameter(description = "ID do usuário") @PathVariable Long userId) {
        Cart cart = cartService.getOrCreateCart(userId);
        // Força o carregamento dos itens do carrinho para serialização
        cart.getItems().size();
        return ResponseEntity.ok(cart);
    }

    // --- NOVO ENDPOINT: Finalizar Carrinho e Criar Pedido ---
    @Operation(summary = "Finaliza o carrinho e cria um novo pedido", description = "Converte o carrinho atual de um usuário em um pedido fechado, copiando os dados do endereço de entrega e dos itens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso", content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "400", description = "Carrinho vazio ou endereço/usuário inválido"),
            @ApiResponse(responseCode = "404", description = "Usuário, carrinho ou endereço de entrega não encontrado")
    })
    @PostMapping("/{cartId}/checkout") // Rota para finalizar um carrinho específico
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Order> checkoutCart(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @Parameter(description = "ID do carrinho a ser finalizado") @PathVariable Long cartId,
            @Parameter(description = "ID do endereço de entrega selecionado") @RequestParam Long deliveryAddressId) {
        Order newOrder = orderService.createOrderFromCart(userId, cartId, deliveryAddressId);
        return ResponseEntity.ok(newOrder); // Retorna 200 OK, embora 201 Created também seja aceitável
    }
}