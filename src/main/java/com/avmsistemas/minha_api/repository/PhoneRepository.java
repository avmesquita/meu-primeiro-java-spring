package com.avmsistemas.minha_api.repository;

import com.avmsistemas.minha_api.model.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Long> {
    List<Phone> findByUserId(Long userId);
}