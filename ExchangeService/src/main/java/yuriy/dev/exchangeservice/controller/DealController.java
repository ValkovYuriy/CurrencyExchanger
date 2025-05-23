package yuriy.dev.exchangeservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yuriy.dev.exchangeservice.dto.DealDto;
import yuriy.dev.exchangeservice.dto.RequestDto;
import yuriy.dev.exchangeservice.dto.ResponseDto;
import yuriy.dev.exchangeservice.service.DealService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deals")
@Tag(name = "Api для работы со сделками")
public class DealController {

    private final DealService dealService;


    @Operation(summary = "Получение списка сделок", security = @SecurityRequirement(name = "JWT"))
    @GetMapping
    public ResponseEntity<ResponseDto<List<DealDto>>> findAllDeals(@Valid @ModelAttribute RequestDto requestDto){
        List<DealDto> list = dealService.findAllDeals(requestDto);
        return ResponseEntity.ok(new ResponseDto<>("OK", list));
    }

    @Operation(summary = "Поиск сделок за определенный период", security = @SecurityRequirement(name = "JWT"))
    @GetMapping("/between-dates")
    public ResponseEntity<ResponseDto<List<DealDto>>> findBetweenDates(@Valid @ModelAttribute RequestDto requestDto,
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate toDate){
        List<DealDto> list = dealService.findDealsBetweenDates(requestDto,fromDate,toDate);
        return ResponseEntity.ok(new ResponseDto<>("OK", list));
    }

    @Operation(summary = "Поиск сделки по id", security = @SecurityRequirement(name = "JWT"))
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<DealDto>> findDealById(@PathVariable UUID id){
        DealDto dto = dealService.findDealById(id);
        return ResponseEntity.ok(new ResponseDto<>("OK", dto));
    }

    @Operation(summary = "Добавление сделки", security = @SecurityRequirement(name = "JWT"))
    @PostMapping
    public ResponseEntity<ResponseDto<DealDto>> addDeal(@Valid @RequestBody DealDto dto){
        DealDto addedDto = dealService.addDeal(dto);
        return ResponseEntity.ok(new ResponseDto<>("OK", addedDto));
    }

    @Operation(summary = "Обновление сделки", security = @SecurityRequirement(name = "JWT"))
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<DealDto>> updateDeal(@PathVariable UUID id, @RequestBody DealDto dto){
        DealDto updatedDto = dealService.updateDeal(id,dto);
        return ResponseEntity.ok(new ResponseDto<>("OK", updatedDto));
    }

    @Operation(summary = "Удаление сделки", security = @SecurityRequirement(name = "JWT"))
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDeal(@PathVariable UUID id){
        dealService.deleteDeal(id);
        return ResponseEntity.ok().build();
    }


}
