package com.avmsistemas.minha_api.dto;

import com.avmsistemas.minha_api.model.Phone;
import com.avmsistemas.minha_api.model.Email;
import com.avmsistemas.minha_api.model.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Dados para criação de um novo usuário")
public class UserCreationRequest {
    @Schema(description = "E-mail principal do usuário para login", example = "novo.usuario@example.com", required = true)
    private String primaryEmail;
    @Schema(description = "Nome de usuário único", example = "novo_user", required = true)
    private String username;
    @Schema(description = "Senha do usuário (em texto puro para este DTO, mas será hashed)", example = "MinhaSenhaSecreta123", required = true)
    private String password;
    @Schema(description = "Nome completo do usuário", example = "Novo Usuário Teste")
    private String fullName;

    @Schema(description = "Lista de telefones do usuário")
    private List<PhoneDTO> phones; // <-- ALTERADO AQUI para PhoneDTO

    @Schema(description = "Lista de e-mails adicionais do usuário")
    private List<EmailDTO> additionalEmails; // <-- ALTERADO AQUI para EmailDTO

    @Schema(description = "Lista de endereços do usuário")
    private List<AddressDTO> addresses; // <-- ALTERADO AQUI para AddressDTO
}