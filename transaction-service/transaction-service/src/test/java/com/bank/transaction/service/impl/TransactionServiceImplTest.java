package com.bank.transaction.service.impl;

import com.bank.transaction.dto.TransactionRequestDTO;
import com.bank.transaction.dto.TransactionResponseDTO;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void testPurchase_CuandoTarjetaTieneSaldo_DebeCrearTransaccion() {
                
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId("123");
        request.setPrice(new BigDecimal("100"));

        Transaction txGuardada = new Transaction(1L, "123", new BigDecimal("100"), LocalDateTime.now(), false);
        
        String urlEsperada = "http://localhost:8081/card/internal/debit";

        when(restTemplate.postForObject(eq(urlEsperada), eq(request), eq(Void.class)))
            .thenReturn(null);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(txGuardada);

        
        TransactionResponseDTO response = transactionService.purchase(request);

                
        assertNotNull(response);
        assertEquals(1L, response.getTransactionId());
        
        verify(restTemplate, times(1)).postForObject(eq(urlEsperada), eq(request), eq(Void.class));
        
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}