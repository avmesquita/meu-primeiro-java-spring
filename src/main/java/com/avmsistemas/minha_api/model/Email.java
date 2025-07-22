package com.avmsistemas.minha_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore; // Importe para evitar loop de serialização
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalhes de um endereço de e-mail associado a um usuário")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do e-mail", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Endereço de e-mail", example = "contato@example.com")
    private String address;

    @Schema(description = "Indica se este e-mail é o principal ou de identidade para login", example = "true")
    private boolean isIdentity; // Pode ser o e-mail principal usado para login

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Essencial para evitar loop de serialização JSON com User
    @Schema(description = "Usuário ao qual este e-mail pertence")
    private User user;

    // Construtor de conveniência
    public Email(String address, boolean isIdentity, User user) {
        this.address = address;
        this.isIdentity = isIdentity;
        this.user = user;
    }
}