package com.example.payment_service.repo;

import com.example.payment_service.entity.CardEntity;
import com.example.payment_service.enums.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepo extends CrudRepository<CardEntity, Long> {
    boolean existsByCardNumber(String cardNumber);
    Optional<CardEntity> findByOwnerIdAndRole(Long ownerId, Role role);
}
