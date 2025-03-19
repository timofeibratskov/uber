package com.example.payment_service;

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
import com.example.payment_service.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepo transactionRepo;

    @Mock
    private CardRepo cardRepo;

    @Mock
    private RideServiceClient rideServiceClient;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionRequestDto transactionRequestDto;
    private TransactionEntity transactionEntity;
    private CardEntity senderCard;
    private CardEntity recipientCard;

    @BeforeEach
    void setUp() {
        transactionRequestDto = new TransactionRequestDto(
                1L,
                2L,
                new BigDecimal("20.00"),
                "1a2b3c",
                1111
        );

        transactionEntity = new TransactionEntity();
        transactionEntity.setId(1L);
        transactionEntity.setSenderId(1L);
        transactionEntity.setRecipientId(2L);
        transactionEntity.setAmount(new BigDecimal("20.00"));
        transactionEntity.setRideId(transactionRequestDto.rideId());
        transactionEntity.setTransactionType(TransactionType.RIDE_PAYMENT);
        transactionEntity.setStatus(TransactionStatus.PENDING);

        senderCard = new CardEntity();
        senderCard.setId(1L);
        senderCard.setOwnerId(1L);
        senderCard.setRole(Role.PASSENGER);
        senderCard.setBalance(new BigDecimal("2000.00"));
        senderCard.setPassword(1111);

        recipientCard = new CardEntity();
        recipientCard.setId(2L);
        recipientCard.setOwnerId(2L);
        recipientCard.setRole(Role.DRIVER);
        recipientCard.setBalance(new BigDecimal("5000.00"));
    }

    @Test
    void createTransaction_Success() {
        when(transactionMapper.toEntity(transactionRequestDto)).thenReturn(transactionEntity);
        when(cardRepo.findByOwnerIdAndRole(1L, Role.PASSENGER)).thenReturn(Optional.of(senderCard));
        when(cardRepo.findByOwnerIdAndRole(2L, Role.DRIVER)).thenReturn(Optional.of(recipientCard));

        transactionService.createTransaction(TransactionType.RIDE_PAYMENT, transactionRequestDto);

        verify(transactionRepo, times(2)).save(transactionEntity);
        verify(cardRepo, times(1)).save(senderCard);
        verify(cardRepo, times(1)).save(recipientCard);
        verify(rideServiceClient, times(1)).payRide("1a2b3c", new BigDecimal("20.00"));
    }

    @Test
    void createTransaction_checkBalances_Success() {
        when(transactionMapper.toEntity(transactionRequestDto)).thenReturn(transactionEntity);
        when(cardRepo.findByOwnerIdAndRole(1L, Role.PASSENGER)).thenReturn(Optional.of(senderCard));
        when(cardRepo.findByOwnerIdAndRole(2L, Role.DRIVER)).thenReturn(Optional.of(recipientCard));

        transactionService.createTransaction(TransactionType.RIDE_PAYMENT, transactionRequestDto);

        // Проверяем, что транзакция сохраняется дважды
        verify(transactionRepo, times(2)).save(transactionEntity);

        // Проверяем, что средства списываются с карты отправителя
        BigDecimal expectedSenderBalance = new BigDecimal("2000.00").subtract(new BigDecimal("20.00"));
        assertEquals(expectedSenderBalance, senderCard.getBalance());
        verify(cardRepo, times(1)).save(senderCard);

        // Проверяем, что средства зачисляются на карту получателя
        BigDecimal expectedRecipientBalance = new BigDecimal("5000.00").add(new BigDecimal("20.00"));
        assertEquals(expectedRecipientBalance, recipientCard.getBalance());
        verify(cardRepo, times(1)).save(recipientCard);

        // Проверяем, что вызывается метод payRide
        verify(rideServiceClient, times(1)).payRide("1a2b3c", new BigDecimal("20.00"));
    }

    @Test
    void createTransaction_SenderCardNotFound() {
        when(transactionMapper.toEntity(transactionRequestDto)).thenReturn(transactionEntity);
        when(cardRepo.findByOwnerIdAndRole(1L, Role.PASSENGER)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            transactionService.createTransaction(TransactionType.RIDE_PAYMENT, transactionRequestDto);
        });

        verify(transactionRepo, times(2)).save(transactionEntity);
        assertEquals(TransactionStatus.FAILED, transactionEntity.getStatus());
    }

    @Test
    void createTransaction_RecipientCardNotFound() {
        when(transactionMapper.toEntity(transactionRequestDto)).thenReturn(transactionEntity);
        when(cardRepo.findByOwnerIdAndRole(1L, Role.PASSENGER)).thenReturn(Optional.of(senderCard));
        when(cardRepo.findByOwnerIdAndRole(2L, Role.DRIVER)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            transactionService.createTransaction(TransactionType.RIDE_PAYMENT, transactionRequestDto);
        });

        verify(transactionRepo, times(2)).save(transactionEntity);
        assertEquals(TransactionStatus.FAILED, transactionEntity.getStatus());
    }

    @Test
    void createTransaction_InvalidPassword() {
        when(transactionMapper.toEntity(any(TransactionRequestDto.class))).thenReturn(transactionEntity);
        when(cardRepo.findByOwnerIdAndRole(1L, Role.PASSENGER)).thenReturn(Optional.of(senderCard));
        when(cardRepo.findByOwnerIdAndRole(2L, Role.DRIVER)).thenReturn(Optional.of(recipientCard));

        TransactionRequestDto dto = new TransactionRequestDto(
                1L, 2L, new BigDecimal("20.00"), "1a2b3c", 1221321 // Неправильный пароль
        );

        assertThrows(InvalidPasswordException.class, () -> {
            transactionService.createTransaction(TransactionType.RIDE_PAYMENT, dto);
        });

        verify(transactionRepo, times(2)).save(transactionEntity);
        assertEquals(TransactionStatus.FAILED, transactionEntity.getStatus());
    }

    @Test
    void createTransaction_BalanceTooLow() {
        senderCard.setBalance(new BigDecimal("0.00"));

        when(transactionMapper.toEntity(transactionRequestDto)).thenReturn(transactionEntity);
        when(cardRepo.findByOwnerIdAndRole(1L, Role.PASSENGER)).thenReturn(Optional.of(senderCard));
        when(cardRepo.findByOwnerIdAndRole(2L, Role.DRIVER)).thenReturn(Optional.of(recipientCard));

        assertThrows(BalanceTooLowException.class, () -> {
            transactionService.createTransaction(TransactionType.RIDE_PAYMENT, transactionRequestDto);
        });

        verify(transactionRepo, times(2)).save(transactionEntity);
        assertEquals(TransactionStatus.FAILED, transactionEntity.getStatus());
    }

    @Test
    void findAll() {
        when(transactionRepo.findAll()).thenReturn(Collections.singletonList(transactionEntity));

        List<TransactionEntity> transactions = transactionService.findAll();

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals(transactionEntity, transactions.get(0));
    }

    @Test
    void findAllTransactionsByStatus() {
        when(transactionRepo.findAllByStatus(TransactionStatus.COMPLETED)).thenReturn(Collections.singletonList(transactionEntity));

        List<TransactionEntity> transactions = transactionService.findAllTransactionsByStatus(TransactionStatus.COMPLETED);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals(transactionEntity, transactions.get(0));
    }

    @Test
    void findAllByTransactionType() {
        when(transactionRepo.findAllByTransactionType(TransactionType.RIDE_PAYMENT)).thenReturn(Collections.singletonList(transactionEntity));

        List<TransactionEntity> transactions = transactionService.findAllByTransactionType(TransactionType.RIDE_PAYMENT);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals(transactionEntity, transactions.get(0));
    }

    @Test
    void dropTransaction() {
        doNothing().when(transactionRepo).deleteById(1L);

        transactionService.dropTransaction(1L);

        verify(transactionRepo, times(1)).deleteById(1L);
    }
}