package com.bank.transaction.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.bank.transaction.dto.AnulationRequestDTO;
import com.bank.transaction.dto.TransactionDetailsDTO;
import com.bank.transaction.dto.TransactionRequestDTO;
import com.bank.transaction.dto.TransactionResponseDTO;
import com.bank.transaction.exception.TransactionException;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transactionService, "CARD_SERVICE_URL_BASE", "http://fake-card-service");
    }


@Test
    void testPurchase_Success() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId("123456");
        request.setPrice(new BigDecimal("100"));

        String expectedUrl = "http://fake-card-service/card/internal/debit";

        when(restTemplate.postForObject(eq(expectedUrl), any(TransactionRequestDTO.class), eq(Void.class)))
            .thenReturn(null);
        
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction tx = inv.getArgument(0);
            tx.setTransactionId(1L); 
            return tx;
        });

        TransactionResponseDTO response = transactionService.purchase(request);

        assertNotNull(response);
        assertEquals(1L, response.getTransactionId());
        verify(restTemplate, times(1)).postForObject(eq(expectedUrl), any(TransactionRequestDTO.class), eq(Void.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testPurchase_CardServiceFails() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId("123456");
        request.setPrice(new BigDecimal("100"));
        
        when(restTemplate.postForObject(anyString(), any(), eq(Void.class)))
            .thenThrow(new RuntimeException("Saldo insuficiente (simulado)"));

        assertThrows(TransactionException.class, () -> {
            transactionService.purchase(request);
        });

        verify(transactionRepository, never()).save(any(Transaction.class));
    }


 @Test
    void testCancelTransaction_Success() {
        AnulationRequestDTO request = new AnulationRequestDTO();
        request.setCardId("123456");
        request.setTransactionId(1L);

        Transaction mockTx = new Transaction();
        mockTx.setTransactionId(1L);
        mockTx.setCardId("123456");
        mockTx.setPrice(new BigDecimal("100"));
        mockTx.setAnnulled(false);
        mockTx.setTransactionDate(LocalDateTime.now().minusHours(1)); 

        when(transactionRepository.findByTransactionIdAndCardId(1L, "123456"))
            .thenReturn(Optional.of(mockTx));
        
        when(restTemplate.postForObject(anyString(), any(TransactionRequestDTO.class), eq(Void.class)))
            .thenReturn(null);

        transactionService.cancelTransaction(request);

        verify(restTemplate, times(1)).postForObject(eq("http://fake-card-service/card/internal/reversal"), any(TransactionRequestDTO.class), eq(Void.class));
        verify(transactionRepository, times(1)).save(mockTx);
        assertTrue(mockTx.isAnnulled());
    }

    @Test
    void testCancelTransaction_NotFound() {
        AnulationRequestDTO request = new AnulationRequestDTO();
        request.setCardId("123456");
        request.setTransactionId(1L);
        
        when(transactionRepository.findByTransactionIdAndCardId(1L, "123456"))
            .thenReturn(Optional.empty());

        assertThrows(TransactionException.class, () -> {
            transactionService.cancelTransaction(request);
        });
    }

 @Test
    void testCancelTransaction_AlreadyAnnulled() {
        AnulationRequestDTO request = new AnulationRequestDTO();
        request.setCardId("123456");
        request.setTransactionId(1L);
        
        Transaction mockTx = new Transaction();
        mockTx.setAnnulled(true);

        when(transactionRepository.findByTransactionIdAndCardId(1L, "123456"))
            .thenReturn(Optional.of(mockTx));

        assertThrows(TransactionException.class, () -> {
            transactionService.cancelTransaction(request);
        });
    }

@Test
    void testCancelTransaction_TooLate() {
        AnulationRequestDTO request = new AnulationRequestDTO();
        request.setCardId("123456");
        request.setTransactionId(1L);
        
        Transaction mockTx = new Transaction();
        mockTx.setAnnulled(false);
        mockTx.setTransactionDate(LocalDateTime.now().minusHours(25));

        when(transactionRepository.findByTransactionIdAndCardId(1L, "123456"))
            .thenReturn(Optional.of(mockTx));

        assertThrows(TransactionException.class, () -> {
            transactionService.cancelTransaction(request);
        });
    }

@Test
    void testCancelTransaction_ReversalFails() {
        AnulationRequestDTO request = new AnulationRequestDTO();
        request.setCardId("123456");
        request.setTransactionId(1L);
        
        Transaction mockTx = new Transaction();
        mockTx.setAnnulled(false);
        mockTx.setTransactionDate(LocalDateTime.now().minusHours(1));

        when(transactionRepository.findByTransactionIdAndCardId(1L, "123456"))
            .thenReturn(Optional.of(mockTx));
        
        when(restTemplate.postForObject(anyString(), any(), eq(Void.class)))
            .thenThrow(new RuntimeException("Error de red simulado"));
        
        assertThrows(TransactionException.class, () -> {
            transactionService.cancelTransaction(request);
        });
        
        verify(transactionRepository, never()).save(mockTx);
        assertFalse(mockTx.isAnnulled());
    }


    @Test
    void testGetTransaction_Success() {
        Transaction mockTx = new Transaction();
        mockTx.setTransactionId(1L);
        mockTx.setCardId("123456");
        mockTx.setPrice(new BigDecimal("100"));
        mockTx.setAnnulled(false);
        mockTx.setTransactionDate(LocalDateTime.now());

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(mockTx));

        TransactionDetailsDTO response = transactionService.getTransaction(1L);

        assertNotNull(response);
        assertEquals(1L, response.getTransactionId());
        assertEquals(false, response.isAnnulled());
    }

    @Test
    void testGetTransaction_NotFound() {
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(TransactionException.class, () -> {
            transactionService.getTransaction(1L);
        });
    }
}