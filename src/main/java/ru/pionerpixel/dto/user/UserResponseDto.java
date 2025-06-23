package ru.pionerpixel.dto.user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class UserResponseDto {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private BigDecimal balance;
    private List<String> emails;
    private List<String> phones;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }

    public List<String> getPhones() { return phones; }
    public void setPhones(List<String> phones) { this.phones = phones; }
}
