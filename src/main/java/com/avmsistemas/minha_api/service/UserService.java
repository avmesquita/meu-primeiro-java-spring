package com.avmsistemas.minha_api.service;

import com.avmsistemas.minha_api.model.User;
import com.avmsistemas.minha_api.model.Phone; // Importe
import com.avmsistemas.minha_api.model.Email; // Importe
import com.avmsistemas.minha_api.model.Address; // Importe
import com.avmsistemas.minha_api.repository.UserRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set; // Pode ser útil para verificar unicidade, etc.
import java.util.stream.Collectors; // Para streams

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired 
    private PhoneService phoneService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AddressService addressService;

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> {
            u.getCarts().size(); // Força carrinhos
            u.getPhones().size(); // Força telefones
            u.getAdditionalEmails().size(); // Força e-mails
            u.getAddresses().size(); // Força endereços
        });
        return user;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.findByPrimaryEmail(user.getPrimaryEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail principal já cadastrado.");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nome de usuário já existe.");
        }
        // TODO: Em um sistema real, hash da senha aqui antes de salvar

        // Salva o usuário principal primeiro para obter o ID
        User savedUser = userRepository.save(user);

        // Delega a criação dos contatos para os serviços especializados
        if (user.getPhones() != null && !user.getPhones().isEmpty()) {
            user.getPhones().forEach(phone -> phoneService.createPhoneForUser(savedUser.getId(), phone));
        }
        if (user.getAdditionalEmails() != null && !user.getAdditionalEmails().isEmpty()) {
            user.getAdditionalEmails().forEach(email -> emailService.createEmailForUser(savedUser.getId(), email));
        }
        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            user.getAddresses().forEach(address -> addressService.createAddressForUser(savedUser.getId(), address));
        }

        // Recarrega o usuário para ter as coleções populadas pelo JPA (opcional, dependendo do fetch type e DTO)
        return userRepository.findById(savedUser.getId()).orElse(savedUser);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        // Validações de unicidade para e-mail principal e username
        if (!existingUser.getPrimaryEmail().equals(userDetails.getPrimaryEmail()) &&
            userRepository.findByPrimaryEmail(userDetails.getPrimaryEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Novo e-mail principal já cadastrado para outro usuário.");
        }
        if (!existingUser.getUsername().equals(userDetails.getUsername()) &&
            userRepository.findByUsername(userDetails.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Novo nome de usuário já existe para outro usuário.");
        }

        existingUser.setPrimaryEmail(userDetails.getPrimaryEmail());
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setFullName(userDetails.getFullName());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(userDetails.getPassword()); // LEMBRE-SE DO HASH DA SENHA AQUI
        }

        // --- Lógica de atualização de coleções agora deve delegar ou gerenciar ID a ID ---
        // Para simplificar, vou manter uma lógica mais básica aqui, mas em produção você faria
        // um "diff" entre a lista antiga e a nova para adicionar/remover/atualizar itens específicos
        // Para um CRUD completo em coleções, você teria endpoints separados para adicionar/remover um item por vez.

        // Exemplo Simplificado: Deleta todos os antigos e cria os novos (NÃO É OTIMIZADO!)
        // O ideal é comparar e fazer as operações de CRUD específicas para cada item
        phoneService.getPhonesByUserId(id).forEach(phone -> phoneService.deletePhone(phone.getId()));
        if (userDetails.getPhones() != null) {
            userDetails.getPhones().forEach(phone -> phoneService.createPhoneForUser(id, phone));
        }

        emailService.getEmailsByUserId(id).forEach(email -> emailService.deleteEmail(email.getId()));
        if (userDetails.getAdditionalEmails() != null) {
            userDetails.getAdditionalEmails().forEach(email -> emailService.createEmailForUser(id, email));
        }

        addressService.getAddressesByUserId(id).forEach(address -> addressService.deleteAddress(address.getId()));
        if (userDetails.getAddresses() != null) {
            userDetails.getAddresses().forEach(address -> addressService.createAddressForUser(id, address));
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado para exclusão.");
        }
        userRepository.deleteById(id);
    }
}