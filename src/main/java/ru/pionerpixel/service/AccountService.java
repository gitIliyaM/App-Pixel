package ru.pionerpixel.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pionerpixel.entity.Account;
import ru.pionerpixel.exception.InsufficientFundsException;
import ru.pionerpixel.exception.UserNotFoundException;
import ru.pionerpixel.repository.AccountRepository;
import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transferMoney(Long fromUserId, Long toUserId, BigDecimal amount) {
        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("Нельзя переводить на тот же самый аккаунт");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }

        Account fromAccount = accountRepository.findByUserId(fromUserId)
            .orElseThrow(() -> new UserNotFoundException("Отправитель не найден с id: " + fromUserId));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        int updatedFrom = accountRepository.updateBalance(fromUserId, amount.negate());
        if (updatedFrom == 0) {
            throw new InsufficientFundsException();
        }

        int updatedTo = accountRepository.updateBalance(toUserId, amount);
        if (updatedTo == 0) {
            accountRepository.updateBalance(fromUserId, amount);
            throw new IllegalStateException("Не удалось обновить баланс получателя");
        }
    }

    @Scheduled(fixedRate = 30000000)
    @Transactional
    public void applyInterest() {
        int pageSize = 100;
        int pageNumber = 0;
        Page<Account> page;

        do {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            page = accountRepository.findAll(pageable);

            for (Account account : page.getContent()) {
                BigDecimal maxAllowed = account.getInitialDeposit().multiply(new BigDecimal("2.07"));
                BigDecimal newBalance = account.getBalance().multiply(new BigDecimal("1.1"));

                if (newBalance.compareTo(maxAllowed) <= 0) {
                    account.setBalance(newBalance);
                    accountRepository.save(account);
                }
            }
            pageNumber++;
        } while (page.hasNext());
    }
}