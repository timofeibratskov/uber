package com.example.payment_service.controller;

import com.example.payment_service.dto.CardRequestDto;
import com.example.payment_service.dto.CardResponseDto;
import com.example.payment_service.dto.UpdateCardPasswordDto;
import com.example.payment_service.enums.Role;
import com.example.payment_service.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;

    @PostMapping("/{role}/{ownerId}/create")
    public Long createCard(@RequestBody CardRequestDto cardRequestDto
    , @PathVariable Role role,@PathVariable Long ownerId) {
        return cardService.createCard(ownerId,role,cardRequestDto);
    }

    @GetMapping("/find/{id}")
    public CardResponseDto findCard(@PathVariable Long id) {
        return cardService.findCardById(id);
    }

    @GetMapping("{id}/getBalance")
    public BigDecimal getBalance(@PathVariable Long id,
                                 @RequestParam Integer password
    ) {
        return cardService.getBalance(id, password);
    }

    @PatchMapping("/{id}/updatePassword")
    public String updateCardPassword(@PathVariable Long id,@RequestBody
                                     UpdateCardPasswordDto updateCardPasswordDto) {
        return cardService.updateCardPassword(id, updateCardPasswordDto);
    }

    @DeleteMapping("{id}/delete")
    public void deleteCard(@PathVariable Long id,
                           @RequestParam Integer password) {
        cardService.deleteCard(id, password);
    }

}
