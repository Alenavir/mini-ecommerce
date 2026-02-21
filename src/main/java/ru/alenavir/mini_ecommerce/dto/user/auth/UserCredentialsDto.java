package ru.alenavir.mini_ecommerce.dto.user.auth;

import lombok.Data;

@Data
public class UserCredentialsDto {
    private String email;
    private String password;
}
