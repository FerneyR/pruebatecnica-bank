package com.bank.card.controller;
import com.bank.card.dto.CardBalanceRequestDTO;
import com.bank.card.dto.CardBalanceResponseDTO;
import com.bank.card.dto.CardEnrollRequestDTO;
import com.bank.card.dto.CardNumberResponseDTO;
import com.bank.card.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/card")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/{productId}/number")
    public ResponseEntity<CardNumberResponseDTO> generarNumeroTarjeta(@PathVariable String productId) {
        String cardNumber = cardService.generarNumeroTarjeta(productId);
        return ResponseEntity.ok(new CardNumberResponseDTO(cardNumber, productId));
    }

    @PostMapping("/enroll")
    public ResponseEntity<Void> activarTarjeta(@RequestBody CardEnrollRequestDTO enrollRequest) {
        cardService.activarTarjeta(enrollRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> bloquearTarjeta(@PathVariable String cardId) {
        cardService.bloquearTarjeta(cardId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/balance")
    public ResponseEntity<CardBalanceResponseDTO> recargarSaldo(@RequestBody CardBalanceRequestDTO request) {
        CardBalanceResponseDTO response = cardService.recargarSaldo(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance/{cardId}")
    public ResponseEntity<BigDecimal> consultarSaldo(@PathVariable String cardId) {
        BigDecimal balance = cardService.consultarSaldo(cardId);
        return ResponseEntity.ok(balance);
    }
}