package com.bank.card.service.impl;

import com.bank.card.dto.CardEnrollRequestDTO;
import com.bank.card.exception.CardException;
import com.bank.card.model.Card;
import com.bank.card.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    
    @Test
    void testActivarTarjeta_CuandoTarjetaExisteEInactiva_DebeActivarla() {
        
        CardEnrollRequestDTO request = new CardEnrollRequestDTO();
        request.setCardId("123456");

        Card tarjetaFalsa = new Card();
        tarjetaFalsa.setCardId("123456");
        tarjetaFalsa.setActive(false);
        tarjetaFalsa.setBalance(BigDecimal.ZERO);
        tarjetaFalsa.setExpiryDate(LocalDate.now().plusYears(1));

        when(cardRepository.findById("123456"))
            .thenReturn(Optional.of(tarjetaFalsa));

        when(cardRepository.save(any(Card.class))).thenReturn(tarjetaFalsa);

        Card tarjetaResultado = cardService.activarTarjeta(request);
        
        assertNotNull(tarjetaResultado);
        assertTrue(tarjetaResultado.isActive());
    }

    @Test
    void testActivarTarjeta_CuandoTarjetaYaEstaActiva_DebeLanzarExcepcion() {
        
        CardEnrollRequestDTO request = new CardEnrollRequestDTO();
        request.setCardId("123456");

        Card tarjetaFalsaActiva = new Card();
        tarjetaFalsaActiva.setCardId("123456");
        tarjetaFalsaActiva.setActive(true);

        when(cardRepository.findById("123456"))
            .thenReturn(Optional.of(tarjetaFalsaActiva));
        
        assertThrows(CardException.class, () -> {
            
            cardService.activarTarjeta(request);
            
        });
        
        verify(cardRepository, never()).save(any(Card.class));
    }
}