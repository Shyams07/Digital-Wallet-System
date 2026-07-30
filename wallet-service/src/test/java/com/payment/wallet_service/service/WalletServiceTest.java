package com.payment.wallet_service.service;

import com.payment.wallet_service.dto.*;
import com.payment.wallet_service.entity.Transaction;
import com.payment.wallet_service.entity.Wallet;
import com.payment.wallet_service.entity.WalletHold;
import com.payment.wallet_service.exception.InsufficientFundsException;
import com.payment.wallet_service.exception.NotFoundException;
import com.payment.wallet_service.repository.TransactionRepository;
import com.payment.wallet_service.repository.WalletHoldRepository;
import com.payment.wallet_service.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletHoldRepository walletHoldRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUserId(100L);
        wallet.setCurrency("INR");
        wallet.setBalance(1000L);
        wallet.setAvailableBalance(1000L);
    }

    @Test
    void createWallet_shouldCreateAndReturnWallet() {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(100L);
        request.setCurrency("INR");

        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        WalletResponse response = walletService.createWallet(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(100L, response.getUserId());
        assertEquals("INR", response.getCurrency());
        assertEquals(1000L, response.getBalance());
        assertEquals(1000L, response.getAvailableBalance());

        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void credit_shouldIncreaseBalanceAndSaveTransaction() {
        CreditRequest request = new CreditRequest();
        request.setUserId(100L);
        request.setCurrency("INR");
        request.setAmount(500L);

        when(walletRepository.findByUserIdAndCurrency(100L, "INR"))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.credit(request);

        assertEquals(1500L, response.getBalance());
        assertEquals(1500L, response.getAvailableBalance());

        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void credit_shouldThrowExceptionWhenWalletNotFound() {
        CreditRequest request = new CreditRequest();
        request.setUserId(100L);
        request.setCurrency("INR");
        request.setAmount(500L);

        when(walletRepository.findByUserIdAndCurrency(100L, "INR"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> walletService.credit(request)
        );

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void debit_shouldReduceBalance() {
        DebitRequest request = new DebitRequest();
        request.setUserId(100L);
        request.setCurrency("INR");
        request.setAmount(300L);

        when(walletRepository.findByUserIdAndCurrency(100L, "INR"))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.debit(request);

        assertEquals(700L, response.getBalance());
        assertEquals(700L, response.getAvailableBalance());

        verify(walletRepository).save(wallet);
    }

    @Test
    void debit_shouldThrowExceptionWhenBalanceIsInsufficient() {
        DebitRequest request = new DebitRequest();
        request.setUserId(100L);
        request.setCurrency("INR");
        request.setAmount(1500L);

        when(walletRepository.findByUserIdAndCurrency(100L, "INR"))
                .thenReturn(Optional.of(wallet));

        assertThrows(
                InsufficientFundsException.class,
                () -> walletService.debit(request)
        );

        verify(walletRepository, never()).save(any());
    }

    @Test
    void debit_shouldThrowExceptionWhenWalletNotFound() {
        DebitRequest request = new DebitRequest();
        request.setUserId(100L);
        request.setCurrency("INR");
        request.setAmount(300L);

        when(walletRepository.findByUserIdAndCurrency(100L, "INR"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> walletService.debit(request)
        );
    }

    @Test
    void getWallet_shouldReturnWallet() {
        when(walletRepository.findByUserId(100L))
                .thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.getWallet(100L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(100L, response.getUserId());
        assertEquals("INR", response.getCurrency());
    }

    @Test
    void getWallet_shouldThrowExceptionWhenWalletNotFound() {
        when(walletRepository.findByUserId(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> walletService.getWallet(100L)
        );
    }

    @Test
    void placeHold_shouldReduceAvailableBalanceAndCreateHold() {
        HoldRequest request = new HoldRequest();
        request.setUserId(100L);
        request.setCurrency("INR");
        request.setAmount(400L);

        when(walletRepository.findByUserIdAndCurrency(100L, "INR"))
                .thenReturn(Optional.of(wallet));

        when(walletHoldRepository.save(any(WalletHold.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HoldResponse response = walletService.placeHold(request);

        assertNotNull(response);
        assertEquals(400L, response.getAmount());
        assertEquals("ACTIVE", response.getStatus());
        assertTrue(response.getHoldReference().startsWith("HOLD-"));
        assertEquals(600L, wallet.getAvailableBalance());

        verify(walletRepository).save(wallet);
        verify(walletHoldRepository).save(any(WalletHold.class));
    }

    @Test
    void placeHold_shouldThrowExceptionWhenBalanceIsInsufficient() {
        HoldRequest request = new HoldRequest();
        request.setUserId(100L);
        request.setCurrency("INR");
        request.setAmount(1500L);

        when(walletRepository.findByUserIdAndCurrency(100L, "INR"))
                .thenReturn(Optional.of(wallet));

        assertThrows(
                InsufficientFundsException.class,
                () -> walletService.placeHold(request)
        );

        verify(walletRepository, never()).save(any());
        verify(walletHoldRepository, never()).save(any());
    }

    @Test
    void captureHold_shouldDeductBalanceAndMarkHoldCaptured() {
        WalletHold hold = new WalletHold();
        hold.setWallet(wallet);
        hold.setAmount(300L);
        hold.setHoldReference("HOLD-123");
        hold.setStatus("ACTIVE");

        wallet.setAvailableBalance(700L);

        CaptureRequest request = new CaptureRequest();
        request.setHoldReference("HOLD-123");

        when(walletHoldRepository.findByHoldReference("HOLD-123"))
                .thenReturn(Optional.of(hold));

        WalletResponse response = walletService.captureHold(request);

        assertEquals(700L, response.getBalance());
        assertEquals(700L, response.getAvailableBalance());
        assertEquals("CAPTURED", hold.getStatus());

        verify(walletRepository).save(wallet);
        verify(walletHoldRepository).save(hold);
    }

    @Test
    void captureHold_shouldThrowExceptionWhenHoldNotFound() {
        CaptureRequest request = new CaptureRequest();
        request.setHoldReference("HOLD-123");

        when(walletHoldRepository.findByHoldReference("HOLD-123"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> walletService.captureHold(request)
        );
    }

    @Test
    void captureHold_shouldThrowExceptionWhenHoldIsNotActive() {
        WalletHold hold = new WalletHold();
        hold.setHoldReference("HOLD-123");
        hold.setStatus("RELEASED");

        CaptureRequest request = new CaptureRequest();
        request.setHoldReference("HOLD-123");

        when(walletHoldRepository.findByHoldReference("HOLD-123"))
                .thenReturn(Optional.of(hold));

        assertThrows(
                IllegalStateException.class,
                () -> walletService.captureHold(request)
        );

        verify(walletRepository, never()).save(any());
        verify(walletHoldRepository, never()).save(any());
    }

    @Test
    void releaseHold_shouldRestoreAvailableBalance() {
        wallet.setAvailableBalance(600L);

        WalletHold hold = new WalletHold();
        hold.setWallet(wallet);
        hold.setAmount(400L);
        hold.setHoldReference("HOLD-123");
        hold.setStatus("ACTIVE");

        when(walletHoldRepository.findByHoldReference("HOLD-123"))
                .thenReturn(Optional.of(hold));

        HoldResponse response = walletService.releaseHold("HOLD-123");

        assertEquals(1000L, wallet.getAvailableBalance());
        assertEquals("RELEASED", response.getStatus());
        assertEquals("RELEASED", hold.getStatus());

        verify(walletRepository).save(wallet);
        verify(walletHoldRepository).save(hold);
    }

    @Test
    void releaseHold_shouldThrowExceptionWhenHoldNotFound() {
        when(walletHoldRepository.findByHoldReference("HOLD-123"))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> walletService.releaseHold("HOLD-123")
        );
    }

    @Test
    void releaseHold_shouldThrowExceptionWhenHoldIsNotActive() {
        WalletHold hold = new WalletHold();
        hold.setHoldReference("HOLD-123");
        hold.setStatus("CAPTURED");

        when(walletHoldRepository.findByHoldReference("HOLD-123"))
                .thenReturn(Optional.of(hold));

        assertThrows(
                IllegalStateException.class,
                () -> walletService.releaseHold("HOLD-123")
        );

        verify(walletRepository, never()).save(any());
        verify(walletHoldRepository, never()).save(any());
    }

    @Test
    void credit_shouldSaveCorrectTransactionDetails() {
        CreditRequest request = new CreditRequest();
        request.setUserId(100L);
        request.setCurrency("INR");
        request.setAmount(500L);

        when(walletRepository.findByUserIdAndCurrency(100L, "INR"))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        walletService.credit(request);

        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository).save(captor.capture());

        Transaction savedTransaction = captor.getValue();

        assertEquals(1L, savedTransaction.getWalletId());
        assertEquals("CREDIT", savedTransaction.getType());
        assertEquals(500L, savedTransaction.getAmount());
        assertEquals("SUCCESS", savedTransaction.getStatus());
    }
}