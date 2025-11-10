package com.bank.transaction.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bank.transaction.dto.AnulationRequestDTO;
import com.bank.transaction.dto.TransactionDetailsDTO;
import com.bank.transaction.dto.TransactionRequestDTO;
import com.bank.transaction.dto.TransactionResponseDTO;
import com.bank.transaction.exception.TransactionException;
import com.bank.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void testPurchase_Success() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId("123456");
        request.setPrice(new BigDecimal("100"));
        
        TransactionResponseDTO response = new TransactionResponseDTO(
            1L, "123456", new BigDecimal("100"), LocalDateTime.now()
        );

        when(transactionService.purchase(any(TransactionRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/transaction/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").value(1L))
            .andExpect(jsonPath("$.cardId").value("123456"));
    }

    @Test
    void testPurchase_Failure_CardServiceFails() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setCardId("123456");
        request.setPrice(new BigDecimal("100"));
        
        when(transactionService.purchase(any(TransactionRequestDTO.class)))
            .thenThrow(new TransactionException("Error al validar tarjeta: Saldo insuficiente"));

        mockMvc.perform(post("/transaction/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Error al validar tarjeta: Saldo insuficiente"));
    }


    @Test
    void testGetTransaction_Success() throws Exception {
        TransactionDetailsDTO response = new TransactionDetailsDTO(
            1L, "123456", new BigDecimal("100"), LocalDateTime.now(), false
        );
        
        when(transactionService.getTransaction(1L)).thenReturn(response);

        mockMvc.perform(get("/transaction/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").value(1L))
            .andExpect(jsonPath("$.annulled").value(false));
    }

    @Test
    void testGetTransaction_NotFound() throws Exception {
        when(transactionService.getTransaction(anyLong()))
            .thenThrow(new TransactionException("Transacción no encontrada (not found)"));
            
        mockMvc.perform(get("/transaction/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Transacción no encontrada (not found)"));
    }


    @Test
    void testCancelTransaction_Success() throws Exception {
        AnulationRequestDTO request = new AnulationRequestDTO();
        request.setCardId("123456");
        request.setTransactionId(1L);
        
        doNothing().when(transactionService).cancelTransaction(any(AnulationRequestDTO.class));

        mockMvc.perform(post("/transaction/anulation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void testCancelTransaction_Failure_TooLate() throws Exception {
        AnulationRequestDTO request = new AnulationRequestDTO();
        request.setCardId("123456");
        request.setTransactionId(1L);
        
        doThrow(new TransactionException("No se puede anular, han pasado más de 24 horas."))
            .when(transactionService).cancelTransaction(any(AnulationRequestDTO.class));

        mockMvc.perform(post("/transaction/anulation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("No se puede anular, han pasado más de 24 horas."));
    }
}