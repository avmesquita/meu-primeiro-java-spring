package com.avmsistemas.minha_api.repository;

import com.avmsistemas.minha_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPrimaryEmail(String primaryEmail);     
    Optional<User> findByUsername(String username);    
}