package com.payment.reward_service.service;

import com.payment.reward_service.entity.Reward;
import java.util.List;

public interface RewardService {
    Reward sendReward(Reward reward);
    List<Reward> getRewardsByUserId(Long UserId);
}
