package com.example.payment_service.controller;

import com.example.payment_service.dto.TransactionRequestDto;
import com.example.payment_service.entity.TransactionEntity;
import com.example.payment_service.enums.TransactionStatus;
import com.example.payment_service.enums.TransactionType;
import com.example.payment_service.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transaction")
@Tag(name = "Transaction Management", description = "APIs for managing transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/{type}")
    @Operation(
            summary = "Create a new transaction",
            description = "Create a new transaction of a specific type",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input provided")
            }
    )
    public void newTransaction(
            @Parameter(description = "Type of the transaction", required = true, example = "REFUND")
            @PathVariable TransactionType type,
            @RequestBody @Valid TransactionRequestDto dto
    ) {
        transactionService.createTransaction(type, dto);
    }

    @GetMapping("/all")
    @Operation(
            summary = "Get all transactions",
            description = "Retrieve a list of all transactions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
            }
    )
    public List<TransactionEntity> findAll() {
        return transactionService.findAll();
    }

    @DeleteMapping("/del/{id}")
    @Operation(
            summary = "Delete a transaction",
            description = "Delete a transaction by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Transaction not found")
            }
    )
    public void delete(
            @Parameter(description = "ID of the transaction", required = true, example = "1")
            @PathVariable Long id
    ) {
        transactionService.dropTransaction(id);
    }

    @GetMapping("/all/status/{status}")
    @Operation(
            summary = "Get transactions by status",
            description = "Retrieve a list of transactions by their status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
            }
    )
    public List<TransactionEntity> findAllByStatus(
            @Parameter(description = "Status of the transactions", required = true, example = "COMPLETED")
            @PathVariable TransactionStatus status
    ) {
        return transactionService.findAllTransactionsByStatus(status);
    }

    @GetMapping("/all/type/{type}")
    @Operation(
            summary = "Get transactions by type",
            description = "Retrieve a list of transactions by their type",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
            }
    )
    public List<TransactionEntity> findAllByType(
            @Parameter(description = "Type of the transactions", required = true)
            @PathVariable TransactionType type
    ) {
        return transactionService.findAllByTransactionType(type);
    }
}