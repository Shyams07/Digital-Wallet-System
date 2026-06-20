package com.payment.reward_service.kafka;


import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payment.reward_service.entity.Reward;
import com.payment.reward_service.entity.Transaction;
import com.payment.reward_service.repository.RewardRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RewardConsumer {

    private final RewardRepository rewardRepository;
    private final JsonMapper objectMapper;

    public RewardConsumer(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    @KafkaListener(topics = "txn-initiated", groupId = "reward-group")
    public void consumeTransaction(String message) {
        try {
            Transaction transaction = objectMapper.readValue(message, Transaction.class);

            if (rewardRepository.existsByTransactionId(transaction.getId())) {
                System.out.println("Reward already exists for transaction: " + transaction.getId());
                return;
            }

            Reward reward = new Reward();
            reward.setUserId(transaction.getSenderId());
            reward.setPoints(transaction.getAmount() * 100);
            reward.setSentAt(LocalDateTime.now());
            reward.setTransactionId(transaction.getId());
            rewardRepository.save(reward);

            System.out.println("Reward saved: " + reward);

        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}