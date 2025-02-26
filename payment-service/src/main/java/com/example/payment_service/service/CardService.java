package com.example.payment_service.service;

import com.example.payment_service.dto.CardRequestDto;
import com.example.payment_service.dto.CardResponseDto;
import com.example.payment_service.dto.UpdateCardPasswordDto;
import com.example.payment_service.entity.CardEntity;
import com.example.payment_service.exception.CardNumberAlreadyExistsException;
import com.example.payment_service.exception.InvalidPasswordException;
import com.example.payment_service.exception.NotFoundException;
import com.example.payment_service.repo.CardRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepo cardRepo;

    public Long createCard(CardRequestDto cardRequestDto) {
        if (cardRepo.existsByCardNumber(cardRequestDto.cardNumber())) {
            throw new CardNumberAlreadyExistsException("Номер карты уже существует");
        }

        CardEntity cardEntity = CardEntity.builder()
                .cardNumber(cardRequestDto.cardNumber())
                .balance(cardRequestDto.balance())
                .password(cardRequestDto.password()).build();
        return cardRepo.save(cardEntity).getId();
    }

    public CardResponseDto findCardById(Long id) {
        CardEntity card = cardRepo.findById(id).orElseThrow(() ->
                new NotFoundException("такой карты не существует"));
        return CardResponseDto.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .balance(card.getBalance())
                .password(card.getPassword()).build();
    }

    public BigDecimal getBalance(Long id, Integer password) {
        CardEntity card = cardRepo.findById(id).orElseThrow(() ->
                new NotFoundException("такой карты не существует"));

        if (card.getPassword().equals(password)) return card.getBalance();
        else {
            throw new InvalidPasswordException("неверный пароль");
        }
    }

    public String updateCardPassword(Long id,UpdateCardPasswordDto updateCardPasswordDto) {
        CardEntity card = cardRepo.findById(id).orElseThrow(() ->
                new NotFoundException("такой карты не существует"));
        if (card.getPassword().equals(updateCardPasswordDto.password())) {
            card.setPassword(updateCardPasswordDto.newPassword());
            cardRepo.save(card);
            return "Пароль изменен успешно!";
        } else {
            throw new InvalidPasswordException("неверный пароль");
        }
    }

    public void deleteCard(Long id, Integer password) {
        CardEntity card = cardRepo.findById(id).orElseThrow(() ->
                new NotFoundException("такой карты не существует"));

        if (card.getPassword().equals(password)) {
            cardRepo.delete(card);
        } else {
            throw new InvalidPasswordException("неверный пароль");
        }
    }
}

