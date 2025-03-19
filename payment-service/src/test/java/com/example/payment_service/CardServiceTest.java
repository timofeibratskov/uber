package com.example.payment_service;

import com.example.payment_service.dto.CardRequestDto;
import com.example.payment_service.dto.CardResponseDto;
import com.example.payment_service.dto.UpdateCardPasswordDto;
import com.example.payment_service.entity.CardEntity;
import com.example.payment_service.enums.Role;
import com.example.payment_service.exception.CardNumberAlreadyExistsException;
import com.example.payment_service.exception.InvalidPasswordException;
import com.example.payment_service.exception.NotFoundException;
import com.example.payment_service.mapper.CardMapper;
import com.example.payment_service.repo.CardRepo;
import com.example.payment_service.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepo cardRepo;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private CardEntity card;
    private CardResponseDto cardResponseDto;

    @BeforeEach
    void setUp() {
        cardResponseDto = new CardResponseDto(
                1L,
                "1111-1111-1111-1111",
                new BigDecimal("1111.00"),
                1111,
                Role.PASSENGER,
                1L
        );

        card = new CardEntity();
        card.setId(1L);
        card.setOwnerId(1L);
        card.setCardNumber("1111-1111-1111-1111");
        card.setPassword(1111);
        card.setRole(Role.PASSENGER);
        card.setBalance(new BigDecimal("1111.00"));
    }

    @Test
    void findCardByCardId_Success() {
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardResponseDto);

        CardResponseDto result = cardService.findCardByCardId(1L);

        assertEquals(cardResponseDto, result);
        verify(cardRepo).findById(1L);
    }

    @Test
    void findCardById_NotFound() {
        when(cardRepo.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> cardService.findCardByCardId(1L)
        );

        assertEquals("такой карты не существует", exception.getMessage());
    }

    @Test
    void createCard_Success() {
        CardRequestDto requestDto = new CardRequestDto(
                "1111-1111-1111-1111",
                new BigDecimal("100.00"),
                1111
        );

        when(cardRepo.existsByCardNumber(requestDto.cardNumber())).thenReturn(false);
        when(cardMapper.toEntity(requestDto)).thenReturn(card);
        when(cardRepo.save(card)).thenReturn(card);

        Long resultId = cardService.createCard(1L, Role.PASSENGER, requestDto);

        assertEquals(1L, resultId);
        verify(cardRepo).existsByCardNumber(requestDto.cardNumber());
        verify(cardRepo).save(card);
    }

    @Test
    void createCard_CardNumberAlreadyExists() {
        CardRequestDto requestDto = new CardRequestDto(
                "1111-1111-1111-1111",
                new BigDecimal("100.00"),
                1111
        );

        when(cardRepo.existsByCardNumber(requestDto.cardNumber())).thenReturn(true);

        CardNumberAlreadyExistsException exception = assertThrows(
                CardNumberAlreadyExistsException.class,
                () -> cardService.createCard(1L, Role.PASSENGER, requestDto)
        );

        assertEquals("Номер карты уже существует", exception.getMessage());
        verify(cardRepo).existsByCardNumber(requestDto.cardNumber());
    }

    @Test
    void findCardByOwnerIdAndRole_Success() {
        when(cardRepo.findByOwnerIdAndRole(1L, Role.PASSENGER)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardResponseDto);

        CardResponseDto result = cardService.findCardByOwnerIdAndRole(1L, Role.PASSENGER);

        assertEquals(cardResponseDto, result);
        verify(cardRepo).findByOwnerIdAndRole(1L, Role.PASSENGER);
    }

    @Test
    void findCardByOwnerIdAndRole_NotFound() {
        when(cardRepo.findByOwnerIdAndRole(1L, Role.PASSENGER)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> cardService.findCardByOwnerIdAndRole(1L, Role.PASSENGER)
        );

        assertEquals("у пользователя 1 с ролью PASSENGER нет карты!", exception.getMessage());
        verify(cardRepo).findByOwnerIdAndRole(1L, Role.PASSENGER);
    }

    @Test
    void getBalance_Success() {
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        BigDecimal result = cardService.getBalance(1L, 1111);

        assertEquals(new BigDecimal("1111.00"), result);
        verify(cardRepo).findById(1L);
    }

    @Test
    void getBalance_InvalidPassword() {
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> cardService.getBalance(1L, 1234)
        );

        assertEquals("неверный пароль", exception.getMessage());
        verify(cardRepo).findById(1L);
    }

    @Test
    void updateCardPassword_Success() {
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        UpdateCardPasswordDto updateDto = new UpdateCardPasswordDto(1111, 2222);
        String result = cardService.updateCardPassword(1L, updateDto);

        assertEquals("Пароль изменен успешно!", result);
        assertEquals(2222, card.getPassword());
        verify(cardRepo).save(card);
    }

    @Test
    void updateCardPassword_InvalidPassword() {
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        UpdateCardPasswordDto updateDto = new UpdateCardPasswordDto(1234, 2222);

        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> cardService.updateCardPassword(1L, updateDto)
        );

        assertEquals("неверный пароль", exception.getMessage());
        verify(cardRepo).findById(1L);
    }

    @Test
    void deleteCard_Success() {
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard(1L, 1111);

        verify(cardRepo).delete(card);
    }

    @Test
    void deleteCard_InvalidPassword() {
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> cardService.deleteCard(1L, 1234)
        );

        assertEquals("неверный пароль", exception.getMessage());
        verify(cardRepo).findById(1L);
    }
}