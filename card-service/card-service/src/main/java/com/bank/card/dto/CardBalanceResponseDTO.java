package com.bank.card.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Data @NoArgsConstructor @AllArgsConstructor
public class CardBalanceResponseDTO {
    private String cardId;
    private BigDecimal newBalance;
}