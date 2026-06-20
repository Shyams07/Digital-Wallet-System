package com.payment.user_service.service;

import com.payment.user_service.client.WalletClient;
import com.payment.user_service.entity.User;
import com.payment.user_service.repository.UserRepository;
import com.payment.wallet_service.dto.CreateWalletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    private UserRepository userRepository;
    private final WalletClient walletClient;

    public UserServiceImpl(UserRepository userRepository, WalletClient walletClient){
        this.userRepository = userRepository;
        this.walletClient = walletClient;
    }

    @Override
    public User createUser(User user) {
        User savedUser = userRepository.save(user);
        try {
            CreateWalletRequest request = new CreateWalletRequest();
            request.setUserId(savedUser.getId());
            request.setCurrency("INR");
            walletClient.createWallet(request);
        } catch (Exception ex) {
            userRepository.deleteById(savedUser.getId()); // rollback
            throw new RuntimeException("Wallet creation failed, user rolled back", ex);
        }
        return savedUser;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}

