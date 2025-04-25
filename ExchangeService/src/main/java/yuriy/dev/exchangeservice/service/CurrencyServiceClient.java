package yuriy.dev.exchangeservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import yuriy.dev.exchangeservice.dto.CurrencyDto;
import yuriy.dev.exchangeservice.dto.ExchangeRateDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
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

    public Optional<ExchangeRateDto> getExchangeRate(UUID baseCurrencyId, UUID targetCurrencyId) {
        try {
            return Optional.of(restTemplate.getForObject(CURRENCY_SERVICE_URL + "/exchange-rates", ExchangeRateDto.class, baseCurrencyId, targetCurrencyId));
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }

}
