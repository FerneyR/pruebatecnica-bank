package com.bank.transaction.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor
public class TransactionResponseDTO {
    private Long transactionId;
    private String cardId;
    private BigDecimal price;
    private LocalDateTime transactionDate;
}