package com.bank.card.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data @NoArgsConstructor @AllArgsConstructor
public class CardNumberResponseDTO {
    private String cardNumber;
    private String productId;

    public static CardNumberResponseDTO of(String cardNumber, String productId) {
        return new CardNumberResponseDTO(cardNumber, productId);
    }
}