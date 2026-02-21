package ru.alenavir.mini_ecommerce.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDto {

    @Size(min = 2, max = 255)
    private String name;

    @Email(message = "Email must be valid")
    private String email;

}

