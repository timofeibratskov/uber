package com.example.payment_service.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepo cardRepo;
    private final CardMapper mapper;

    public CardResponseDto createCard(Long ownerId, Role role, CardRequestDto cardRequestDto) {

        if (cardRepo.existsByCardNumber(cardRequestDto.cardNumber())) {
            throw new CardNumberAlreadyExistsException("Номер карты уже существует");
        }

        CardEntity cardEntity = mapper.toEntity(cardRequestDto);
        cardEntity.setRole(role);
        cardEntity.setOwnerId(ownerId);

         cardRepo.save(cardEntity);

         return mapper.toDto(cardEntity);
    }

    public CardResponseDto findCardByCardId(Long id) {
        CardEntity card = cardRepo.findById(id).orElseThrow(() ->
                new NotFoundException("такой карты не существует"));
        return mapper.toDto(card);
    }

    public CardResponseDto findCardByOwnerIdAndRole(Long ownerId, Role role) {
        CardEntity card = cardRepo.findByOwnerIdAndRole(ownerId, role).orElseThrow(()
                -> new NotFoundException("у пользователя " + ownerId + " с ролью " + role + " нет карты!"));
        return mapper.toDto(card);
    }

    public BigDecimal getBalance(Long id, Integer password) {
        CardEntity card = cardRepo.findById(id).orElseThrow(() ->
                new NotFoundException("такой карты не существует"));

        if (card.getPassword().equals(password)) return card.getBalance();
        else {
            throw new InvalidPasswordException("неверный пароль");
        }
    }

    public String updateCardPassword(Long id, UpdateCardPasswordDto updateCardPasswordDto) {
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

