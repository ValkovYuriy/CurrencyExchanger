package yuriy.dev.exchangeservice.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import yuriy.dev.exchangeservice.dto.CurrencyDto;
import yuriy.dev.exchangeservice.dto.DealDto;
import yuriy.dev.exchangeservice.dto.ExchangeRateDto;
import yuriy.dev.exchangeservice.dto.RequestDto;
import yuriy.dev.exchangeservice.exception.AuthenticationMismatchException;
import yuriy.dev.exchangeservice.exception.NotFoundException;
import yuriy.dev.exchangeservice.mapper.DealMapper;
import yuriy.dev.exchangeservice.model.Deal;
import yuriy.dev.exchangeservice.repository.DealRepository;
import yuriy.dev.model.User;
import yuriy.dev.util.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DealServiceTest {

    @Mock
    private CurrencyServiceClient currencyServiceClient;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private DealMapper dealMapper;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private KafkaTemplate<String, DealDto> kafkaTemplate;

    @InjectMocks
    private DealService dealService;


    @Nested
    class findAllDeals {

        private List<Deal> deals;

        @BeforeEach
        void setUp() {
            deals = List.of(
                    Deal.builder()
                            .id(UUID.randomUUID())
                            .fromCurrencyCode("USD")
                            .toCurrencyCode("EUR")
                            .amountFrom(BigDecimal.valueOf(100))
                            .amountTo(BigDecimal.valueOf(85))
                            .exchangeRate(BigDecimal.valueOf(0.85))
                            .timestamp(LocalDateTime.now())
                            .build(),
                    Deal.builder()
                            .id(UUID.randomUUID())
                            .fromCurrencyCode("EUR")
                            .toCurrencyCode("RUB")
                            .amountFrom(BigDecimal.valueOf(100))
                            .amountTo(BigDecimal.valueOf(9372))
                            .exchangeRate(BigDecimal.valueOf(93.72))
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        @Test
        void findAllDeals_withDefaultRequestDto_Success() {
            RequestDto request = new RequestDto();
            Pageable pageable = PageRequest.of(request.getFrom(), request.getSize());

            Page<Deal> page = new PageImpl<>(deals);

            when(dealRepository.findAll(pageable)).thenReturn(page);
            when(dealMapper.toDealDto(any(Deal.class)))
                    .thenAnswer(inv -> {
                        Deal d = inv.getArgument(0);
                        return DealDto.builder()
                                .id(d.getId())
                                .fromCurrencyCode(d.getFromCurrencyCode())
                                .toCurrencyCode(d.getToCurrencyCode())
                                .amountFrom(d.getAmountFrom())
                                .amountTo(d.getAmountTo())
                                .exchangeRate(d.getExchangeRate())
                                .timestamp(d.getTimestamp())
                                .build();
                    });

            List<DealDto> result = dealService.findAllDeals(request);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isNotNull();
            assertThat(result.get(1).id()).isNotNull();

            verify(dealRepository).findAll(pageable);
            verify(dealMapper, times(2)).toDealDto(any());
        }

        @Test
        void findAllDeals_withCustomRequestDto_Success() {
            RequestDto request = new RequestDto(1, 2);
            Pageable pageable = PageRequest.of(request.getFrom(), request.getSize());

            Page<Deal> page = new PageImpl<>(List.of(deals.get(1)));

            when(dealRepository.findAll(pageable)).thenReturn(page);
            when(dealMapper.toDealDto(any(Deal.class)))
                    .thenAnswer(inv -> {
                        Deal d = inv.getArgument(0);
                        return DealDto.builder()
                                .id(d.getId())
                                .fromCurrencyCode(d.getFromCurrencyCode())
                                .toCurrencyCode(d.getToCurrencyCode())
                                .amountFrom(d.getAmountFrom())
                                .amountTo(d.getAmountTo())
                                .exchangeRate(d.getExchangeRate())
                                .timestamp(d.getTimestamp())
                                .build();
                    });

            List<DealDto> result = dealService.findAllDeals(request);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().id()).isNotNull();

            verify(dealRepository).findAll(pageable);
            verify(dealMapper, times(1)).toDealDto(any());
        }

        @Test
        void findAllDeals_EmptyResult_ShouldReturnEmptyList() {
            RequestDto request = new RequestDto();
            Pageable pageable = PageRequest.of(request.getFrom(), request.getSize());

            when(dealRepository.findAll(pageable))
                    .thenReturn(Page.empty());

            List<DealDto> result = dealService.findAllDeals(request);

            assertThat(result).isEmpty();
        }

        @Test
        void findAllDeals_InvalidPage_ShouldThrowException() {
            RequestDto invalidRequest = new RequestDto(-1, 10);

            assertThrows(IllegalArgumentException.class,
                    () -> dealService.findAllDeals(invalidRequest));
        }
    }


    @Nested
    class findDealBetweenDates {
        private List<Deal> deals;

        @BeforeEach
        void setUp() {
            deals = List.of(
                    Deal.builder()
                            .timestamp(LocalDateTime.of(2025, Month.APRIL, 10, 10, 10))
                            .build(),

                    Deal.builder()
                            .timestamp(LocalDateTime.of(2025, Month.AUGUST, 15, 10, 10))
                            .build()
            );
        }

        @Test
        void findBetweenDates_shouldReturnEmptyList(){
            LocalDate fromDate = LocalDate.of(2026, Month.JANUARY, 1);
            LocalDate toDate = LocalDate.of(2026, Month.DECEMBER, 31);

            assertThat(dealService.findDealsBetweenDates(new RequestDto(), fromDate, toDate))
                    .isEmpty();
        }

        @Test
        void findBetweenDates_shouldReturnListOfOneElement() {
            LocalDate fromDate = LocalDate.of(2025, Month.AUGUST, 10);
            LocalDate toDate = LocalDate.of(2025, Month.SEPTEMBER, 10);

            when(dealRepository.findDealsBetween(anyInt(), anyInt(), any(), any()))
                    .thenAnswer(inv -> {
                        LocalDate from = inv.getArgument(2);
                        LocalDate to = inv.getArgument(3);
                        return deals.stream()
                                .filter(d -> !d.getTimestamp().toLocalDate().isBefore(from))
                                .filter(d -> !d.getTimestamp().toLocalDate().isAfter(to))
                                .toList();
                    });
            when(dealMapper.toDealDto(any(Deal.class)))
                    .thenAnswer(inv -> {
                        Deal d = inv.getArgument(0);
                        return DealDto.builder()
                                .id(d.getId())
                                .fromCurrencyCode(d.getFromCurrencyCode())
                                .toCurrencyCode(d.getToCurrencyCode())
                                .amountFrom(d.getAmountFrom())
                                .amountTo(d.getAmountTo())
                                .exchangeRate(d.getExchangeRate())
                                .timestamp(d.getTimestamp())
                                .build();
                    });

            List<DealDto> result = dealService.findDealsBetweenDates(new RequestDto(), fromDate, toDate);

            assertThat(result)
                    .hasSize(1)
                    .allMatch(dto ->
                            dto.timestamp().isAfter(fromDate.atStartOfDay()) &&
                                    dto.timestamp().isBefore(toDate.plusDays(1).atStartOfDay())
                    );
        }
    }



    //findById

    @Nested
    class findDealById {
        private UUID existingDealId;
        private UUID nonExistingDealId;
        private Deal testDeal;
        private DealDto testDealDto;

        @BeforeEach
        void setUp() {
            existingDealId = UUID.randomUUID();
            nonExistingDealId = UUID.randomUUID();
            User user = User.builder().id(UUID.randomUUID()).build();
            testDeal = Deal.builder()
                    .id(existingDealId)
                    .user(user)
                    .fromCurrencyCode("USD")
                    .toCurrencyCode("EUR")
                    .amountFrom(BigDecimal.valueOf(100))
                    .build();

            testDealDto = DealDto.builder()
                    .id(existingDealId)
                    .userId(user.getId())
                    .fromCurrencyCode("USD")
                    .toCurrencyCode("EUR")
                    .amountFrom(BigDecimal.valueOf(100))
                    .amountTo(BigDecimal.valueOf(85))
                    .exchangeRate(BigDecimal.valueOf(0.85))
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        @Test
        void findDealById_ExistingId_ReturnsDealDto() {
            when(dealRepository.findById(existingDealId))
                    .thenReturn(Optional.of(testDeal));
            when(dealMapper.toDealDto(testDeal))
                    .thenReturn(testDealDto);

            DealDto result = dealService.findDealById(existingDealId);

            assertThat(result)
                    .isNotNull()
                    .isEqualTo(testDealDto);

            verify(dealRepository).findById(existingDealId);
            verify(dealMapper).toDealDto(testDeal);
        }

        @Test
        void findDealById_NonExistingId_ReturnsNull() {
            when(dealRepository.findById(nonExistingDealId))
                    .thenReturn(Optional.empty());

            DealDto result = dealService.findDealById(nonExistingDealId);

            assertThat(result).isNull();
            verify(dealRepository).findById(nonExistingDealId);
            verify(dealMapper, never()).toDealDto(any());
        }

    }


    @Nested
    class addDeal {
        private User user;

        private DealDto inputDealDto, expectedDealDto;

        private Deal savedDeal;

        private CurrencyDto usdCurrency, eurCurrency;

        private ExchangeRateDto exchangeRate;

        @BeforeEach
        public void setUp() {
            user = User.builder()
                    .id(UUID.randomUUID())
                    .username("username")
                    .build();
            inputDealDto = DealDto.builder()
                    .id(UUID.randomUUID())
                    .userId(user.getId())
                    .fromCurrencyCode("USD")
                    .toCurrencyCode("EUR")
                    .amountFrom(BigDecimal.valueOf(100))
                    .build();
            usdCurrency = new CurrencyDto(UUID.randomUUID(), "USD", "Dollar");
            eurCurrency = new CurrencyDto(UUID.randomUUID(), "EUR", "Euro");
            exchangeRate = ExchangeRateDto.builder()
                    .id(UUID.randomUUID())
                    .baseCurrencyId(usdCurrency.getId())
                    .targetCurrencyId(eurCurrency.getId())
                    .rate(BigDecimal.valueOf(0.85))
                    .date(LocalDate.now())
                    .build();
            expectedDealDto = DealDto.builder()
                    .id(inputDealDto.id())
                    .userId(user.getId())
                    .fromCurrencyCode("USD")
                    .toCurrencyCode("EUR")
                    .amountFrom(BigDecimal.valueOf(100))
                    .amountTo(BigDecimal.valueOf(85))
                    .exchangeRate(BigDecimal.valueOf(0.85))
                    .timestamp(LocalDateTime.now())
                    .build();

            savedDeal = Deal.builder()
                    .id(expectedDealDto.id())
                    .user(user)
                    .fromCurrencyCode(expectedDealDto.fromCurrencyCode())
                    .toCurrencyCode(expectedDealDto.toCurrencyCode())
                    .amountFrom(expectedDealDto.amountFrom())
                    .amountTo(expectedDealDto.amountTo())
                    .exchangeRate(expectedDealDto.exchangeRate())
                    .timestamp(expectedDealDto.timestamp())
                    .build();
        }

        @Test
        void addDeal_Success() {
            when(currencyServiceClient.getCurrencyByCode("USD")).thenReturn(Optional.of(usdCurrency));
            when(currencyServiceClient.getCurrencyByCode("EUR")).thenReturn(Optional.of(eurCurrency));
            when(currencyServiceClient.getExchangeRate(usdCurrency.getId(), eurCurrency.getId(), LocalDate.now())).thenReturn(Optional.of(exchangeRate));
            when(securityUtils.getCurrentUser()).thenReturn(user);
            when(dealMapper.toDeal(inputDealDto)).thenReturn(Deal.builder()
                    .id(inputDealDto.id())
                    .user(null)
                    .fromCurrencyCode(inputDealDto.fromCurrencyCode())
                    .toCurrencyCode(inputDealDto.toCurrencyCode())
                    .amountFrom(inputDealDto.amountFrom())
                    .build());
            when(dealRepository.save(any(Deal.class))).thenReturn(savedDeal);
            when(dealMapper.toDealDto(savedDeal)).thenReturn(expectedDealDto);
            CompletableFuture<SendResult<String, DealDto>> future = new CompletableFuture<>();
            future.complete(null);
            when(kafkaTemplate.sendDefault(expectedDealDto)).thenReturn(future);

            DealDto result = dealService.addDeal(inputDealDto);

            assertEquals(expectedDealDto, result);
            verify(kafkaTemplate).sendDefault(expectedDealDto);
        }

        @Test
        void addDeal_CurrencyNotFound() {

            when(currencyServiceClient.getCurrencyByCode("USD")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> dealService.addDeal(inputDealDto));
        }

        @Test
        void addDeal_ExchangeRateNotFound() {
            when(currencyServiceClient.getCurrencyByCode("USD")).thenReturn(Optional.of(usdCurrency));
            when(currencyServiceClient.getCurrencyByCode("EUR")).thenReturn(Optional.of(eurCurrency));
            when(currencyServiceClient.getExchangeRate(any(UUID.class), any(UUID.class), any(LocalDate.class))).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> dealService.addDeal(inputDealDto));
        }

        @Test
        void addDeal_UserMismatch() {
            DealDto dealDtoWithWrongUser = DealDto.builder()
                    .id(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .fromCurrencyCode("USD")
                    .toCurrencyCode("EUR")
                    .amountFrom(BigDecimal.valueOf(100))
                    .build();
            when(currencyServiceClient.getCurrencyByCode("USD")).thenReturn(Optional.of(usdCurrency));
            when(currencyServiceClient.getCurrencyByCode("EUR")).thenReturn(Optional.of(eurCurrency));
            when(currencyServiceClient.getExchangeRate(any(UUID.class), any(UUID.class), any(LocalDate.class))).thenReturn(Optional.of(exchangeRate));
            when(securityUtils.getCurrentUser()).thenReturn(user);

            assertThrows(AuthenticationMismatchException.class, () -> dealService.addDeal(dealDtoWithWrongUser));
        }
    }


    @Nested
    class UpdateDealTests {
        private UUID existingDealId;
        private Deal existingDeal;
        private DealDto updateDealDto;

        @BeforeEach
        void setUpForUpdate() {
            existingDealId = UUID.randomUUID();
            existingDeal = Deal.builder()
                    .id(existingDealId)
                    .fromCurrencyCode("USD")
                    .toCurrencyCode("EUR")
                    .amountFrom(BigDecimal.valueOf(100))
                    .amountTo(BigDecimal.valueOf(85))
                    .exchangeRate(BigDecimal.valueOf(0.85))
                    .timestamp(LocalDateTime.now())
                    .build();

            updateDealDto = new DealDto(
                    existingDealId,
                    any(),
                    "EUR",
                    "RUB",
                    BigDecimal.valueOf(100),
                    BigDecimal.valueOf(9372),
                    BigDecimal.valueOf(93.72),
                    LocalDateTime.now().plusDays(1)
            );
        }

        @Test
        void updateDeal_ExistingId_UpdatesAllFields() {
            when(dealRepository.findById(existingDealId))
                    .thenReturn(Optional.of(existingDeal));
            when(dealRepository.save(any(Deal.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(dealMapper.toDealDto(any(Deal.class)))
                    .thenReturn(updateDealDto);

            DealDto result = dealService.updateDeal(existingDealId, updateDealDto);

            assertThat(result)
                    .isNotNull()
                    .isEqualTo(updateDealDto);

            verify(dealRepository).findById(existingDealId);
            verify(dealRepository).save(existingDeal);
            verify(dealMapper).toDealDto(existingDeal);

            assertThat(existingDeal.getFromCurrencyCode()).isEqualTo("EUR");
            assertThat(existingDeal.getToCurrencyCode()).isEqualTo("RUB");
            assertThat(existingDeal.getAmountFrom()).isEqualTo(BigDecimal.valueOf(100));
            assertThat(existingDeal.getAmountTo()).isEqualTo(BigDecimal.valueOf(9372));
            assertThat(existingDeal.getExchangeRate()).isEqualTo(BigDecimal.valueOf(93.72));
        }

        @Test
        void updateDeal_NonExistingId_ThrowsException() {
            UUID nonExistingId = UUID.randomUUID();
            when(dealRepository.findById(nonExistingId))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> dealService.updateDeal(nonExistingId, updateDealDto));

            verify(dealRepository).findById(nonExistingId);
            verify(dealRepository, never()).save(any());
            verify(dealMapper, never()).toDealDto(any());
        }

    }


    @Nested
    class DeleteDealTests {
        private UUID existingDealId;
        private UUID nonExistingDealId;

        @BeforeEach
        void setUpForDelete() {
            existingDealId = UUID.randomUUID();
            nonExistingDealId = UUID.randomUUID();
        }

        @Test
        void deleteDeal_ExistingId_DeletesSuccessfully() {
            doNothing().when(dealRepository).deleteById(existingDealId);

            dealService.deleteDeal(existingDealId);

            verify(dealRepository).deleteById(existingDealId);
        }

        @Test
        void deleteDeal_NonExistingId_CompletesSilently() {
            doNothing().when(dealRepository).deleteById(nonExistingDealId);

            assertDoesNotThrow(() -> dealService.deleteDeal(nonExistingDealId));
            verify(dealRepository).deleteById(nonExistingDealId);
        }

    }


}
