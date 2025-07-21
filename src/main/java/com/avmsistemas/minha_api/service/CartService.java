package com.avmsistemas.minha_api.service;

import com.avmsistemas.minha_api.model.Cart;
import com.avmsistemas.minha_api.model.CartItem;
import com.avmsistemas.minha_api.model.CartStatus;
import com.avmsistemas.minha_api.model.PriceHistory;
import com.avmsistemas.minha_api.model.Product;
import com.avmsistemas.minha_api.model.User; // Importe a entidade User
import com.avmsistemas.minha_api.repository.CartRepository;
import com.avmsistemas.minha_api.repository.ProductRepository;
import com.avmsistemas.minha_api.repository.UserRepository; // Importe o UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository; // Injetar UserRepository

    // Método para criar um novo carrinho (para convidado)
    @Transactional
    public Cart createCart() {
        Cart cart = new Cart();
        // Não associado a um usuário específico por enquanto
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart createGuestCart() {
        Cart cart = new Cart(); // Usa o construtor padrão (sem argumentos)
        // O campo 'user' será null.
        // O status e timestamps serão preenchidos por @PrePersist.
        return cartRepository.save(cart);
    }

    /*
    @Transactional
    public Cart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        // Você pode usar o construtor de conveniência:
        Cart cart = new Cart(user);
        user.addCart(cart); // Garante a ligação bidirecional

        // Ou usar o construtor padrão e depois o setter:
        // Cart cart = new Cart();
        // cart.setUser(user);
        // user.addCart(cart);

        return cartRepository.save(cart);
    }*/

    // Método para criar um novo carrinho para um usuário existente
    @Transactional
    public Cart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        // Opcional: Verificar se o usuário já tem um carrinho PENDING
        Optional<Cart> existingPendingCart = cartRepository.findByUserIdAndStatus(userId, CartStatus.PENDING);
        if (existingPendingCart.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já possui um carrinho PENDING ativo.");
        }

        Cart cart = new Cart(user); // Usa o novo construtor
        user.addCart(cart); // Adiciona o carrinho ao usuário (bidirecional)
        return cartRepository.save(cart);
    }


    @Transactional(readOnly = true)
    public Optional<Cart> getCartById(Long id) {
        Optional<Cart> cart = cartRepository.findById(id);
        cart.ifPresent(c -> {
            c.getItems().size(); // Força o carregamento dos itens
            if (c.getUser() != null) {
                c.getUser().getId(); // Acessa o ID do usuário para garantir que o proxy seja inicializado se necessário em DTOs futuros
            }
        });
        return cart;
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
