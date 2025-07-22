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
@Schema(description = "Detalhes de um endereço associado a um usuário")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do endereço", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nome do logradouro", example = "Rua das Flores")
    private String street;

    @Schema(description = "Número do endereço", example = "123")
    private String number; // Pode ser String para incluir "s/n" ou "apto X"

    @Schema(description = "Complemento (opcional)", example = "Bloco A, Apto 401")
    private String complement;

    @Column(nullable = false)
    @Schema(description = "Bairro", example = "Centro")
    private String neighborhood;

    @Column(nullable = false)
    @Schema(description = "Cidade", example = "Niterói")
    private String city;

    @Column(nullable = false, length = 2)
    @Schema(description = "Estado (UF)", example = "RJ")
    private String state;

    @Column(nullable = false, length = 10) // Ex: "24000-000"
    @Schema(description = "CEP", example = "24000-000")
    private String zipCode;

    @Column(nullable = false)
    @Schema(description = "País", example = "Brasil")
    private String country;

    @Schema(description = "Indica se este é o endereço principal do usuário", example = "true")
    private boolean isPrimary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Essencial para evitar loop de serialização JSON com User
    @Schema(description = "Usuário ao qual este endereço pertence")
    private User user;

    // Construtor de conveniência
    public Address(String street, String number, String complement, String neighborhood, String city, String state, String zipCode, String country, boolean isPrimary, User user) {
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
        this.isPrimary = isPrimary;
        this.user = user;
    }
}