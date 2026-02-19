package ru.alenavir.mini_ecommerce.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDto {

    private String name;
    private String email;
    private String password;
}
