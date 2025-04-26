package yuriy.dev.currencyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record CurrencyDto(
        @Schema(hidden = true)
        UUID id,
        @Schema(description = "Код валюты", example = "USD")
        String code,
        @Schema(description = "Название валюты", example = "Dollar")
        String name
) {
}
