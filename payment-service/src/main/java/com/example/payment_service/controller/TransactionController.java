package com.example.payment_service.controller;

import com.example.payment_service.dto.TransactionRequestDto;
import com.example.payment_service.entity.TransactionEntity;
import com.example.payment_service.enums.TransactionStatus;
import com.example.payment_service.enums.TransactionType;
import com.example.payment_service.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/{type}")
    public void newTransaction(@PathVariable TransactionType type,
                                            @RequestBody @Valid TransactionRequestDto dto) {
       transactionService.createTransaction(type, dto);
    }

    @GetMapping("/all")
    public List<TransactionEntity> findAll() {
        return transactionService.findAll();
    }
    @DeleteMapping("/del/{id}")
    public void delete(@PathVariable Long id) {
        transactionService.dropTransaction(id);
    }

    @GetMapping("/all/status/{status}")
    public List<TransactionEntity> findAllByStatus(@PathVariable TransactionStatus status) {
        return transactionService.findAllTransactionsByStatus(status);
    }

    @GetMapping("/all/type/{type}")
    public List<TransactionEntity> findAllByType(@PathVariable TransactionType type) {
        return transactionService.findAllByTransactionType(type);
    }
}
