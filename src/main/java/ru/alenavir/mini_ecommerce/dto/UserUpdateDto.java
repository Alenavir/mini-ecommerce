package ru.alenavir.mini_ecommerce.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDto {

    private String name;
    private String email;
    private Boolean isActive;
}

