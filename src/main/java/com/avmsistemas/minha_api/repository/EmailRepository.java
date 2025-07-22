package com.avmsistemas.minha_api.repository;

import com.avmsistemas.minha_api.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
    List<Email> findByUserId(Long userId);
    Optional<Email> findByAddress(String address); // Para garantir unicidade se necessário
    List<Email> findByUserIdAndIsIdentityTrue(Long userId); // Encontrar e-mail de identidade
}