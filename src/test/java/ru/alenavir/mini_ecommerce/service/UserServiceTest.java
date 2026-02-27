package ru.alenavir.mini_ecommerce.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alenavir.mini_ecommerce.dto.user.UserCreateDto;
import ru.alenavir.mini_ecommerce.dto.user.UserResponseDto;
import ru.alenavir.mini_ecommerce.dto.user.UserUpdateDto;
import ru.alenavir.mini_ecommerce.entity.User;
import ru.alenavir.mini_ecommerce.exceptions.NotFoundException;
import ru.alenavir.mini_ecommerce.mapper.UserMapper;
import ru.alenavir.mini_ecommerce.repo.UserRepo;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo repo;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService service;

    private final User user = new User() {{
        setId(1L);
        setName("Alice");
        setEmail("alice@gmail.com");
        setIsActive(true);
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }};

    private final UserCreateDto createDto = new UserCreateDto() {{
        setName("Alice");
        setEmail("alice@gmail.com");
        setPassword("password123");
    }};

    private final UserUpdateDto updateDto = new UserUpdateDto() {{
        setName("Alice Updated");
        setEmail("alice.updated@gmail.com");
    }};

    private final UserResponseDto responseDto = new UserResponseDto() {{
        setId(1L);
        setName("Alice");
        setEmail("alice@gmail.com");
    }};

    @Test
    void save_ShouldReturnResponseDto() {
        when(mapper.toEntity(createDto)).thenReturn(user);
        when(repo.save(user)).thenReturn(user);
        when(mapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = service.save(createDto);

        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        verify(repo).save(user);
    }

//    @Test
//    void findAll_ShouldReturnList() {
//        List<User> users = Collections.singletonList(user);
//        List<UserResponseDto> responses = Collections.singletonList(responseDto);
//
//        when(repo.findAll()).thenReturn(users);
//        when(mapper.toDtoList(users)).thenReturn(responses);
//
//        List<UserResponseDto> result = service.findAll();
//
//        assertEquals(1, result.size());
//        assertEquals(responseDto.getId(), result.get(0).getId());
//    }

    @Test
    void findById_WhenFound_ShouldReturnResponse() {
        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = service.findById(1L);

        assertEquals(responseDto.getId(), result.getId());
    }

    @Test
    void findById_WhenNotFound_ShouldThrow() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void update_WhenFound_ShouldReturnUpdatedResponse() throws Exception {
        when(repo.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(mapper).updateUserFromDto(updateDto, user);
        when(repo.save(user)).thenReturn(user);
        when(mapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = service.update(1L, updateDto);

        assertEquals(responseDto.getId(), result.getId());
        verify(mapper).updateUserFromDto(updateDto, user);
        verify(repo).save(user);
    }

    @Test
    void update_WhenNotFound_ShouldThrow() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(1L, updateDto));
    }

    @Test
    void delete_WhenFound_ShouldCallRepoDelete() {
        when(repo.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repo).deleteById(1L);
    }

    @Test
    void delete_WhenNotFound_ShouldThrow() {
        when(repo.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(1L));
    }
}
