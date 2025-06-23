package ru.pionerpixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.pionerpixel.entity.Account;
import ru.pionerpixel.entity.User;
import ru.pionerpixel.repository.AccountRepository;
import ru.pionerpixel.repository.UserRepository;
import ru.pionerpixel.service.JwtService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private String validToken;

    @BeforeEach
    void prepareAccounts() {
        accountRepository.deleteAll();
        userRepository.deleteAll();

        User user1 = new User();
        user1.setName("testUser1");
        user1.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user1.setPassword("password");
        user1 = userRepository.save(user1);

        User user2 = new User();
        user2.setName("testUser2");
        user2.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user2.setPassword("password");
        user2 = userRepository.save(user2);

        Account from = new Account();
        from.setBalance(BigDecimal.valueOf(1000.00));
        from.setInitialDeposit(BigDecimal.valueOf(0.0));
        from.setUser(user1);
        accountRepository.save(from);

        Account to = new Account();
        to.setBalance(BigDecimal.valueOf(500.00));
        to.setInitialDeposit(BigDecimal.valueOf(0.0));
        to.setUser(user2);
        accountRepository.save(to);

        validToken = "Bearer " + jwtService.generateAccessToken(user1.getId());
    }

    @Test
    void transferMoneyApi_Success() throws Exception {
        Account from = accountRepository.findAll().get(0);
        Account to = accountRepository.findAll().get(1);

        String requestBody = String.format(
            "{\"recipientId\":%d,\"amount\":200}",
            to.getUser().getId());

        mockMvc.perform(post("/api/users/transfer")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());
    }
}