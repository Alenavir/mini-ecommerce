package ru.alenavir.mini_ecommerce.dto;

import lombok.Getter;
import lombok.Setter;
import ru.alenavir.mini_ecommerce.entity.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private Boolean isActive;
    private Set<Role> roles;
    private LocalDateTime createdAt;
}

