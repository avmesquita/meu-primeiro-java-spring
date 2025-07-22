package com.avmsistemas.minha_api.dto;

import com.avmsistemas.minha_api.model.Email;
import com.avmsistemas.minha_api.model.User; // Importe
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de Transferência de Dados para E-mail Adicional")
public class EmailDTO {

    @Schema(description = "ID único do e-mail (opcional para criação)", example = "1")
    private Long id;

    @Schema(description = "Endereço de e-mail", example = "contato@example.com", required = true)
    private String address;

    @Schema(description = "Indica se este e-mail é de identidade (para login ou principal)", example = "false")
    private boolean isIdentity; // Para e-mails adicionais, isso seria 'false' na maioria dos casos

    // Métodos para converter entre Entidade e DTO
    public static EmailDTO fromEntity(Email email) {
        return new EmailDTO(
                email.getId(),
                email.getAddress(),
                email.isIdentity()
        );
    }

    public static Email toEntity(EmailDTO dto, User user) { // Recebe User para associar
        Email email = new Email();
        email.setId(dto.getId());
        email.setAddress(dto.getAddress());
        email.setIdentity(dto.isIdentity());
        email.setUser(user); // Associa o usuário passado
        return email;
    }
}