package com.bank.card.service.impl;

import com.bank.card.dto.CardBalanceRequestDTO;
import com.bank.card.dto.CardBalanceResponseDTO;
import com.bank.card.dto.CardEnrollRequestDTO;
import com.bank.card.dto.TransactionRequestDTO;
import com.bank.card.exception.CardException;
import com.bank.card.model.Card;
import com.bank.card.repository.CardRepository;
import com.bank.card.service.CardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    public CardServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public String generarNumeroTarjeta(String productId) {
        if (productId == null || productId.length() != 6) {
            throw new IllegalArgumentException("El ID de producto debe ser de 6 dígitos.");
        }

        String numeroGenerado = generateCardNumber(productId);

        Card nuevaTarjeta = Card.builder()
            .cardId(numeroGenerado)
            .productId(productId)
            .holderName("Usuario Bank")
            .expiryDate(LocalDate.now().plusYears(3))
            .balance(BigDecimal.ZERO)
            .isActive(false)
            .isBlocked(false)
            .build();

        cardRepository.save(nuevaTarjeta);
        return numeroGenerado;
    }
    
    private String generateCardNumber(String productId) {
        StringBuilder sb = new StringBuilder(productId);
        for (int i = 0; i < 10; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        return sb.toString();
    }

    @Override
    public Card activarTarjeta(CardEnrollRequestDTO enrollRequest) {
        Card card = getCardById(enrollRequest.getCardId());

        if (card.isActive()) {
            throw new CardException("La tarjeta ya se encuentra activa.");
        }
        card.setActive(true);
        return cardRepository.save(card);
    }

    @Override
    public void bloquearTarjeta(String cardId) {
        Card card = getCardById(cardId);
        if (card.isBlocked()) {
            throw new CardException("La tarjeta ya se encuentra bloqueada.");
        }
        card.setBlocked(true);
        cardRepository.save(card);
    }

    @Override
    public CardBalanceResponseDTO recargarSaldo(CardBalanceRequestDTO request) {
        Card card = getCardById(request.getCardId());
        
        if (request.getBalance() == null || request.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La recarga debe ser un monto positivo.");
        }
        
        card.setBalance(card.getBalance().add(request.getBalance()));
        Card cardGuardada = cardRepository.save(card);
        
        return new CardBalanceResponseDTO(cardGuardada.getCardId(), cardGuardada.getBalance());
    }

    @Override
    public BigDecimal consultarSaldo(String cardId) {
        Card card = getCardById(cardId);
        return card.getBalance();
    }

    @Override
    public Card getCardById(String cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardException("Tarjeta no encontrada (not found) con id: " + cardId));
    }

    @Override
    public Card getCardActiveAndValid(String cardId) {
        Card card = getCardById(cardId);
        
        if (!card.isActive()) {
            throw new CardException("La tarjeta no está activa.");
        }
        if (card.isBlocked()) {
            throw new CardException("La tarjeta está bloqueada.");
        }
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new CardException("La tarjeta ha expirado.");
        }
        return card;
    }

    @Override
    public void saveCard(Card card) {
        cardRepository.save(card);
    }

    @Override
    public void debit(TransactionRequestDTO request) {
        Card card = getCardActiveAndValid(request.getCardId());

        if (card.getBalance().compareTo(request.getPrice()) < 0) {
            throw new CardException("Saldo insuficiente.");
        }
        card.setBalance(card.getBalance().subtract(request.getPrice()));
        cardRepository.save(card);
    }

    @Override
    public void reversal(TransactionRequestDTO request) {
        Card card = getCardById(request.getCardId());
        card.setBalance(card.getBalance().add(request.getPrice()));
        cardRepository.save(card);
    }
}