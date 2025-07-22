package com.avmsistemas.minha_api.dto;

import com.avmsistemas.minha_api.model.Address;
import com.avmsistemas.minha_api.model.User; // Importe
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de Transferência de Dados para Endereço")
public class AddressDTO {

    @Schema(description = "ID único do endereço (opcional para criação)", example = "1")
    private Long id;

    @Schema(description = "Nome do logradouro", example = "Rua das Flores", required = true)
    private String street;

    @Schema(description = "Número do endereço", example = "123")
    private String number;

    @Schema(description = "Complemento (opcional)", example = "Bloco A, Apto 401")
    private String complement;

    @Schema(description = "Bairro", example = "Centro", required = true)
    private String neighborhood;

    @Schema(description = "Cidade", example = "Niterói", required = true)
    private String city;

    @Schema(description = "Estado (UF)", example = "RJ", required = true)
    private String state;

    @Schema(description = "CEP", example = "24000-000", required = true)
    private String zipCode;

    @Schema(description = "País", example = "Brasil", required = true)
    private String country;

    @Schema(description = "Indica se este é o endereço principal do usuário", example = "true")
    private boolean isPrimary;

    // Métodos para converter entre Entidade e DTO
    public static AddressDTO fromEntity(Address address) {
        return new AddressDTO(
                address.getId(),
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry(),
                address.isPrimary()
        );
    }

    public static Address toEntity(AddressDTO dto, User user) { // Recebe User para associar
        Address address = new Address();
        address.setId(dto.getId());
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setComplement(dto.getComplement());
        address.setNeighborhood(dto.getNeighborhood());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setCountry(dto.getCountry());
        address.setPrimary(dto.isPrimary());
        address.setUser(user); // Associa o usuário passado
        return address;
    }
}