package com.avmsistemas.minha_api.service;

import com.avmsistemas.minha_api.model.Cart;
import com.avmsistemas.minha_api.model.CartItem;
import com.avmsistemas.minha_api.model.CartStatus;
import com.avmsistemas.minha_api.model.PriceHistory;
import com.avmsistemas.minha_api.model.Product;
import com.avmsistemas.minha_api.repository.CartRepository;
import com.avmsistemas.minha_api.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importe esta anotação
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

@Service // Indica que esta classe é um componente de serviço gerenciado pelo Spring
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    // Método para criar um novo carrinho
    @Transactional // Garante que toda a operação ocorra em uma única transação
    public Cart createCart() {
        Cart cart = new Cart();
        return cartRepository.save(cart);
    }

    // Método para obter um carrinho pelo ID
    @Transactional(readOnly = true) // Otimiza para operações de leitura
    public Optional<Cart> getCartById(Long id) {
        // Ao buscar um carrinho, se a lista de itens for acessada fora do service,
        // pode precisar de um DTO ou de carregar eagerly, ou ainda forçar o carregamento aqui
        // Ex: cart.ifPresent(c -> c.getItems().size());
        return cartRepository.findById(id);
    }

    // Método para adicionar um item ao carrinho
    @Transactional
    public Cart addItemToCart(Long cartId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade do item deve ser maior que zero.");
        }

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrinho não encontrado."));

        // TODO: Em um e-commerce real, verificaríamos o status do carrinho aqui (ex: não adicionar se COMPLETED)
        if (cart.getStatus() != CartStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível adicionar itens a um carrinho que não esteja PENDING.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem(product, quantity, product.getPrice(), cart);
            cart.addItem(newItem);
        }

        return cartRepository.save(cart);
    }

    // Método para atualizar a quantidade de um item no carrinho
    @Transactional
    public Cart updateCartItemQuantity(Long cartId, Long itemId, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade do item não pode ser negativa.");
        }

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrinho não encontrado."));

        if (cart.getStatus() != CartStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível alterar itens de um carrinho que não esteja PENDING.");
        }

        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item do carrinho não encontrado neste carrinho."));

        if (quantity == 0) {
            cart.removeItem(itemToUpdate);
        } else {
            itemToUpdate.setQuantity(quantity);
        }

        return cartRepository.save(cart);
    }

    // Método para remover um item do carrinho
    @Transactional
    public void removeCartItem(Long cartId, Long itemId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrinho não encontrado."));

        if (cart.getStatus() != CartStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível remover itens de um carrinho que não esteja PENDING.");
        }

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item do carrinho não encontrado neste carrinho."));

        cart.removeItem(itemToRemove);
        cartRepository.save(cart); // Persiste a remoção
    }

    // Método para atualizar o status do carrinho
    @Transactional
    public Cart updateCartStatus(Long cartId, CartStatus newStatus, String paymentInstructions) {
        if (newStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status é obrigatório.");
        }

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrinho não encontrado."));

        // Lógica de transição de status mais robusta (exemplo)
        if (cart.getStatus() == CartStatus.COMPLETED || cart.getStatus() == CartStatus.ABANDONED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível alterar o status de um carrinho já finalizado ou abandonado.");
        }

        if (newStatus == CartStatus.COMPLETED) {
            if (paymentInstructions == null || paymentInstructions.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instruções de pagamento são obrigatórias para finalizar a compra.");
            }
            // TODO: Adicionar lógica de processamento de pagamento aqui
            // TODO: Reduzir estoque dos produtos
            cart.setPaymentInstructions(paymentInstructions);
        } else {
            cart.setPaymentInstructions(null);
        }

        cart.setStatus(newStatus);
        return cartRepository.save(cart);
    }

    // Métodos para o ProductService (apenas para referência, não vamos refatorar o ProductController agora)
    @Transactional
    public Product createProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        PriceHistory initialPrice = new PriceHistory(savedProduct.getPrice(), savedProduct);
        savedProduct.addPriceHistory(initialPrice);
        return productRepository.save(savedProduct);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isPresent()) {
            Product existingProduct = productOptional.get();

            if (!existingProduct.getPrice().equals(productDetails.getPrice())) {
                PriceHistory newPriceEntry = new PriceHistory(productDetails.getPrice(), existingProduct);
                existingProduct.addPriceHistory(newPriceEntry);
            }

            existingProduct.setName(productDetails.getName());
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setPrice(productDetails.getPrice());

            return productRepository.save(existingProduct);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado.");
        }
    }
}
