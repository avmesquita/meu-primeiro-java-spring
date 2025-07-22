package com.avmsistemas.minha_api.service;

import com.avmsistemas.minha_api.model.Phone;
import com.avmsistemas.minha_api.model.User;
import com.avmsistemas.minha_api.repository.PhoneRepository;
import com.avmsistemas.minha_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class PhoneService {

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private UserRepository userRepository; // Precisamos do UserRepository para associar o telefone a um usuário

    @Transactional
    public Phone createPhoneForUser(Long userId, Phone phoneDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        // TODO: Adicionar validações de negócio para o telefone aqui (ex: formato, unicidade, etc.)
        // Se houver regras de negócio específicas para telefones, coloque-as aqui.

        Phone newPhone = new Phone(
                phoneDetails.getNumber(),
                phoneDetails.getType(),
                phoneDetails.isHasWhatsapp(),
                phoneDetails.isHasSignal(),
                phoneDetails.isHasTelegram(),
                user
        );
        user.addPhone(newPhone); // Garante a ligação bidirecional na entidade User também
        return phoneRepository.save(newPhone);
    }

    @Transactional(readOnly = true)
    public List<Phone> getPhonesByUserId(Long userId) {
        return phoneRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Phone> getPhoneById(Long phoneId) {
        return phoneRepository.findById(phoneId);
    }

    @Transactional
    public Phone updatePhone(Long phoneId, Phone phoneDetails) {
        Phone existingPhone = phoneRepository.findById(phoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Telefone não encontrado."));

        // TODO: Adicionar validações antes de atualizar (ex: garantir que o telefone ainda pertence ao mesmo usuário se necessário)

        existingPhone.setNumber(phoneDetails.getNumber());
        existingPhone.setType(phoneDetails.getType());
        existingPhone.setHasWhatsapp(phoneDetails.isHasWhatsapp());
        existingPhone.setHasSignal(phoneDetails.isHasSignal());
        existingPhone.setHasTelegram(phoneDetails.isHasTelegram());

        return phoneRepository.save(existingPhone);
    }

    @Transactional
    public void deletePhone(Long phoneId) {
        if (!phoneRepository.existsById(phoneId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Telefone não encontrado para exclusão.");
        }
        phoneRepository.deleteById(phoneId);
    }
}