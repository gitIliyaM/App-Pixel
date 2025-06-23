package ru.pionerpixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pionerpixel.entity.Account;
import ru.pionerpixel.entity.User;
import ru.pionerpixel.exception.InsufficientFundsException;
import ru.pionerpixel.repository.AccountRepository;
import ru.pionerpixel.service.AccountService;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account senderAccount;
    private Account recipientAccount;
    private User senderUser;
    private User recipientUser;

    private Account createAccount(Long id, BigDecimal balance) {
        Account account = new Account();
        account.setId(id);
        account.setBalance(balance);
        account.setInitialDeposit(BigDecimal.valueOf(0.00));
        account.setId(id);
        return account;
    }

    @BeforeEach
    void setUp() {
        // Create test users
        senderUser = new User();
        senderUser.setId(1L);
        senderUser.setName("Sender User");
        senderUser.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
        senderUser.setPassword("password");

        recipientUser = new User();
        recipientUser.setId(2L);
        recipientUser.setName("Recipient User");
        recipientUser.setDateOfBirth(java.time.LocalDate.of(1995, 5, 15));
        recipientUser.setPassword("password");

        // Create test accounts
        senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setUser(senderUser);
        senderAccount.setBalance(new BigDecimal("1000.00"));
        senderAccount.setInitialDeposit(new BigDecimal("1000.00"));

        recipientAccount = new Account();
        recipientAccount.setId(2L);
        recipientAccount.setUser(recipientUser);
        recipientAccount.setBalance(new BigDecimal("500.00"));
        recipientAccount.setInitialDeposit(new BigDecimal("500.00"));
    }

    @Test
    public void transferMoney_Success() {
        Long fromUserId = 1L;
        Long toUserId = 2L;
        BigDecimal amount = BigDecimal.valueOf(200.00);

        when(accountRepository.findByUserId(fromUserId))
            .thenReturn(Optional.of(createAccount(fromUserId, BigDecimal.valueOf(1000.00))));

        when(accountRepository.updateBalance(fromUserId, amount.negate())).thenReturn(1);
        when(accountRepository.updateBalance(toUserId, amount)).thenReturn(1);

        accountService.transferMoney(fromUserId, toUserId, amount);

        verify(accountRepository, times(1)).findByUserId(fromUserId);
        verify(accountRepository, times(1)).updateBalance(fromUserId, amount.negate());
        verify(accountRepository, times(1)).updateBalance(toUserId, amount);
    }

    @Test
    void transferMoney_InsufficientFunds() {
        Long fromUserId = 1L;
        Long toUserId = 2L;
        BigDecimal amount = BigDecimal.valueOf(200.00);

        Account fromAccount = createAccount(fromUserId, BigDecimal.valueOf(100.00));

        when(accountRepository.findByUserId(fromUserId))
            .thenReturn(Optional.of(fromAccount));

        assertThrows(InsufficientFundsException.class,
            () -> accountService.transferMoney(fromUserId, toUserId, amount));

        verify(accountRepository, times(1)).findByUserId(fromUserId);
    }

    @Test
    void transferMoney_ExactBalance_ShouldNotThrow() {
        // Подготовка
        Account fromAccount = createAccount(1L, new BigDecimal("200.00"));
        Account toAccount = createAccount(2L, BigDecimal.ZERO);

        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(toAccount));

        // Заглушки для updateBalance должны быть добавлены только если они действительно нужны
        when(accountRepository.updateBalance(1L, new BigDecimal("-200.00"))).thenReturn(1);
        when(accountRepository.updateBalance(2L, new BigDecimal("200.00"))).thenReturn(1);

        // Выполнение и проверка
        assertDoesNotThrow(() ->
            accountService.transferMoney(1L, 2L, new BigDecimal("200.00")));

        // Проверка обновлений баланса
        verify(accountRepository).updateBalance(1L, new BigDecimal("-200.00"));
        verify(accountRepository).updateBalance(2L, new BigDecimal("200.00"));
    }

    @Test
    void transferMoney_NegativeAmount() {
        Account fromAccount = createAccount(1L, BigDecimal.valueOf(1000.00));

        verify(accountRepository, times(0)).save(any());
        assertThrows(IllegalArgumentException.class,
            () -> accountService.transferMoney(1L, 2L, BigDecimal.valueOf(-100.00)));
    }

    @Test
    void transferMoneySameAccountException() {
        assertThatThrownBy(() -> accountService.transferMoney(1L, 1L, new BigDecimal("100.00")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Нельзя переводить на тот же самый аккаунт");
    }

    @Test
    void transferMoneyNegativeAmountException() {
        assertThatThrownBy(() -> accountService.transferMoney(1L, 2L, new BigDecimal("-100.00")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Сумма должна быть положительной");
    }

    @Test
    void transferMoneyZeroAmountException() {
        assertThatThrownBy(() -> accountService.transferMoney(1L, 2L, BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Сумма должна быть положительной");
    }
}