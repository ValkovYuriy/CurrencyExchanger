package yuriy.dev.exchangeservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import yuriy.dev.exchangeservice.dto.JwtAuthenticationResponse;
import yuriy.dev.exchangeservice.dto.SignInRequest;
import yuriy.dev.exchangeservice.dto.SignUpRequest;
import yuriy.dev.exchangeservice.dto.UserDto;
import yuriy.dev.exchangeservice.mapper.UserMapper;
import yuriy.dev.exchangeservice.model.Role;
import yuriy.dev.exchangeservice.model.User;
import yuriy.dev.exchangeservice.token.JwtUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(Role.builder().role("ROLE_USER").build()))
                .build();
        UserDto userDto = userMapper.toUserDto(user);
        userService.addUser(userDto);

        var jwt = jwtUtil.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var userDetails = (UserDetails) authentication.getPrincipal();
        var jwt = jwtUtil.generateToken(userDetails);

        return new JwtAuthenticationResponse(jwt);
    }
}
