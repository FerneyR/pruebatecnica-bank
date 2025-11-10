package com.bank.transaction.service.impl;

import com.bank.transaction.dto.AnulationRequestDTO;
import com.bank.transaction.dto.TransactionDetailsDTO;
import com.bank.transaction.dto.TransactionRequestDTO;
import com.bank.transaction.dto.TransactionResponseDTO;
import com.bank.transaction.exception.TransactionException;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;
    @Value("${card.service.url:http://localhost:8081}")
    private String CARD_SERVICE_URL_BASE;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, RestTemplate restTemplate) {
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public TransactionResponseDTO purchase(TransactionRequestDTO request) {
        String url = CARD_SERVICE_URL_BASE + "/card/internal/debit";

        try {
            restTemplate.postForObject(url, request, Void.class);
            
        } catch (Exception e) {
            throw new TransactionException("Error al validar tarjeta: " + e.getMessage());
        }
        Transaction tx = Transaction.builder()
            .cardId(request.getCardId())
            .price(request.getPrice())
            .transactionDate(LocalDateTime.now())
            .isAnnulled(false)
            .build();

        Transaction savedTx = transactionRepository.save(tx);
        return new TransactionResponseDTO(
                savedTx.getTransactionId(),
                savedTx.getCardId(),
                savedTx.getPrice(),
                savedTx.getTransactionDate()
        );
    }

    @Override
    @Transactional
    public void cancelTransaction(AnulationRequestDTO request) {
        
        Transaction tx = transactionRepository.findByTransactionIdAndCardId(request.getTransactionId(), request.getCardId())
                .orElseThrow(() -> new TransactionException("Transacción no encontrada (not found) o no pertenece a la tarjeta."));

        if (tx.isAnnulled()) {
            throw new TransactionException("La transacción ya fue anulada previamente.");
        }
        Duration duration = Duration.between(tx.getTransactionDate(), LocalDateTime.now());
        
        if (duration.toHours() >= 24) {
            throw new TransactionException("No se puede anular, han pasado más de 24 horas.");
        }

        TransactionRequestDTO reversalRequest = new TransactionRequestDTO();
        reversalRequest.setCardId(tx.getCardId());
        reversalRequest.setPrice(tx.getPrice());
        
        String url = CARD_SERVICE_URL_BASE + "/card/internal/reversal";
        
        try {
            restTemplate.postForObject(url, reversalRequest, Void.class);
        } catch (Exception e) {

            throw new TransactionException("Error al reversar saldo en tarjeta: " + e.getMessage());
        }
        tx.setAnnulled(true);
        transactionRepository.save(tx);
    }

    @Override
    public TransactionDetailsDTO getTransaction(Long transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException("Transacción no encontrada (not found) con id: " + transactionId));
                return TransactionDetailsDTO.fromEntity(tx);
    }
}