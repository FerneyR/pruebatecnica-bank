package com.bank.card.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bank.card.dto.TransactionRequestDTO;
import com.bank.card.exception.CardException;
import com.bank.card.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(InternalCardController.class)
class InternalCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void testDebit_Success() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId("123456");
        request.setPrice(new BigDecimal("100"));
        
        doNothing().when(cardService).debit(any(TransactionRequestDTO.class));

        mockMvc.perform(post("/card/internal/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

@Test
    void testDebit_Failure_InsufficientFunds() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId("123456");
        request.setPrice(new BigDecimal("1000"));

        doThrow(new CardException("Saldo insuficiente"))
            .when(cardService).debit(any(TransactionRequestDTO.class));

        mockMvc.perform(post("/card/internal/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Saldo insuficiente"));
    }


    @Test
    void testReversal_Success() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId("123456");
        request.setPrice(new BigDecimal("100"));
        
        doNothing().when(cardService).reversal(any(TransactionRequestDTO.class));

        mockMvc.perform(post("/card/internal/reversal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk()); 
    }

@Test
    void testReversal_Failure_CardNotFound() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId("non-existent");
        request.setPrice(new BigDecimal("100"));

        doThrow(new CardException("Tarjeta no encontrada"))
            .when(cardService).reversal(any(TransactionRequestDTO.class));

        mockMvc.perform(post("/card/internal/reversal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Tarjeta no encontrada"));
    }
}