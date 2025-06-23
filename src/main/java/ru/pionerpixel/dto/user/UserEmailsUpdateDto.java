package ru.pionerpixel.dto.user;

import jakarta.validation.constraints.Email;

import java.util.List;

public class UserEmailsUpdateDto {
    private List<@Email String> emails;

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }
}