package com.bank.transaction.service;

import com.bank.transaction.dto.AnulationRequestDTO;
import com.bank.transaction.dto.TransactionDetailsDTO;
import com.bank.transaction.dto.TransactionRequestDTO;
import com.bank.transaction.dto.TransactionResponseDTO;

public interface TransactionService {

    TransactionResponseDTO purchase(TransactionRequestDTO request);

    TransactionDetailsDTO getTransaction(Long transactionId);

    void cancelTransaction(AnulationRequestDTO request);
}