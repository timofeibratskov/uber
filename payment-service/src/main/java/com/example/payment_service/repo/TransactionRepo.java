package com.example.payment_service.repo;

import com.example.payment_service.entity.TransactionEntity;
import com.example.payment_service.enums.TransactionStatus;
import com.example.payment_service.enums.TransactionType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TransactionRepo extends CrudRepository<TransactionEntity, Long> {
    List<TransactionEntity> findAllByStatus(TransactionStatus status);

    List<TransactionEntity> findAllByTransactionType(TransactionType type);

}
