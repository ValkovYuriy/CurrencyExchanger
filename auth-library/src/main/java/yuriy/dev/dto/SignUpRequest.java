package yuriy.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на регистрацию")
public class SignUpRequest {

    @Schema(description = "Имя пользователя", example = "test")
    @Size(min = 1, max = 30, message = "Имя пользователя должно содержать от 1 до 30 символов")
    @NotBlank(message = "Имя пользователя не может быть пустыми")
    private String username;

    @Schema(description = "Пароль", example = "test")
    @Size(min = 1, max = 30, message = "Длина пароля должна быть от 1 до 30 символов")
    @NotBlank(message = "Пароль не может быть пустыми")
    private String password;

}
