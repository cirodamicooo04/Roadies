package it.roadies.user_service.services;
import java.math.BigDecimal;

public interface GamificationService {
    void addPointsBySpending(String userId, Long amountSpent);
}