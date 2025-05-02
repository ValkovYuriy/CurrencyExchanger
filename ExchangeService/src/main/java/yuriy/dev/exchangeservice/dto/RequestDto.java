package yuriy.dev.exchangeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
public class RequestDto {

    @Builder.Default
    @Schema(description = "Индекс начала выборки", example = "0")
    @PositiveOrZero
    private int from = 0;

    @Builder.Default
    @Schema(description = "Количество элементов в выборке",example = "10")
    @Positive
    private int size = 10;
}
