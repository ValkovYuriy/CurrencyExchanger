package yuriy.dev.exchangeservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import yuriy.dev.dto.RoleDto;
import yuriy.dev.exchangeservice.dto.DealDto;
import yuriy.dev.exchangeservice.dto.RequestDto;
import yuriy.dev.exchangeservice.dto.UserDto;
import yuriy.dev.exchangeservice.exception.NotFoundException;
import yuriy.dev.exchangeservice.mapper.DealMapper;
import yuriy.dev.exchangeservice.mapper.RoleMapper;
import yuriy.dev.exchangeservice.mapper.UserMapper;
import yuriy.dev.exchangeservice.model.Deal;
import yuriy.dev.exchangeservice.repository.DealRepository;
import yuriy.dev.model.Role;
import yuriy.dev.model.User;
import yuriy.dev.repository.RoleRepository;
import yuriy.dev.repository.UserRepository;
import yuriy.dev.util.SecurityUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private DealRepository dealRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private DealMapper dealMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User testUser;
    private UserDto testUserDto;
    private RequestDto testRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testRequest = new RequestDto(0, 10);

        testUser = User.builder()
                .id(userId)
                .username("testUser")
                .password("encodedPass")
                .roles(List.of(new Role(UUID.randomUUID(), "ROLE_USER")))
                .build();

        testUserDto = UserDto.builder()
                .id(userId)
                .username("testUser")
                .password("rawPassword")
                .rolesDto(List.of(new RoleDto(UUID.randomUUID(), "ROLE_USER")))
                .dealsDto(List.of())
                .build();
    }

    @Nested
    class findAllUsers{
        @Test
        void findAllUsers_ReturnsPaginatedResults() {
            Page<User> userPage = new PageImpl<>(List.of(testUser));
            List<Deal> userDeals = List.of(
                    Deal.builder().id(UUID.randomUUID()).user(testUser).build()
            );

            when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
            when(dealRepository.findAllByUserIds(anyList())).thenReturn(userDeals);
            when(roleMapper.toDto(any())).thenReturn(new RoleDto(UUID.randomUUID(), "ROLE_USER"));
            when(dealMapper.toDealDto(any())).thenReturn(DealDto.builder().build());

            List<UserDto> result = userService.findAllUsers(testRequest);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().username()).isEqualTo("testUser");

            verify(userRepository).findAll(PageRequest.of(0, 10));
            verify(dealRepository).findAllByUserIds(List.of(userId));
        }

        @Test
        void findAllUsers_EmptyResult_ReturnsEmptyList() {
            when(userRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

            List<UserDto> result = userService.findAllUsers(testRequest);

            assertThat(result).isEmpty();
        }
    }


    @Nested
    class findById{
        @Test
        void findById_ExistingUser_ReturnsUserDto() {
            List<Deal> userDeals = List.of(
                    Deal.builder().id(UUID.randomUUID()).user(testUser).build()
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(dealRepository.findAllByUserId(userId)).thenReturn(userDeals);
            when(roleMapper.toDto(any())).thenReturn(new RoleDto(UUID.randomUUID(), "ROLE_USER"));
            when(dealMapper.toDealDto(any())).thenReturn(DealDto.builder().build());
            when(securityUtils.getCurrentUser()).thenReturn(testUser);

            UserDto result = userService.findUserById(userId);

            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("testUser");
            assertThat(result.dealsDto()).hasSize(1);

            verify(userRepository).findById(userId);
            verify(dealRepository).findAllByUserId(userId);
        }

        @Test
        void findById_NonExistingUser_ThrowsNotFoundException() {
            when(userRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> userService.findUserById(UUID.randomUUID()));
        }


        @Test
        void findById_ExistingUser_ThrowsAccessDeniedException() {
            UUID someUserId = UUID.randomUUID();
            User mockUser = new User();
            mockUser.setId(UUID.randomUUID());

            when(userRepository.findById(someUserId)).thenReturn(Optional.of(mockUser));
            when(securityUtils.getCurrentUser()).thenReturn(testUser);


            testUser.setId(UUID.randomUUID());
            testUser.setRoles(List.of(new Role(UUID.randomUUID(),"ROLE_USER")));

            assertThrows(AccessDeniedException.class,
                    () -> userService.findUserById(someUserId));
        }
    }


    @Nested
    class addUser{
        @Test
        void addUser_ValidInput_ReturnsSavedUser() {
            when(userMapper.toUser(any())).thenReturn(testUser);
            when(roleRepository.findByRoles(anyList())).thenReturn(testUser.getRoles());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
            when(userRepository.save(any())).thenReturn(testUser);
            when(userMapper.toUserDto(any())).thenReturn(testUserDto);

            UserDto result = userService.addUser(testUserDto);

            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("testUser");

            verify(passwordEncoder).encode("rawPassword");
            verify(userRepository).save(testUser);
        }

    }


    @Nested
    class updateUser{
        @Test
        void updateUser_ValidInput_UpdatesUser() {
            UserDto updateDto = UserDto.builder()
                    .username("updatedUser")
                    .password("newPassword")
                    .rolesDto(List.of(new RoleDto(UUID.randomUUID(), "ROLE_ADMIN")))
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByRoles(anyList())).thenReturn(List.of(new Role(UUID.randomUUID(), "ROLE_ADMIN")));
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPass");
            when(userRepository.save(any())).thenReturn(testUser);
            when(userMapper.toUserDto(any())).thenReturn(updateDto);

            UserDto result = userService.updateUser(userId, updateDto);

            assertThat(result.username()).isEqualTo("updatedUser");
            assertThat(testUser.getPassword()).isEqualTo("newEncodedPass");

            verify(userRepository).findById(userId);
            verify(userRepository).save(testUser);
        }

        @Test
        void updateUser_NonExistingUser_ThrowsException() {
            when(userRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> userService.updateUser(UUID.randomUUID(), testUserDto));
        }
    }


    @Nested
    class deleteUser{
        @Test
        void deleteUser_ExistingUser_DeletesSuccessfully() {
            doNothing().when(userRepository).deleteById(userId);

            assertDoesNotThrow(() -> userService.deleteUser(userId));
            verify(userRepository).deleteById(userId);
        }

    }

}
