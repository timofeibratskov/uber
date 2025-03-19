package com.example.payment_service.service;

import com.example.payment_service.client.RideServiceClient;
import com.example.payment_service.dto.TransactionRequestDto;
import com.example.payment_service.entity.CardEntity;
import com.example.payment_service.entity.TransactionEntity;
import com.example.payment_service.enums.Role;
import com.example.payment_service.enums.TransactionStatus;
import com.example.payment_service.enums.TransactionType;
import com.example.payment_service.exception.BalanceTooLowException;
import com.example.payment_service.exception.InvalidPasswordException;
import com.example.payment_service.exception.NotFoundException;
import com.example.payment_service.mapper.TransactionMapper;
import com.example.payment_service.repo.CardRepo;
import com.example.payment_service.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.payment_service.enums.Role.DRIVER;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepo transactionRepo;
    private final CardRepo cardRepo;
    private final RideServiceClient client;
    private final TransactionMapper transactionMapper;

    @Transactional
    public void createTransaction(TransactionType type, TransactionRequestDto dto) {

        TransactionEntity transaction = transactionMapper.toEntity(dto);
        transaction.setTransactionType(type);

        transactionRepo.save(transaction);
        System.out.println("transaction created " + transaction.toString());


        boolean isRidePayment = transaction.getTransactionType() == TransactionType.RIDE_PAYMENT;

        Role senderRole = isRidePayment ? Role.PASSENGER : Role.DRIVER;
        Role recipientRole = isRidePayment ? Role.DRIVER : Role.PASSENGER;
        System.out.println("транзакция типа " + transaction.getTransactionType() + " ," +
                "будет выполняться между отправителем и получателем " +
                transaction.getSenderId() + " и " + transaction.getRecipientId());

        CardEntity senderCard = cardRepo.findByOwnerIdAndRole(dto.senderId(), senderRole).orElseGet(() -> {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepo.save(transaction);
            throw new NotFoundException("нет карты у sender или неверный указан айди");
        });
        System.out.println("карточка у пользователя отправителя есть " + senderCard.toString());
        CardEntity recipientCard = cardRepo.findByOwnerIdAndRole(dto.recipientId(), recipientRole).orElseGet(() -> {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepo.save(transaction);
            throw new NotFoundException("нет карты у получателя или неверный указан айди");
        });
        System.out.println("карточка у пользователя получателя есть " + recipientCard.toString());


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
            client.payRide(dto.rideId(), dto.amount());
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
