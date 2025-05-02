package yuriy.dev.currencyservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yuriy.dev.currencyservice.dto.ExchangeRateDto;
import yuriy.dev.currencyservice.dto.UpdateExchangeRateDto;
import yuriy.dev.currencyservice.service.ExchangeRateService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exchange-rates")
@Tag(name = "Api для работы с курсом валют")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @Operation(summary = "Получение списка всех курсов обмена",security = @SecurityRequirement(name = "JWT"))
    @GetMapping("/all")
    public ResponseEntity<List<ExchangeRateDto>> findAllExchangeRates(){
        List<ExchangeRateDto> list = exchangeRateService.findAllExchangeRates();
        return ResponseEntity.ok(list);
    }
    @Operation(summary = "Получение курса обмена по id",security = @SecurityRequirement(name = "JWT"))
    @GetMapping("/{id}")
    public ResponseEntity<ExchangeRateDto> findExchangeRateById(@PathVariable UUID id){
        ExchangeRateDto dto = exchangeRateService.findExchangeRateById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Получение курса обмена базовой валюты на целевую за определенную дату",security = @SecurityRequirement(name = "JWT"))
    @GetMapping(params = {"baseCurrencyId","targetCurrencyId","date"})
    public ResponseEntity<ExchangeRateDto> findExchangeRateForCurrencies(@RequestParam UUID baseCurrencyId,
                                                                                      @RequestParam UUID targetCurrencyId,
                                                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        ExchangeRateDto dto = exchangeRateService.findExchangeRateForCurrencies(baseCurrencyId, targetCurrencyId,date);
        return ResponseEntity.ok(dto);
    }
    @Operation(summary = "Добавление курса обмена",security = @SecurityRequirement(name = "JWT"))
    @PostMapping
    public ResponseEntity<ExchangeRateDto> addExchangeRate(@RequestBody @Valid ExchangeRateDto dto){
        ExchangeRateDto addedDto = exchangeRateService.addExchangeRate(dto);
        return ResponseEntity.ok(addedDto);
    }

    @Operation(summary = "Обновление курса обмена",security = @SecurityRequirement(name = "JWT"))
    @PutMapping("/{id}")
    public ResponseEntity<ExchangeRateDto> updateExchangeRate(@PathVariable UUID id, @Valid @RequestBody UpdateExchangeRateDto dto){
        ExchangeRateDto updatedDto = exchangeRateService.updateExchangeRate(id,dto);
        return ResponseEntity.ok(updatedDto);
    }

    @Operation(summary = "Удаление курса обмена",security = @SecurityRequirement(name = "JWT"))
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExchangeRate(@PathVariable UUID id){
        exchangeRateService.deleteExchangeRate(id);
        return ResponseEntity.ok().build();
    }


}
