package it.roadies.booking_service.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    public String paymentIntentId;
    public String clientSecret;
    public String status;
}
