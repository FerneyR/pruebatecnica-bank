package com.bank.card.controller;
import com.bank.card.dto.TransactionRequestDTO;
import com.bank.card.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/card/internal")
public class InternalCardController {

    private final CardService cardService;

    public InternalCardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/debit")
    public ResponseEntity<Void> debit(@RequestBody TransactionRequestDTO request) {
        cardService.debit(request);
        
        return ResponseEntity.ok().build();
    }
    @PostMapping("/reversal")
    public ResponseEntity<Void> reversal(@RequestBody TransactionRequestDTO request) {
        cardService.reversal(request);
        return ResponseEntity.ok().build();
    }
}