package yuriy.dev.exchangeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на аутентификацию")
public class SignInRequest {

    @Schema(description = "Имя пользователя", example = "admin")
    @Size(min = 1, max = 30, message = "Имя пользователя должно содержать от 1 до 30 символов")
    @NotBlank(message = "Имя пользователя не может быть пустыми")
    private String username;

    @Schema(description = "Пароль", example = "admin")
    @Size(min = 1, max = 30, message = "Длина пароля должна быть от 1 до 30 символов")
    @NotBlank(message = "Пароль не может быть пустыми")
    private String password;
}
