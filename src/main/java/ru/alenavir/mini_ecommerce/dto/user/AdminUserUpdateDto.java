package ru.alenavir.mini_ecommerce.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Role;

import java.util.Set;

@Getter
@Setter
public class AdminUserUpdateDto {

    @Size(min = 2, max = 255)
    private String name;

    @Email
    private String email;

    private Set<Role> roles;

    private Boolean isActive;
}