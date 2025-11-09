package com.bank.transaction.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data @NoArgsConstructor
public class AnulationRequestDTO {
    private String cardId;
    private Long transactionId;
}