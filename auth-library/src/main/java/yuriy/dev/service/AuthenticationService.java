package yuriy.dev.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import yuriy.dev.dto.JwtAuthenticationResponse;
import yuriy.dev.dto.SignInRequest;
import yuriy.dev.dto.SignUpRequest;
import yuriy.dev.model.Role;
import yuriy.dev.model.User;
import yuriy.dev.repository.RoleRepository;
import yuriy.dev.repository.UserRepository;
import yuriy.dev.token.JwtUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(roleRepository.findByName("ROLE_USER")
                        .orElseGet(() -> roleRepository.save(Role.builder().role("ROLE_USER").build()))))
                .build();

        userRepository.save(user);
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
