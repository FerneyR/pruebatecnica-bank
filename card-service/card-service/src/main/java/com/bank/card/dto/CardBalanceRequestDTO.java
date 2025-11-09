package com.bank.card.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Data @NoArgsConstructor
public class CardBalanceRequestDTO {
    private String cardId;
    private BigDecimal balance;
}