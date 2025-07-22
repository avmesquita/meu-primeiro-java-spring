package com.avmsistemas.minha_api.dto;

import com.avmsistemas.minha_api.model.Phone;
import com.avmsistemas.minha_api.model.PhoneType;
import com.avmsistemas.minha_api.model.User; // Importe
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de Transferência de Dados para Telefone")
public class PhoneDTO {

    @Schema(description = "ID único do telefone (opcional para criação)", example = "1")
    private Long id;

    @Schema(description = "Número de telefone completo", example = "+5521987654321", required = true)
    private String number;

    @Schema(description = "Tipo de telefone", example = "MOBILE", required = true)
    private PhoneType type;

    @Schema(description = "Indica se possui WhatsApp", example = "true")
    private boolean hasWhatsapp;

    @Schema(description = "Indica se possui Signal", example = "false")
    private boolean hasSignal;

    @Schema(description = "Indica se possui Telegram", example = "true")
    private boolean hasTelegram;

    // Métodos para converter entre Entidade e DTO
    public static PhoneDTO fromEntity(Phone phone) {
        return new PhoneDTO(
                phone.getId(),
                phone.getNumber(),
                phone.getType(),
                phone.isHasWhatsapp(),
                phone.isHasSignal(),
                phone.isHasTelegram()
        );
    }

    public static Phone toEntity(PhoneDTO dto, User user) { // Recebe User para associar
        Phone phone = new Phone();
        phone.setId(dto.getId()); // ID pode ser nulo para nova criação
        phone.setNumber(dto.getNumber());
        phone.setType(dto.getType());
        phone.setHasWhatsapp(dto.isHasWhatsapp());
        phone.setHasSignal(dto.isHasSignal());
        phone.setHasTelegram(dto.isHasTelegram());
        phone.setUser(user); // Associa o usuário passado
        return phone;
    }
}