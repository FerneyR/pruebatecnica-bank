package com.bank.card.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bank.card.dto.CardBalanceRequestDTO;
import com.bank.card.dto.CardBalanceResponseDTO;
import com.bank.card.dto.CardEnrollRequestDTO;
import com.bank.card.exception.CardException;
import com.bank.card.model.Card;
import com.bank.card.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGenerarNumeroTarjeta_Success() throws Exception {
        String productId = "123456";
        String numeroTarjetaGenerado = "1234560000000000";
        
        when(cardService.generarNumeroTarjeta(productId)).thenReturn(numeroTarjetaGenerado);

        mockMvc.perform(get("/card/" + productId + "/number"))
            .andExpect(status().isOk()) 
            .andExpect(jsonPath("$.cardNumber").value(numeroTarjetaGenerado))
            .andExpect(jsonPath("$.productId").value(productId));
    }

  @Test
    void testActivarTarjeta_Success() throws Exception {
        CardEnrollRequestDTO request = new CardEnrollRequestDTO();
        request.setCardId("123456");

        when(cardService.activarTarjeta(any(CardEnrollRequestDTO.class))).thenReturn(new Card());

        mockMvc.perform(post("/card/enroll")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void testBloquearTarjeta_Success() throws Exception {
        String cardId = "123456";
        doNothing().when(cardService).bloquearTarjeta(cardId);

        mockMvc.perform(delete("/card/" + cardId))
            .andExpect(status().isOk());
    }

@Test
    void testRecargarSaldo_Success() throws Exception {
        CardBalanceRequestDTO request = new CardBalanceRequestDTO();
        request.setCardId("123456");
        request.setBalance(new BigDecimal("100"));

        CardBalanceResponseDTO response = new CardBalanceResponseDTO(
            "123456", new BigDecimal("100")
        );
        
        when(cardService.recargarSaldo(any(CardBalanceRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/card/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk()) 
            .andExpect(jsonPath("$.cardId").value("123456"))
            .andExpect(jsonPath("$.newBalance").value(100));
    }

    @Test
    void testConsultarSaldo_Success() throws Exception {
        String cardId = "123456";
        BigDecimal balance = new BigDecimal("500.75");
        
        when(cardService.consultarSaldo(cardId)).thenReturn(balance);

        mockMvc.perform(get("/card/balance/" + cardId))
            .andExpect(status().isOk())
            .andExpect(content().string("500.75"));
    }


    @Test
    void testConsultarSaldo_NotFound() throws Exception {
        String cardId = "non-existent";
        
        when(cardService.consultarSaldo(cardId))
            .thenThrow(new CardException("Tarjeta no encontrada"));

        mockMvc.perform(get("/card/balance/" + cardId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Tarjeta no encontrada"));
    }
    
    @Test
    void testGenerarNumeroTarjeta_BadRequest() throws Exception {
        String productId = "123";
        
        when(cardService.generarNumeroTarjeta(productId))
            .thenThrow(new IllegalArgumentException("El ID de producto debe ser de 6 dígitos."));

        mockMvc.perform(get("/card/" + productId + "/number"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("El ID de producto debe ser de 6 dígitos."));
    }
}