package com.avmsistemas.minha_api.service;

import com.avmsistemas.minha_api.model.User;
import com.avmsistemas.minha_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nome de usuário já existe.");
        }
        // TODO: Em um sistema real, hash da senha aqui antes de salvar
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        // Opcional: Forçar carregamento de carrinhos para um DTO, se necessário
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> u.getCarts().size()); // Força o carregamento lazy dos carrinhos
        return user;
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (!existingUser.getEmail().equals(userDetails.getEmail()) && userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Novo e-mail já cadastrado para outro usuário.");
        }
        if (!existingUser.getUsername().equals(userDetails.getUsername()) && userRepository.findByUsername(userDetails.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Novo nome de usuário já existe para outro usuário.");
        }

        existingUser.setEmail(userDetails.getEmail());
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setFullName(userDetails.getFullName());
        // TODO: Lógica para atualizar senha (não atualizar se estiver vazio, exigir confirmação, etc.)
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(userDetails.getPassword()); // Lembre-se do HASH
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