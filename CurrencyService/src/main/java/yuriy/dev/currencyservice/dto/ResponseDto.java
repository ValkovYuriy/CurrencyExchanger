package yuriy.dev.currencyservice.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseDto<D> {

    @Schema(description = "Сообщение при ответе")
    String message;

    @Schema(description = "Данные ответа")
    D data;
}
