package com.avmsistemas.minha_api.service;

import com.avmsistemas.minha_api.model.Address;
import com.avmsistemas.minha_api.model.User;
import com.avmsistemas.minha_api.repository.AddressRepository;
import com.avmsistemas.minha_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Address createAddressForUser(Long userId, Address addressDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        // Se o novo endereço for o principal, desative os outros principais para este usuário
        if (addressDetails.isPrimary()) {
            addressRepository.findByUserIdAndIsPrimaryTrue(userId).stream()
                    .forEach(addr -> {
                        addr.setPrimary(false);
                        addressRepository.save(addr);
                    });
        }

        Address newAddress = new Address(
                addressDetails.getStreet(),
                addressDetails.getNumber(),
                addressDetails.getComplement(),
                addressDetails.getNeighborhood(),
                addressDetails.getCity(),
                addressDetails.getState(),
                addressDetails.getZipCode(),
                addressDetails.getCountry(),
                addressDetails.isPrimary(),
                user
        );
        user.addAddress(newAddress); // Garante a ligação bidirecional na entidade User
        return addressRepository.save(newAddress);
    }

    @Transactional(readOnly = true)
    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Address> getAddressById(Long addressId) {
        return addressRepository.findById(addressId);
    }

    @Transactional
    public Address updateAddress(Long addressId, Address addressDetails) {
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado."));

        // Se o endereço está sendo definido como principal, desative os outros
        if (addressDetails.isPrimary() && !existingAddress.isPrimary()) {
            addressRepository.findByUserIdAndIsPrimaryTrue(existingAddress.getUser().getId()).stream()
                    .filter(addr -> !addr.getId().equals(addressId)) // Ignora o próprio endereço sendo atualizado
                    .forEach(addr -> {
                        addr.setPrimary(false);
                        addressRepository.save(addr);
                    });
        }

        existingAddress.setStreet(addressDetails.getStreet());
        existingAddress.setNumber(addressDetails.getNumber());
        existingAddress.setComplement(addressDetails.getComplement());
        existingAddress.setNeighborhood(addressDetails.getNeighborhood());
        existingAddress.setCity(addressDetails.getCity());
        existingAddress.setState(addressDetails.getState());
        existingAddress.setZipCode(addressDetails.getZipCode());
        existingAddress.setCountry(addressDetails.getCountry());
        existingAddress.setPrimary(addressDetails.isPrimary());

        return addressRepository.save(existingAddress);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        if (!addressRepository.existsById(addressId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado para exclusão.");
        }
        addressRepository.deleteById(addressId);
    }
}