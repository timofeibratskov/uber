package com.example.payment_service.repo;

import com.example.payment_service.entity.CardEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepo extends CrudRepository<CardEntity, Long> {
    boolean existsByCardNumber(String cardNumber);
}
