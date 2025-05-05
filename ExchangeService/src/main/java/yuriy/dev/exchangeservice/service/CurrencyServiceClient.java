package yuriy.dev.exchangeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import yuriy.dev.exchangeservice.dto.CurrencyDto;
import yuriy.dev.exchangeservice.dto.ExchangeRateDto;
import yuriy.dev.token.JwtUtil;
import yuriy.dev.util.SecurityUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyServiceClient {

    private final RestTemplate restTemplate;

    private final SecurityUtils securityUtils;

    private final JwtUtil jwtUtil;

    private static final String CURRENCY_SERVICE_URL = "http://localhost:8082/api/";

    public Optional<CurrencyDto> getCurrencyByCode(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtUtil.generateToken(securityUtils.getCurrentUser()));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<CurrencyDto> response = restTemplate.exchange(
                    CURRENCY_SERVICE_URL + "currencies/" + code,
                    HttpMethod.GET,
                    entity,
                    CurrencyDto.class);

            return Optional.ofNullable(response.getBody());
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }

    public Optional<ExchangeRateDto> getExchangeRate(UUID baseCurrencyId, UUID targetCurrencyId, LocalDate date) {
        String url = UriComponentsBuilder.fromUriString(CURRENCY_SERVICE_URL + "exchange-rates")
                .queryParam("baseCurrencyId", baseCurrencyId)
                .queryParam("targetCurrencyId", targetCurrencyId)
                .queryParam("date", date)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtUtil.generateToken(securityUtils.getCurrentUser()));
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<ExchangeRateDto> response = restTemplate.exchange(url, HttpMethod.GET,entity,ExchangeRateDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (RestClientException e) {
            log.error("Произошла ошибка при загрузке курса обмена");
            return Optional.empty();
        }
    }

}
