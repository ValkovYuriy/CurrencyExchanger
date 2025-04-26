package yuriy.dev.currencyservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yuriy.dev.currencyservice.dto.CurrencyDto;
import yuriy.dev.currencyservice.service.CurrencyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/currencies")
@Tag(name = "Api для работы с валютами")
public class CurrencyController {

    private final CurrencyService currencyService;

    @Operation(summary = "Получение списка всех валют")
    @GetMapping
    public ResponseEntity<List<CurrencyDto>> findAllCurrencies(){
        List<CurrencyDto> list = currencyService.findAllCurrencies();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Поиск валюты по коду")
    @GetMapping("/{code}")
    public ResponseEntity<CurrencyDto> findCurrencyByCode(@PathVariable String code){
        CurrencyDto dto = currencyService.findCurrencyByCode(code);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Добавление валюты")
    @PostMapping
    public ResponseEntity<CurrencyDto> addCurrency(@RequestBody CurrencyDto dto){
        CurrencyDto addedDto = currencyService.addCurrency(dto);
        return ResponseEntity.ok(addedDto);
    }

    @Operation(summary = "Обновление валюты")
    @PutMapping("/{id}")
    public ResponseEntity<CurrencyDto> updateCurrency(@PathVariable UUID id, @RequestBody CurrencyDto dto){
        CurrencyDto updatedDto = currencyService.updateCurrency(id,dto);
        return ResponseEntity.ok(updatedDto);
    }

    @Operation(summary = "Удаление валюты")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCurrency(@PathVariable UUID id){
        currencyService.deleteCurrency(id);
        return ResponseEntity.ok().build();
    }


}
