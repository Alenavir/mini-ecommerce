package ru.alenavir.mini_ecommerce.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alenavir.mini_ecommerce.entity.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
