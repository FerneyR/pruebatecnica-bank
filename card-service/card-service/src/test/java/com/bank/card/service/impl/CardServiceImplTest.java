package com.bank.card.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bank.card.dto.CardBalanceRequestDTO;
import com.bank.card.dto.CardBalanceResponseDTO;
import com.bank.card.dto.CardEnrollRequestDTO;
import com.bank.card.dto.TransactionRequestDTO;
import com.bank.card.exception.CardException;
import com.bank.card.model.Card;
import com.bank.card.repository.CardRepository;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;


    @Test
    void testGenerarNumeroTarjeta_Success() {
        String productId = "123456";
        
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String numeroTarjeta = cardService.generarNumeroTarjeta(productId);

        assertNotNull(numeroTarjeta);
        assertEquals(16, numeroTarjeta.length());
        assertTrue(numeroTarjeta.startsWith(productId));
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void testGenerarNumeroTarjeta_InvalidProductId() {
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.generarNumeroTarjeta("123");
        });
    }


    @Test
    void testActivarTarjeta_Success() {
        String cardId = "1234567890";
        Card mockCard = new Card();
        mockCard.setCardId(cardId);
        mockCard.setActive(false);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));

        CardEnrollRequestDTO request = new CardEnrollRequestDTO();
        request.setCardId(cardId);

        cardService.activarTarjeta(request);

        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).save(any(Card.class));
        assertTrue(mockCard.isActive());
    }

    @Test
    void testActivarTarjeta_CardNotFound() {
        String cardId = "non-existent";
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        CardEnrollRequestDTO request = new CardEnrollRequestDTO();
        request.setCardId(cardId);
        assertThrows(CardException.class, () -> {
            cardService.activarTarjeta(request);
        });
    }

    @Test
    void testActivarTarjeta_AlreadyActive() {
        String cardId = "1234567890";
        Card mockCard = new Card();
        mockCard.setCardId(cardId);
        mockCard.setActive(true);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));

        CardEnrollRequestDTO request = new CardEnrollRequestDTO();
        request.setCardId(cardId);

        assertThrows(CardException.class, () -> {
            cardService.activarTarjeta(request);
        });

        verify(cardRepository, never()).save(any(Card.class));
    }
    
    
    @Test
    void testBloquearTarjeta_Success() {
        String cardId = "123456";
        Card mockCard = new Card();
        mockCard.setBlocked(false);
        
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        
        cardService.bloquearTarjeta(cardId);
        
        assertTrue(mockCard.isBlocked());
        verify(cardRepository, times(1)).save(mockCard);
    }
    
    @Test
    void testBloquearTarjeta_AlreadyBlocked() {
        String cardId = "123456";
        Card mockCard = new Card();
        mockCard.setBlocked(true);
        
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        
        assertThrows(CardException.class, () -> {
            cardService.bloquearTarjeta(cardId);
        });
        
        verify(cardRepository, never()).save(any(Card.class));
    }


    @Test
    void testRecargarSaldo_Success() {
        String cardId = "123456";
        Card mockCard = new Card();
        mockCard.setCardId(cardId);
        mockCard.setBalance(new BigDecimal("100"));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class))).thenReturn(mockCard);

        CardBalanceRequestDTO request = new CardBalanceRequestDTO();
        request.setCardId(cardId);
        request.setBalance(new BigDecimal("500"));

        CardBalanceResponseDTO response = cardService.recargarSaldo(request);

        assertEquals(new BigDecimal("600"), mockCard.getBalance());
        assertEquals(new BigDecimal("600"), response.getNewBalance());
        verify(cardRepository, times(1)).save(mockCard);
    }
    
    @Test
    void testRecargarSaldo_InvalidAmount() {
        CardBalanceRequestDTO request = new CardBalanceRequestDTO();
        request.setCardId("123456");
        request.setBalance(BigDecimal.ZERO);

        Card mockCard = new Card(); 
        when(cardRepository.findById("123456")).thenReturn(Optional.of(mockCard));

        assertThrows(IllegalArgumentException.class, () -> {
            cardService.recargarSaldo(request);
        });
        
        verify(cardRepository, never()).save(any(Card.class));
    }


    @Test
    void testConsultarSaldo_Success() {
        String cardId = "123456";
        Card mockCard = new Card();
        mockCard.setBalance(new BigDecimal("1000"));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));

        BigDecimal balance = cardService.consultarSaldo(cardId);

        assertEquals(new BigDecimal("1000"), balance);
        verify(cardRepository, times(1)).findById(cardId);
    }
    
    
    @Test
    void testGetCardById_NotFound() {
        when(cardRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(CardException.class, () -> {
            cardService.getCardById("non-existent");
        });
    }

    
    @Test
    void testGetCardActiveAndValid_CardNotActive() {
        String cardId = "123456";
        Card mockCard = new Card();
        mockCard.setActive(false);
        
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        
        assertThrows(CardException.class, () -> {
            cardService.getCardActiveAndValid(cardId);
        });
    }
    
    @Test
    void testGetCardActiveAndValid_CardIsBlocked() {
        String cardId = "123456";
        Card mockCard = new Card();
        mockCard.setActive(true);
        mockCard.setBlocked(true);
        
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        
        assertThrows(CardException.class, () -> {
            cardService.getCardActiveAndValid(cardId);
        });
    }
    
    @Test
    void testGetCardActiveAndValid_CardIsExpired() {
        String cardId = "123456";
        Card mockCard = new Card();
        mockCard.setActive(true);
        mockCard.setBlocked(false);
        mockCard.setExpiryDate(LocalDate.now().minusDays(1));
        
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        
        assertThrows(CardException.class, () -> {
            cardService.getCardActiveAndValid(cardId);
        });
    }


    @Test
    void testDebit_Success() {
        String cardId = "123456";
        Card mockCard = new Card();
        mockCard.setBalance(new BigDecimal("1000"));
        mockCard.setActive(true);
        mockCard.setBlocked(false);
        mockCard.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId(cardId);
        request.setPrice(new BigDecimal("100"));

        cardService.debit(request);

        assertEquals(new BigDecimal("900"), mockCard.getBalance());
        verify(cardRepository, times(1)).save(mockCard);
    }

    @Test
    void testDebit_InsufficientFunds() {
        String cardId = "123456";
        Card mockCard = new Card();
        mockCard.setBalance(new BigDecimal("50"));
        mockCard.setActive(true);
        mockCard.setBlocked(false);
        mockCard.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId(cardId);
        request.setPrice(new BigDecimal("100"));

        assertThrows(CardException.class, () -> {
            cardService.debit(request);
        });

        verify(cardRepository, never()).save(any(Card.class));
    }

    
    @Test
    void testReversal_Success() {
        String cardId = "123456";
        Card mockCard = new Card();
        mockCard.setBalance(new BigDecimal("900"));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId(cardId);
        request.setPrice(new BigDecimal("100"));

        cardService.reversal(request);

        assertEquals(new BigDecimal("1000"), mockCard.getBalance());
        verify(cardRepository, times(1)).save(mockCard);
    }
}
