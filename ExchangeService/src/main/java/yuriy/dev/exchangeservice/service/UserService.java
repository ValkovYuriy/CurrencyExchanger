package yuriy.dev.exchangeservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import yuriy.dev.dto.RoleDto;
import yuriy.dev.exchangeservice.dto.RequestDto;
import yuriy.dev.exchangeservice.dto.UserDto;
import yuriy.dev.exchangeservice.exception.NotFoundException;
import yuriy.dev.exchangeservice.mapper.DealMapper;
import yuriy.dev.exchangeservice.mapper.RoleMapper;
import yuriy.dev.exchangeservice.mapper.UserMapper;
import yuriy.dev.exchangeservice.model.Deal;
import yuriy.dev.exchangeservice.repository.DealRepository;
import yuriy.dev.model.User;
import yuriy.dev.repository.RoleRepository;
import yuriy.dev.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final DealRepository dealRepository;
    private final DealMapper dealMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> findAllUsers(RequestDto requestDto) {
        Pageable pageable = PageRequest.of(requestDto.getFrom(), requestDto.getSize());
        List<User> users = userRepository.findAll(pageable).stream().toList();
        List<UUID> usersIds = users.stream().map(User::getId).toList();
        List<Deal> deals = dealRepository.findAllByUserIds(usersIds);
        Map<UUID, List<Deal>> dealsByUserId = deals.stream()
                .collect(Collectors.groupingBy(deal -> deal.getUser().getId()));
        return users.stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getRoles().stream().map(roleMapper::toDto).toList(),
                        dealsByUserId.getOrDefault(user.getId(), Collections.emptyList()).stream().map(dealMapper::toDealDto).toList()
                ))
                .toList();
    }


    public UserDto findById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(()-> new NotFoundException("Пользователь не найден"));
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .rolesDto(user.getRoles().stream().map(roleMapper::toDto).toList())
                .dealsDto(dealRepository.findAllByUserId(user.getId()).stream().map(dealMapper::toDealDto).toList())
                .build();
    }

    @Transactional
    public UserDto addUser(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        user.setRoles(roleRepository.findByRoles(userDto.rolesDto().stream().map(RoleDto::role).toList()));
        user.setPassword(passwordEncoder.encode(userDto.password()));
        return userMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUser(UUID id, UserDto userDto) {
        User user = userRepository.findById(id).orElseThrow(()-> new NotFoundException("Пользователь не найден"));
        user.setUsername(userDto.username());
        user.setPassword(passwordEncoder.encode(userDto.password()));
        user.setRoles(roleRepository.findByRoles(userDto.rolesDto().stream().map(RoleDto::role).toList()));
        return userMapper.toUserDto(userRepository.save(user));
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

}
