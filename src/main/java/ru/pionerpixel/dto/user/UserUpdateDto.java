package ru.pionerpixel.dto.user;

import jakarta.validation.constraints.*;

import java.util.List;

public class UserUpdateDto {
    private List<@Email String> emails;

    public List<@Pattern(regexp = "^7\\d{10}$") String> getPhones() {
        return phones;
    }

    public void setPhones(List<@Pattern(regexp = "^7\\d{10}$") String> phones) {
        this.phones = phones;
    }

    public List<@Email String> getEmails() {
        return emails;
    }

    public void setEmails(List<@Email String> emails) {
        this.emails = emails;
    }

    private List<@Pattern(regexp = "^7\\d{10}$") String> phones;

}