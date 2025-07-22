package com.avmsistemas.minha_api.repository;

import com.avmsistemas.minha_api.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    List<Address> findByUserIdAndIsPrimaryTrue(Long userId); // Encontrar endereço principal
}