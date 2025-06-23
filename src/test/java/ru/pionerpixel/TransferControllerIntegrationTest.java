package ru.pionerpixel;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.pionerpixel.dto.transfer.TransferRequestDto;
import ru.pionerpixel.entity.Account;
import ru.pionerpixel.entity.EmailData;
import ru.pionerpixel.entity.PhoneData;
import ru.pionerpixel.entity.User;
import ru.pionerpixel.repository.AccountRepository;
import ru.pionerpixel.repository.EmailDataRepository;
import ru.pionerpixel.repository.PhoneDataRepository;
import ru.pionerpixel.repository.UserRepository;
import ru.pionerpixel.service.JwtService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class TransferControllerIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("jwt.secret", () -> "testSecretKeyForJwtTokenGenerationAndValidationInTestsOnly");
        registry.add("jwt.access-token-expiration", () -> 3600000L);
        registry.add("jwt.refresh-token-expiration", () -> 86400000L);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmailDataRepository emailDataRepository;

    @Autowired
    private PhoneDataRepository phoneDataRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User senderUser;
    private User recipientUser;
    private Account senderAccount;
    private Account recipientAccount;
    private String senderToken;
    private String recipientToken;

    @BeforeEach
    void setUp() {
        // Clean up database
        accountRepository.deleteAll();
        emailDataRepository.deleteAll();
        phoneDataRepository.deleteAll();
        userRepository.deleteAll();

        // Create sender user and account
        senderUser = new User();
        senderUser.setName("Sender User");
        senderUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        senderUser.setPassword("$2a$10$cqZInCnLzNIMiUspoP0s/e/yH292le203zy7rNRrmE0Dfdpz2x1K."); // "test123"
        senderUser = userRepository.save(senderUser);

        senderAccount = new Account();
        senderAccount.setUser(senderUser);
        senderAccount.setBalance(new BigDecimal("1000.00"));
        senderAccount.setInitialDeposit(new BigDecimal("1000.00"));
        senderAccount = accountRepository.save(senderAccount);

        // Create recipient user and account
        recipientUser = new User();
        recipientUser.setName("Recipient User");
        recipientUser.setDateOfBirth(LocalDate.of(1995, 5, 15));
        recipientUser.setPassword("$2a$10$cqZInCnLzNIMiUspoP0s/e/yH292le203zy7rNRrmE0Dfdpz2x1K."); // "test123"
        recipientUser = userRepository.save(recipientUser);

        recipientAccount = new Account();
        recipientAccount.setUser(recipientUser);
        recipientAccount.setBalance(new BigDecimal("500.00"));
        recipientAccount.setInitialDeposit(new BigDecimal("500.00"));
        recipientAccount = accountRepository.save(recipientAccount);

        // Add email and phone for authentication
        EmailData senderEmail = new EmailData();
        senderEmail.setUser(senderUser);
        senderEmail.setEmail("sender@test.com");
        emailDataRepository.save(senderEmail);

        PhoneData senderPhone = new PhoneData();
        senderPhone.setUser(senderUser);
        senderPhone.setPhone("71234567890");
        phoneDataRepository.save(senderPhone);

        EmailData recipientEmail = new EmailData();
        recipientEmail.setUser(recipientUser);
        recipientEmail.setEmail("recipient@test.com");
        emailDataRepository.save(recipientEmail);

        PhoneData recipientPhone = new PhoneData();
        recipientPhone.setUser(recipientUser);
        recipientPhone.setPhone("79876543210");
        phoneDataRepository.save(recipientPhone);

        // Generate JWT tokens
        senderToken = "Bearer " + jwtService.generateAccessToken(senderUser.getId());
        recipientToken = "Bearer " + jwtService.generateAccessToken(recipientUser.getId());
    }

    @Test
    void transferMoney_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Arrange
        TransferRequestDto request = new TransferRequestDto();
        request.setRecipientId(recipientUser.getId());
        request.setAmount(new BigDecimal("100.00"));

        // Act & Assert
        mockMvc.perform(post("/api/users/transfer")
                .header("Authorization", "Bearer invalid.token.here")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void transferMoney_NoToken_ReturnsUnauthorized() throws Exception {
        // Arrange
        TransferRequestDto request = new TransferRequestDto();
        request.setRecipientId(recipientUser.getId());
        request.setAmount(new BigDecimal("100.00"));

        // Act & Assert
        mockMvc.perform(post("/api/users/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void transferMoney_InsufficientFunds_ReturnsBadRequest() throws Exception {
        // Arrange
        TransferRequestDto request = new TransferRequestDto();
        request.setRecipientId(recipientUser.getId());
        request.setAmount(new BigDecimal("1500.00"));

        // Act & Assert
        mockMvc.perform(post("/api/users/transfer")
                .header("Authorization", senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Insufficient funds")));
    }

    @Test
    void transferMoney_SameUser_ReturnsBadRequest() throws Exception {
        // Arrange
        TransferRequestDto request = new TransferRequestDto();
        request.setRecipientId(senderUser.getId()); // Same as sender
        request.setAmount(new BigDecimal("100.00"));

        // Act & Assert
        mockMvc.perform(post("/api/users/transfer")
                .header("Authorization", senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("тот же самый аккаунт")));
    }

    @Test
    void transferMoney_InvalidRequestFormat_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users/transfer")
                .header("Authorization", senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invalid\": \"json\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void transferMoney_MissingRecipientId_ReturnsBadRequest() throws Exception {
        // Arrange
        TransferRequestDto request = new TransferRequestDto();
        request.setAmount(new BigDecimal("100.00"));
        // recipientId is null

        // Act & Assert
        mockMvc.perform(post("/api/users/transfer")
                .header("Authorization", senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.recipientId", notNullValue()));
    }

    @Test
    void transferMoney_MissingAmount_ReturnsBadRequest() throws Exception {
        // Arrange
        TransferRequestDto request = new TransferRequestDto();
        request.setRecipientId(recipientUser.getId());
        // amount is null

        // Act & Assert
        mockMvc.perform(post("/api/users/transfer")
                .header("Authorization", senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.amount", notNullValue()));
    }
} 