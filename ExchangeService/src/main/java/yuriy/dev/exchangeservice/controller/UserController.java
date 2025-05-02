package yuriy.dev.exchangeservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yuriy.dev.exchangeservice.dto.RequestDto;
import yuriy.dev.exchangeservice.dto.ResponseDto;
import yuriy.dev.exchangeservice.dto.UserDto;
import yuriy.dev.exchangeservice.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Api для работы с пользователями")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Получение списка пользователей", security = @SecurityRequirement(name = "JWT"))
    @GetMapping
    public ResponseEntity<ResponseDto<List<UserDto>>> findAllUsers(@Valid @ModelAttribute RequestDto requestDto){
        List<UserDto> list = userService.findAllUsers(requestDto);
        return ResponseEntity.ok(new ResponseDto<>("OK", list));
    }

    @Operation(summary = "Получение пользователя по id", security = @SecurityRequirement(name = "JWT"))
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<UserDto>> findUserById(@PathVariable UUID id){
        UserDto dto = userService.findById(id);
        return ResponseEntity.ok(new ResponseDto<>("OK", dto));
    }

    @Operation(summary = "Добавление нового пользователя", security = @SecurityRequirement(name = "JWT"))
    @PostMapping
    public ResponseEntity<ResponseDto<UserDto>> addUser(@RequestBody UserDto dto){
        UserDto addedDto = userService.addUser(dto);
        return ResponseEntity.ok(new ResponseDto<>("OK", addedDto));
    }

    @Operation(summary = "Обновление пользователя", security = @SecurityRequirement(name = "JWT"))
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<UserDto>> updateUser(@PathVariable UUID id, @RequestBody UserDto dto){
        UserDto updatedDto = userService.updateUser(id,dto);
        return ResponseEntity.ok(new ResponseDto<>("OK", updatedDto));
    }

    @Operation(summary = "Удаление пользователя", security = @SecurityRequirement(name = "JWT"))
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id){
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }


}
