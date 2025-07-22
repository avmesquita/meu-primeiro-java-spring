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
@Schema(description = "Detalhes de um número de telefone associado a um usuário")
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do telefone", example = "1")
    private Long id;

    @Column(nullable = false, length = 20)
    @Schema(description = "Número de telefone completo, incluindo código do país e DDD", example = "+5521987654321")
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Tipo de telefone", example = "MOBILE")
    private PhoneType type; // MOBILE, HOME, WORK

    @Schema(description = "Indica se possui WhatsApp", example = "true")
    private boolean hasWhatsapp;

    @Schema(description = "Indica se possui Signal", example = "false")
    private boolean hasSignal;

    @Schema(description = "Indica se possui Telegram", example = "true")
    private boolean hasTelegram;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Essencial para evitar loop de serialização JSON com User
    @Schema(description = "Usuário ao qual este telefone pertence")
    private User user;

    // Construtor de conveniência
    public Phone(String number, PhoneType type, boolean hasWhatsapp, boolean hasSignal, boolean hasTelegram, User user) {
        this.number = number;
        this.type = type;
        this.hasWhatsapp = hasWhatsapp;
        this.hasSignal = hasSignal;
        this.hasTelegram = hasTelegram;
        this.user = user;
    }
}