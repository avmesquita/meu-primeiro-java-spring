package com.avmsistemas.minha_api.controller;

import com.avmsistemas.minha_api.model.User;
import com.avmsistemas.minha_api.service.UserService;
import com.avmsistemas.minha_api.service.PhoneService; // Importe
import com.avmsistemas.minha_api.service.EmailService; // Importe
import com.avmsistemas.minha_api.service.AddressService; // Importe
import com.avmsistemas.minha_api.dto.UserCreationRequest; // DTO para criar usuário
import com.avmsistemas.minha_api.dto.PhoneDTO; // Novo DTO para telefone
import com.avmsistemas.minha_api.dto.EmailDTO; // Novo DTO para email
import com.avmsistemas.minha_api.dto.AddressDTO; // Novo DTO para endereço

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Para mapear DTOs

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Operações para gerenciamento de usuários e seus contatos")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired // Injetando os serviços dos contatos para gerenciar individualmente
    private PhoneService phoneService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AddressService addressService;

    // Métodos de CRUD de Usuário (já refatorados no UserService)
    @Operation(summary = "Cria um novo usuário", description = "Registra um novo usuário no sistema com seus dados básicos e contatos iniciais.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "409", description = "Conflito: e-mail principal ou nome de usuário já existe"),
            @ApiResponse(responseCode = "400", description = "Dados do usuário inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody UserCreationRequest request) {
        User user = new User(request.getPrimaryEmail(), request.getUsername(), request.getPassword(), request.getFullName());
        // A lógica de adicionar telefones, emails e endereços ao objeto user
        // e depois passá-lo para o userService já está tratada, e o userService
        // chamará os serviços dedicados para persistir.
        if (request.getPhones() != null) {
            request.getPhones().forEach(p -> user.addPhone(PhoneDTO.toEntity(p, user))); // Mapeia DTO para entidade
        }
        if (request.getAdditionalEmails() != null) {
            request.getAdditionalEmails().forEach(e -> user.addEmail(EmailDTO.toEntity(e, user)));
        }
        if (request.getAddresses() != null) {
            request.getAddresses().forEach(a -> user.addAddress(AddressDTO.toEntity(a, user)));
        }
        return userService.createUser(user);
    }

    @Operation(summary = "Lista todos os usuários", description = "Retorna uma lista de todos os usuários cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
    @GetMapping
    public List<User> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // Força carregamento lazy para a serialização JSON
        users.forEach(u -> {
            u.getCarts().size();
            u.getPhones().size();
            u.getAdditionalEmails().size();
            u.getAddresses().size();
        });
        return users;
    }

    @Operation(summary = "Busca um usuário por ID", description = "Retorna os detalhes de um usuário específico pelo seu ID, incluindo seus contatos e carrinhos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@Parameter(description = "ID do usuário a ser buscado", required = true) @PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualiza um usuário existente", description = "Atualiza os detalhes básicos de um usuário existente pelo seu ID. Para atualizar contatos, use os endpoints específicos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito: e-mail principal ou nome de usuário já existe"),
            @ApiResponse(responseCode = "400", description = "Dados do usuário inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID do usuário a ser atualizado", required = true) @PathVariable Long id,
            @RequestBody User userDetails) { // Aqui userDetails é a entidade User, mas idealmente seria um DTO
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Exclui um usuário", description = "Remove um usuário do sistema pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID do usuário a ser excluído", required = true) @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints para gerenciar telefones de usuários ---

    @Operation(summary = "Adiciona um telefone a um usuário", description = "Associa um novo número de telefone a um usuário existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Telefone adicionado com sucesso", content = @Content(schema = @Schema(implementation = PhoneDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados do telefone inválidos")
    })
    @PostMapping("/{userId}/phones")
    @ResponseStatus(HttpStatus.CREATED)
    public PhoneDTO addPhoneToUser(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @RequestBody PhoneDTO phoneDTO) {
        return PhoneDTO.fromEntity(phoneService.createPhoneForUser(userId, PhoneDTO.toEntity(phoneDTO, null))); // Passa null para User no DTO
    }

    @Operation(summary = "Lista todos os telefones de um usuário", description = "Retorna todos os números de telefone associados a um usuário.")
    @ApiResponse(responseCode = "200", description = "Lista de telefones retornada com sucesso", content = @Content(schema = @Schema(type = "array", implementation = PhoneDTO.class)))
    @GetMapping("/{userId}/phones")
    public List<PhoneDTO> getUserPhones(@Parameter(description = "ID do usuário") @PathVariable Long userId) {
        return phoneService.getPhonesByUserId(userId).stream()
                .map(PhoneDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Atualiza um telefone de um usuário", description = "Atualiza os detalhes de um telefone específico de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Telefone atualizado com sucesso", content = @Content(schema = @Schema(implementation = PhoneDTO.class))),
            @ApiResponse(responseCode = "404", description = "Telefone não encontrado")
    })
    @PutMapping("/{userId}/phones/{phoneId}")
    public PhoneDTO updatePhone(
            @Parameter(description = "ID do usuário (não usado para lógica, apenas para rota)") @PathVariable Long userId,
            @Parameter(description = "ID do telefone") @PathVariable Long phoneId,
            @RequestBody PhoneDTO phoneDTO) {
        return PhoneDTO.fromEntity(phoneService.updatePhone(phoneId, PhoneDTO.toEntity(phoneDTO, null)));
    }

    @Operation(summary = "Remove um telefone de um usuário", description = "Desassocia e remove um número de telefone de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Telefone removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Telefone não encontrado")
    })
    @DeleteMapping("/{userId}/phones/{phoneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deletePhone(
            @Parameter(description = "ID do usuário (não usado para lógica, apenas para rota)") @PathVariable Long userId,
            @Parameter(description = "ID do telefone") @PathVariable Long phoneId) {
        phoneService.deletePhone(phoneId);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints para gerenciar e-mails adicionais de usuários ---

    @Operation(summary = "Adiciona um e-mail adicional a um usuário", description = "Associa um novo endereço de e-mail secundário a um usuário existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "E-mail adicional adicionado com sucesso", content = @Content(schema = @Schema(implementation = EmailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados do e-mail inválidos")
    })
    @PostMapping("/{userId}/emails")
    @ResponseStatus(HttpStatus.CREATED)
    public EmailDTO addEmailToUser(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @RequestBody EmailDTO emailDTO) {
        return EmailDTO.fromEntity(emailService.createEmailForUser(userId, EmailDTO.toEntity(emailDTO, null)));
    }

    @Operation(summary = "Lista todos os e-mails adicionais de um usuário", description = "Retorna todos os endereços de e-mail adicionais associados a um usuário.")
    @ApiResponse(responseCode = "200", description = "Lista de e-mails retornada com sucesso", content = @Content(schema = @Schema(type = "array", implementation = EmailDTO.class)))
    @GetMapping("/{userId}/emails")
    public List<EmailDTO> getUserEmails(@Parameter(description = "ID do usuário") @PathVariable Long userId) {
        return emailService.getEmailsByUserId(userId).stream()
                .map(EmailDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Atualiza um e-mail adicional de um usuário", description = "Atualiza os detalhes de um e-mail adicional específico de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail atualizado com sucesso", content = @Content(schema = @Schema(implementation = EmailDTO.class))),
            @ApiResponse(responseCode = "404", description = "E-mail não encontrado"),
            @ApiResponse(responseCode = "409", description = "Novo e-mail já cadastrado")
    })
    @PutMapping("/{userId}/emails/{emailId}")
    public EmailDTO updateEmail(
            @Parameter(description = "ID do usuário (não usado para lógica, apenas para rota)") @PathVariable Long userId,
            @Parameter(description = "ID do e-mail") @PathVariable Long emailId,
            @RequestBody EmailDTO emailDTO) {
        return EmailDTO.fromEntity(emailService.updateEmail(emailId, EmailDTO.toEntity(emailDTO, null)));
    }

    @Operation(summary = "Remove um e-mail adicional de um usuário", description = "Desassocia e remove um endereço de e-mail adicional de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "E-mail removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "E-mail não encontrado")
    })
    @DeleteMapping("/{userId}/emails/{emailId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteEmail(
            @Parameter(description = "ID do usuário (não usado para lógica, apenas para rota)") @PathVariable Long userId,
            @Parameter(description = "ID do e-mail") @PathVariable Long emailId) {
        emailService.deleteEmail(emailId);
        return ResponseEntity.noContent().build();
    }    

    // --- Endpoints para gerenciar endereços de usuários ---

    @Operation(summary = "Adiciona um endereço a um usuário", description = "Associa um novo endereço a um usuário existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Endereço adicionado com sucesso", content = @Content(schema = @Schema(implementation = AddressDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados do endereço inválidos (ex: mais de um principal)")
    })
    @PostMapping("/{userId}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressDTO addAddressToUser(
            @Parameter(description = "ID do usuário") @PathVariable Long userId,
            @RequestBody AddressDTO addressDTO) {
        return AddressDTO.fromEntity(addressService.createAddressForUser(userId, AddressDTO.toEntity(addressDTO, null)));
    }

    @Operation(summary = "Lista todos os endereços de um usuário", description = "Retorna todos os endereços associados a um usuário.")
    @ApiResponse(responseCode = "200", description = "Lista de endereços retornada com sucesso", content = @Content(schema = @Schema(type = "array", implementation = AddressDTO.class)))
    @GetMapping("/{userId}/addresses")
    public List<AddressDTO> getUserAddresses(@Parameter(description = "ID do usuário") @PathVariable Long userId) {
        return addressService.getAddressesByUserId(userId).stream()
                .map(AddressDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Atualiza um endereço de um usuário", description = "Atualiza os detalhes de um endereço específico de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso", content = @Content(schema = @Schema(implementation = AddressDTO.class))),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados do endereço inválidos (ex: mais de um principal)")
    })
    @PutMapping("/{userId}/addresses/{addressId}")
    public AddressDTO updateAddress(
            @Parameter(description = "ID do usuário (não usado para lógica, apenas para rota)") @PathVariable Long userId,
            @Parameter(description = "ID do endereço") @PathVariable Long addressId,
            @RequestBody AddressDTO addressDTO) {
        return AddressDTO.fromEntity(addressService.updateAddress(addressId, AddressDTO.toEntity(addressDTO, null)));
    }

    @Operation(summary = "Remove um endereço de um usuário", description = "Desassocia e remove um endereço de um usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Endereço removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    @DeleteMapping("/{userId}/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "ID do usuário (não usado para lógica, apenas para rota)") @PathVariable Long userId,
            @Parameter(description = "ID do endereço") @PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }
}