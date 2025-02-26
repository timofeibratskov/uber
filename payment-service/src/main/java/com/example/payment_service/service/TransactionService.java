package com.example.payment_service.service;

import com.example.payment_service.dto.TransactionRequestDto;
import com.example.payment_service.entity.CardEntity;
import com.example.payment_service.entity.TransactionEntity;
import com.example.payment_service.enums.TransactionStatus;
import com.example.payment_service.enums.TransactionType;
import com.example.payment_service.exception.BalanceTooLowException;
import com.example.payment_service.exception.InvalidPasswordException;
import com.example.payment_service.exception.NotFoundException;
import com.example.payment_service.repo.CardRepo;
import com.example.payment_service.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepo transactionRepo;
    private final CardRepo cardRepo;

    @Transactional
    public void createTransaction(TransactionType type, TransactionRequestDto dto) {
        TransactionEntity transaction = TransactionEntity
                .builder()
                .transactionType(type)
                .senderCardId(dto.senderId())
                .recipientCardId(dto.recipientId())
                .amount(dto.amount())
                .transactionDate(LocalDateTime.now())
                .rideId(dto.rideId())
                .status(TransactionStatus.PENDING)
                .build();

        transactionRepo.save(transaction);

        CardEntity senderCard = cardRepo.findById(dto.senderId()).orElseGet(() -> {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepo.save(transaction);
            throw new NotFoundException("нет карты у sender или неверный указан айди");
        });
        CardEntity recipientCard = cardRepo.findById(dto.recipientId()).orElseGet(() -> {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepo.save(transaction);
            throw new NotFoundException("нет карты у получателя или неверный указан айди");
        });


        if (!dto.password().equals(senderCard.getPassword())) {

            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepo.save(transaction);
            throw new InvalidPasswordException("неверный пароль");
        }
        if (senderCard.getBalance().compareTo(dto.amount()) >= 0) {
            BigDecimal newRecipientBalance = recipientCard.getBalance().add(dto.amount());
            recipientCard.setBalance(newRecipientBalance);
            cardRepo.save(recipientCard);

            BigDecimal newSenderBalance = senderCard.getBalance().subtract(dto.amount());
            senderCard.setBalance(newSenderBalance);
            cardRepo.save(senderCard);

            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepo.save(transaction);
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepo.save(transaction);
            throw new BalanceTooLowException("недостаточно средств. транзакция отклонена!");
        }
    }

    public List<TransactionEntity> findAll() {
        return (List<TransactionEntity>) transactionRepo.findAll();
    }

    public List<TransactionEntity> findAllTransactionsByStatus(TransactionStatus status) {
        return transactionRepo.findAllByStatus(status);
    }

    public List<TransactionEntity> findAllByTransactionType(TransactionType type) {
        return transactionRepo.findAllByTransactionType(type);
    }

    public void dropTransaction(Long id) {
        transactionRepo.deleteById(id);
    }
}
