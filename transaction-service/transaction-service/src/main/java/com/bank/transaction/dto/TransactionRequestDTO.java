package com.bank.transaction.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Data @NoArgsConstructor
public class TransactionRequestDTO {
    private String cardId;
    private BigDecimal price;
}