package com.avmsistemas.minha_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user") // Renomeia a tabela para evitar conflito com 'USER' que é uma palavra reservada em alguns DBs
@Schema(description = "Representa um usuário do sistema")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do usuário", example = "1")
    private Long id;

    @Column(unique = true, nullable = false)
    @Schema(description = "Endereço de e-mail do usuário (único)", example = "usuario@example.com")
    private String primaryEmail;

    @Column(nullable = false)
    @Schema(description = "Nome de usuário", example = "john_doe")
    private String username;

    @Column(nullable = false)
    @Schema(description = "Senha do usuário (em um sistema real, seria hash)", example = "senhaSegura123")
    private String password; // Em produção, SEMPRE armazene hashes de senhas!

    @Schema(description = "Nome completo do usuário", example = "João da Silva")
    private String fullName;

    // --- Novas coleções de contatos ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "Lista de telefones associados a este usuário")
    private List<Phone> phones = new ArrayList<>();

    // O primaryEmail acima é para o e-mail de login.
    // Esta lista é para e-mails adicionais, como "contato@", "trabalho@" etc.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "Lista de e-mails adicionais associados a este usuário")
    private List<Email> additionalEmails = new ArrayList<>(); // Renomeado para evitar conflito com 'email'

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "Lista de endereços associados a este usuário")
    private List<Address> addresses = new ArrayList<>();
    // --- Fim das novas coleções ---

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "Lista de carrinhos associados a este usuário")
    private List<Cart> carts = new ArrayList<>(); // Lista de carrinhos do usuário

    @Schema(description = "Data de criação do usuário")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização do usuário")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Construtor sem ID e timestamps para facilitar a criação inicial
    public User(String email, String username, String password, String fullName) {
        this.primaryEmail = email;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public void addPhone(Phone phone) {
        phones.add(phone);
        phone.setUser(this);
    }    

    public void removePhone(Phone phone) {
        phones.remove(phone);
        phone.setUser(null);
    }

    public void addEmail(Email email) {
        additionalEmails.add(email);
        email.setUser(this);
    }

    public void removeEmail(Email email) {
        additionalEmails.remove(email);
        email.setUser(null);
    }

    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUser(null);
    }

    // Método auxiliar para adicionar um carrinho (bidirecional)
    public void addCart(Cart cart) {
        carts.add(cart);
        cart.setUser(this);
    }
}