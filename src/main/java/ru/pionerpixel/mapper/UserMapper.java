package ru.pionerpixel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.pionerpixel.dto.user.UserResponseDto;
import ru.pionerpixel.entity.EmailData;
import ru.pionerpixel.entity.PhoneData;
import ru.pionerpixel.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "emails", source = "emails")
    @Mapping(target = "phones", source = "phones")
    @Mapping(target = "balance", source = "account.balance")
    UserResponseDto toDto(User user);

    default List<String> mapEmails(List<EmailData> emails) {
        return emails.stream()
            .map(EmailData::getEmail)
            .collect(Collectors.toList());
    }

    default List<String> mapPhones(List<PhoneData> phones) {
        return phones.stream()
            .map(PhoneData::getPhone)
            .collect(Collectors.toList());
    }
}