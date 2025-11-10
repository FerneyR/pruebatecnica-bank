package com.bank.transaction.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.transaction.model.Transaction;
@Data @NoArgsConstructor @AllArgsConstructor
public class TransactionDetailsDTO {
    private Long transactionId;
    private String cardId;
    private BigDecimal price;
    private LocalDateTime transactionDate;
    private boolean isAnnulled;

    public static TransactionDetailsDTO fromEntity(Transaction entity) {
        return new TransactionDetailsDTO(
            entity.getTransactionId(),
            entity.getCardId(),
            entity.getPrice(),
            entity.getTransactionDate(),
            entity.isAnnulled()
        );
    }
}