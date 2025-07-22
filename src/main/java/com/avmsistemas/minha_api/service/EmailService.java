package com.avmsistemas.minha_api.service;

import com.avmsistemas.minha_api.model.Email;
import com.avmsistemas.minha_api.model.User;
import com.avmsistemas.minha_api.repository.EmailRepository;
import com.avmsistemas.minha_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class EmailService {

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Email createEmailForUser(Long userId, Email emailDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        // Validação de unicidade para e-mail adicional em todo o sistema
        if (emailRepository.findByAddress(emailDetails.getAddress()).isPresent() ||
            userRepository.findByPrimaryEmail(emailDetails.getAddress()).isPresent()) { // Verifica se é primary de outro
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail '" + emailDetails.getAddress() + "' já cadastrado em outro usuário (seja como principal ou adicional).");
        }

        Email newEmail = new Email(
                emailDetails.getAddress(),
                emailDetails.isIdentity(),
                user
        );
        user.addEmail(newEmail); // Garante a ligação bidirecional na entidade User
        return emailRepository.save(newEmail);
    }

    @Transactional(readOnly = true)
    public List<Email> getEmailsByUserId(Long userId) {
        return emailRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Email> getEmailById(Long emailId) {
        return emailRepository.findById(emailId);
    }

    @Transactional
    public Email updateEmail(Long emailId, Email emailDetails) {
        Email existingEmail = emailRepository.findById(emailId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail não encontrado."));

        // Validação de unicidade ao atualizar: permite o próprio e-mail, mas não outros existentes
        if (!existingEmail.getAddress().equals(emailDetails.getAddress())) {
            if (emailRepository.findByAddress(emailDetails.getAddress()).isPresent() ||
                userRepository.findByPrimaryEmail(emailDetails.getAddress()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Novo e-mail '" + emailDetails.getAddress() + "' já cadastrado.");
            }
        }

        existingEmail.setAddress(emailDetails.getAddress());
        existingEmail.setIdentity(emailDetails.isIdentity());

        return emailRepository.save(existingEmail);
    }

    @Transactional
    public void deleteEmail(Long emailId) {
        if (!emailRepository.existsById(emailId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail não encontrado para exclusão.");
        }
        emailRepository.deleteById(emailId);
    }
}