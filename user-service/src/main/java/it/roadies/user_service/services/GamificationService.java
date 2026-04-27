package it.roadies.user_service.services;

public interface GamificationService {
    void addPointsBySpending(String userId, double amountSpent);
}