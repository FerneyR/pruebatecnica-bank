package com.bank.transaction.controller;

import com.bank.transaction.dto.AnulationRequestDTO;
import com.bank.transaction.dto.TransactionDetailsDTO;
import com.bank.transaction.dto.TransactionRequestDTO;
import com.bank.transaction.dto.TransactionResponseDTO;
import com.bank.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<TransactionResponseDTO> purchase(@RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO response = transactionService.purchase(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDetailsDTO> getTransaction(@PathVariable Long transactionId) {
        TransactionDetailsDTO response = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/anulation")
    public ResponseEntity<Void> cancelTransaction(@RequestBody AnulationRequestDTO request) {
        transactionService.cancelTransaction(request);
        return ResponseEntity.ok().build();
    }
}