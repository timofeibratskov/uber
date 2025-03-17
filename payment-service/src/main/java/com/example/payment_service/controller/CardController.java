package com.example.payment_service.controller;

import com.example.payment_service.dto.CardRequestDto;
import com.example.payment_service.dto.CardResponseDto;
import com.example.payment_service.dto.UpdateCardPasswordDto;
import com.example.payment_service.enums.Role;
import com.example.payment_service.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
@Tag(name = "Card Management", description = "APIs for managing payment cards")
public class CardController {
    private final CardService cardService;

    @PostMapping("/{role}/{ownerId}/create")
    @Operation(
            summary = "Create a new card",
            description = "Create a new payment card for a user with a specific role",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input provided")
            }
    )
    public Long createCard(
            @RequestBody CardRequestDto cardRequestDto,
            @Parameter(description = "Role of the card owner", required = true, example = "DRIVER")
            @PathVariable Role role,
            @Parameter(description = "ID of the card owner", required = true, example = "1")
            @PathVariable Long ownerId
    ) {
        return cardService.createCard(ownerId, role, cardRequestDto);
    }

    @GetMapping("/find/{id}")
    @Operation(
            summary = "Find card by ID",
            description = "Retrieve card details by its unique ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card details retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Card not found")
            }
    )
    public CardResponseDto findCard(
            @Parameter(description = "ID of the card", required = true, example = "1")
            @PathVariable Long id
    ) {
        return cardService.findCardByCardId(id);
    }

    @GetMapping("/find/{role}/{id}")
    @Operation(
            summary = "Find card by ID and userRole",
            description = "Retrieve card details by its unique ID and userRole",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card details retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Card not found")
            }
    )
    public CardResponseDto findCardByOwner(
            @PathVariable Role role, @PathVariable Long id
    ) {
        return cardService.findCardByOwnerIdAndRole(id, role);
    }

    @GetMapping("{id}/getBalance")
    @Operation(
            summary = "Get card balance",
            description = "Retrieve the balance of a card by its ID and password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid password or card ID")
            }
    )
    public BigDecimal getBalance(
            @Parameter(description = "ID of the card", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Card password", required = true, example = "1234")
            @RequestParam Integer password
    ) {
        return cardService.getBalance(id, password);
    }

    @PatchMapping("/{id}/updatePassword")
    @Operation(
            summary = "Update card password",
            description = "Update the password of a card by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input provided")
            }
    )
    public String updateCardPassword(
            @Parameter(description = "ID of the card", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody UpdateCardPasswordDto updateCardPasswordDto
    ) {
        return cardService.updateCardPassword(id, updateCardPasswordDto);
    }

    @DeleteMapping("{id}/delete")
    @Operation(
            summary = "Delete a card",
            description = "Delete a card by its ID and password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid password or card ID")
            }
    )
    public void deleteCard(
            @Parameter(description = "ID of the card", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Card password", required = true, example = "1234")
            @RequestParam Integer password
    ) {
        cardService.deleteCard(id, password);
    }
}