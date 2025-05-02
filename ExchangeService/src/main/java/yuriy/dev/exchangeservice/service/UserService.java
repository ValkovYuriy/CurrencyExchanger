package yuriy.dev.exchangeservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yuriy.dev.exchangeservice.dto.RequestDto;
import yuriy.dev.exchangeservice.dto.UserDto;
import yuriy.dev.exchangeservice.mapper.UserMapper;
import yuriy.dev.exchangeservice.model.User;
import yuriy.dev.exchangeservice.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    public List<UserDto> findAllUsers(RequestDto requestDto) {
        Pageable pageable = PageRequest.of(requestDto.getFrom(), requestDto.getSize());
        return userRepository
                .findAll(pageable)
                .stream()
                .map(userMapper::toUserDto)
                .toList();
    }


    public UserDto findById(UUID id) {
        return userRepository.findById(id).map(userMapper::toUserDto).orElse(null);
    }

    @Transactional
    public UserDto addUser(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        return userMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUser(UUID id, UserDto userDto) {
        User user = userRepository.findById(id).orElse(null);
        assert user != null;
        user.setUsername(userDto.username());
        user.setPassword(userDto.password());
        return userMapper.toUserDto(userRepository.save(user));
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
    

}
