package yuriy.dev.exchangeservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yuriy.dev.exchangeservice.dto.JwtAuthenticationResponse;
import yuriy.dev.exchangeservice.dto.SignInRequest;
import yuriy.dev.exchangeservice.dto.SignUpRequest;
import yuriy.dev.exchangeservice.service.AuthenticationService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Api для аутентификации пользователей")
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @Operation(summary = "Аутентификация пользователя")
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }
}
