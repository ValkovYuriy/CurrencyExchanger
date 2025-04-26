package yuriy.dev.exchangeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import yuriy.dev.exchangeservice.dto.CurrencyDto;
import yuriy.dev.exchangeservice.dto.ExchangeRateDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyServiceClient {

    private final RestTemplate restTemplate;

    private static final String CURRENCY_SERVICE_URL = "http://localhost:8082/api/";

    public List<CurrencyDto> getAllCurrencies() {
        ResponseEntity<List<CurrencyDto>> response = restTemplate.exchange(
                CURRENCY_SERVICE_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    public Optional<CurrencyDto> getCurrencyByCode(String code) {
        try {
            return Optional.of(restTemplate.getForObject(CURRENCY_SERVICE_URL + "/currencies/" + code, CurrencyDto.class));
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }

    public Optional<ExchangeRateDto> getExchangeRate(UUID baseCurrencyId, UUID targetCurrencyId, LocalDate date) {
        String url = UriComponentsBuilder.fromUriString(CURRENCY_SERVICE_URL + "/exchange-rates")
                .queryParam("baseCurrencyId", baseCurrencyId)
                .queryParam("targetCurrencyId", targetCurrencyId)
                .queryParam("date", date)
                .toUriString();

        try {
            ExchangeRateDto response = restTemplate.getForObject(url, ExchangeRateDto.class);
            return Optional.of(response);
        } catch (RestClientException e) {
            log.error("Произошла ошибка при загрузке курса обмена");
            return Optional.empty();
        }
    }

}
