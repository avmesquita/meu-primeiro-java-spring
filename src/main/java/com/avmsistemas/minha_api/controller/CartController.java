package com.avmsistemas.minha_api.controller;

import com.avmsistemas.minha_api.model.Cart;
import com.avmsistemas.minha_api.model.CartStatus;
import com.avmsistemas.minha_api.service.CartService;

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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Carrinhos de Compras", description = "Operações para gerenciamento de carrinhos de compras")
public class CartController {

    @Autowired
    private CartService cartService; // Injeta o serviço

    // DTOs permanecem no controller ou em um pacote de DTOs separados
    @Schema(description = "Objeto para adicionar um produto ao carrinho")
    public static class AddItemToCartRequest {
        @Schema(description = "ID do produto a ser adicionado", example = "1")
        public Long productId;
        @Schema(description = "Quantidade do produto", example = "1")
        public Integer quantity;
    }

    @Schema(description = "Objeto para atualizar o status do carrinho")
    public static class UpdateCartStatusRequest {
        @Schema(description = "Novo status do carrinho", example = "COMPLETED")
        public CartStatus status;
        @Schema(description = "Instruções de pagamento (apenas se status for COMPLETED)", example = "Cartão de crédito via Stripe")
        public String paymentInstructions;
    }

    @Operation(summary = "Cria um novo carrinho de compras")
    @ApiResponse(responseCode = "201", description = "Carrinho criado com sucesso", content = @Content(schema = @Schema(implementation = Cart.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cart createCart() {
        return cartService.createCart(); // Delega para o serviço
    }

    @Operation(summary = "Obtém um carrinho pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrinho encontrado", content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "404", description = "Carrinho não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Cart> getCartById(@Parameter(description = "ID do carrinho") @PathVariable Long id) {
        Optional<Cart> cart = cartService.getCartById(id); // Delega para o serviço
        // Force lazy loading of items if they are to be included in the JSON response
        // Isso ainda pode ser necessário aqui, pois a serialização ocorre após o serviço
        // Ou, uma solução melhor é fazer com que o serviço retorne um DTO já com os itens carregados.
        cart.ifPresent(c -> c.getItems().size());
        return cart.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Adiciona um item a um carrinho existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item adicionado com sucesso", content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "404", description = "Carrinho ou Produto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida")
    })
    @PostMapping("/{cartId}/items")
    public ResponseEntity<Cart> addItemToCart(
            @Parameter(description = "ID do carrinho") @PathVariable Long cartId,
            @RequestBody AddItemToCartRequest request) {
        Cart updatedCart = cartService.addItemToCart(cartId, request.productId, request.quantity); // Delega para o serviço
        updatedCart.getItems().size(); // Força o carregamento para serialização JSON
        return ResponseEntity.ok(updatedCart);
    }

    @Operation(summary = "Atualiza a quantidade de um item no carrinho ou remove-o")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantidade do item atualizada", content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "404", description = "Carrinho ou Item do Carrinho não encontrado"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida")
    })
    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<Cart> updateCartItemQuantity(
            @Parameter(description = "ID do carrinho") @PathVariable Long cartId,
            @Parameter(description = "ID do item do carrinho") @PathVariable Long itemId,
            @Parameter(description = "Nova quantidade do item (0 para remover)", example = "3") @RequestParam Integer quantity) {
        Cart updatedCart = cartService.updateCartItemQuantity(cartId, itemId, quantity); // Delega para o serviço
        updatedCart.getItems().size(); // Força o carregamento
        return ResponseEntity.ok(updatedCart);
    }

    @Operation(summary = "Remove um item específico de um carrinho")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Carrinho ou Item do Carrinho não encontrado")
    })
    @DeleteMapping("/{cartId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeCartItem(
            @Parameter(description = "ID do carrinho") @PathVariable Long cartId,
            @Parameter(description = "ID do item do carrinho") @PathVariable Long itemId) {
        cartService.removeCartItem(cartId, itemId); // Delega para o serviço
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualiza o status de um carrinho (finalizar, abandonar, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do carrinho atualizado", content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "404", description = "Carrinho não encontrado"),
            @ApiResponse(responseCode = "400", description = "Status inválido para a transição")
    })
    @PutMapping("/{cartId}/status")
    public ResponseEntity<Cart> updateCartStatus(
            @Parameter(description = "ID do carrinho") @PathVariable Long cartId,
            @RequestBody UpdateCartStatusRequest request) {
        Cart updatedCart = cartService.updateCartStatus(cartId, request.status, request.paymentInstructions); // Delega para o serviço
        updatedCart.getItems().size(); // Força o carregamento
        return ResponseEntity.ok(updatedCart);
    }
}