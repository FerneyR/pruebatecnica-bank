package com.bank.card.service;

import com.bank.card.dto.CardBalanceRequestDTO;
import com.bank.card.dto.CardBalanceResponseDTO;
import com.bank.card.dto.CardEnrollRequestDTO;
import com.bank.card.dto.TransactionRequestDTO;
import com.bank.card.model.Card;

import java.math.BigDecimal;

public interface CardService {

    String generarNumeroTarjeta(String productId);

    Card activarTarjeta(CardEnrollRequestDTO enrollRequest);

    void bloquearTarjeta(String cardId);

    CardBalanceResponseDTO recargarSaldo(CardBalanceRequestDTO request);

    BigDecimal consultarSaldo(String cardId);
    
    Card getCardById(String cardId);
    
    Card getCardActiveAndValid(String cardId);

    void saveCard(Card card);

    void debit(TransactionRequestDTO request);

    void reversal(TransactionRequestDTO request);
}