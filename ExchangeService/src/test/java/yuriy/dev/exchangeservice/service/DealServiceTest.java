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
import org.springframework.security.access.AccessDeniedException;
import yuriy.dev.exchangeservice.dto.CurrencyDto;
import yuriy.dev.exchangeservice.dto.DealDto;
import yuriy.dev.exchangeservice.dto.ExchangeRateDto;
import yuriy.dev.exchangeservice.dto.RequestDto;
import yuriy.dev.exchangeservice.exception.NotFoundException;
import yuriy.dev.exchangeservice.mapper.DealMapper;
import yuriy.dev.exchangeservice.model.Deal;
import yuriy.dev.exchangeservice.repository.DealRepository;
import yuriy.dev.model.Role;
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
        private List<Deal> allDeals;
        private User regularUser;
        private User adminUser;
        private UUID userId;
        private UUID adminId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            adminId = UUID.randomUUID();

            regularUser = User.builder()
                    .id(userId)
                    .roles(List.of(Role.builder().role("ROLE_USER").build()))
                    .build();

            adminUser = User.builder()
                    .id(adminId)
                    .roles(List.of(Role.builder().role("ROLE_ADMIN").build()))
                    .build();

            allDeals = List.of(
                    Deal.builder()
                            .id(UUID.randomUUID())
                            .user(User.builder().id(userId).build())
                            .fromCurrencyCode("USD")
                            .toCurrencyCode("EUR")
                            .amountFrom(BigDecimal.valueOf(100))
                            .amountTo(BigDecimal.valueOf(85))
                            .exchangeRate(BigDecimal.valueOf(0.85))
                            .timestamp(LocalDateTime.now())
                            .build(),
                    Deal.builder()
                            .id(UUID.randomUUID())
                            .user(User.builder().id(adminId).build())
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
        void findAllDeals_AdminUser_ReturnsAllDeals() {
            RequestDto request = new RequestDto();
            Pageable pageable = PageRequest.of(request.getFrom(), request.getSize());
            Page<Deal> page = new PageImpl<>(allDeals);

            when(securityUtils.getCurrentUser()).thenReturn(adminUser);
            when(dealRepository.findAll(pageable)).thenReturn(page);
            when(dealMapper.toDealDto(any(Deal.class))).thenAnswer(inv -> {
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
            verify(dealRepository).findAll(pageable);
            verify(dealRepository, never()).findAllByUserId(any());
        }

        @Test
        void findAllDeals_RegularUser_ReturnsOnlyOwnDeals() {
            RequestDto request = new RequestDto();
            List<Deal> userDeals = allDeals.stream()
                    .filter(d -> d.getUser().getId().equals(userId))
                    .toList();

            when(securityUtils.getCurrentUser()).thenReturn(regularUser);
            when(dealRepository.findAllByUserId(userId)).thenReturn(userDeals);
            when(dealMapper.toDealDto(any(Deal.class))).thenAnswer(inv -> {
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
            assertThat(result.getFirst().fromCurrencyCode()).isEqualTo("USD");
            verify(dealRepository).findAllByUserId(userId);
            verify(dealRepository, never()).findAll();
        }

        @Test
        void findAllDeals_EmptyResult_ShouldReturnEmptyList() {
            RequestDto request = new RequestDto();

            when(securityUtils.getCurrentUser()).thenReturn(regularUser);
            when(dealRepository.findAllByUserId(regularUser.getId()))
                    .thenReturn(List.of());

            List<DealDto> result = dealService.findAllDeals(request);

            assertThat(result).isEmpty();
        }

        @Test
        void findAllDeals_InvalidPage_ShouldThrowException() {
            RequestDto invalidRequest = new RequestDto(-1, 10);

            assertThrows(IllegalArgumentException.class,
                    () -> dealService.findAllDeals(invalidRequest));
        }

        @Test
        void findAllDeals_PaginationWorksCorrectly() {
            RequestDto request = new RequestDto(1, 1);
            Pageable pageable = PageRequest.of(request.getFrom(), request.getSize());
            Page<Deal> page = new PageImpl<>(List.of(allDeals.get(1)));

            when(securityUtils.getCurrentUser()).thenReturn(adminUser);
            when(dealRepository.findAll(pageable)).thenReturn(page);
            when(dealMapper.toDealDto(any(Deal.class))).thenAnswer(inv -> {
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
            assertThat(result.getFirst().fromCurrencyCode()).isEqualTo("EUR");
        }
    }


    @Nested
    class findDealBetweenDates {
        private List<Deal> deals;
        private User regularUser;
        private User adminUser;
        private UUID userId;
        private UUID adminId;
        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            adminId = UUID.randomUUID();

            regularUser = User.builder()
                    .id(userId)
                    .roles(List.of(Role.builder().role("ROLE_USER").build()))
                    .build();

            adminUser = User.builder()
                    .id(adminId)
                    .roles(List.of(Role.builder().role("ROLE_ADMIN").build()))
                    .build();
            deals = List.of(
                    Deal.builder()
                            .user(regularUser)
                            .timestamp(LocalDateTime.of(2025, Month.APRIL, 10, 10, 10))
                            .build(),

                    Deal.builder()
                            .user(adminUser)
                            .timestamp(LocalDateTime.of(2025, Month.AUGUST, 15, 10, 10))
                            .build()
            );
        }

        @Test
        void findBetweenDates_withUser_shouldReturnEmptyList(){
            LocalDate fromDate = LocalDate.of(2026, Month.JANUARY, 1);
            LocalDate toDate = LocalDate.of(2026, Month.DECEMBER, 31);
            when(securityUtils.getCurrentUser()).thenReturn(regularUser);
            assertThat(dealService.findDealsBetweenDates(new RequestDto(), fromDate, toDate))
                    .isEmpty();
        }

        @Test
        void findBetweenDates_withAdmin_shouldReturnListOfOneElement() {
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
                                .userId(d.getUser().getId())
                                .fromCurrencyCode(d.getFromCurrencyCode())
                                .toCurrencyCode(d.getToCurrencyCode())
                                .amountFrom(d.getAmountFrom())
                                .amountTo(d.getAmountTo())
                                .exchangeRate(d.getExchangeRate())
                                .timestamp(d.getTimestamp())
                                .build();
                    });
            when(securityUtils.getCurrentUser()).thenReturn(adminUser);

            List<DealDto> result = dealService.findDealsBetweenDates(new RequestDto(), fromDate, toDate);

            assertThat(result)
                    .hasSize(1)
                    .allMatch(dto ->
                            dto.userId().equals(adminId) &&
                            dto.timestamp().isAfter(fromDate.atStartOfDay()) &&
                                    dto.timestamp().isBefore(toDate.plusDays(1).atStartOfDay())
                    );
        }

        @Test
        void findBetweenDates_withUser_shouldReturnListOfOneElement() {
            LocalDate fromDate = LocalDate.of(2025, Month.APRIL, 10);
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
                                .userId(d.getUser().getId())
                                .fromCurrencyCode(d.getFromCurrencyCode())
                                .toCurrencyCode(d.getToCurrencyCode())
                                .amountFrom(d.getAmountFrom())
                                .amountTo(d.getAmountTo())
                                .exchangeRate(d.getExchangeRate())
                                .timestamp(d.getTimestamp())
                                .build();
                    });
            when(securityUtils.getCurrentUser()).thenReturn(regularUser);

            List<DealDto> result = dealService.findDealsBetweenDates(new RequestDto(), fromDate, toDate);

            assertThat(result)
                    .hasSize(1)
                    .allMatch(dto ->
                            dto.userId().equals(userId) &&
                                    dto.timestamp().isAfter(fromDate.atStartOfDay()) &&
                                    dto.timestamp().isBefore(toDate.plusDays(1).atStartOfDay())
                    );
        }

    }


    @Nested
    class findDealById {
        private UUID existingDealIdForUser, existingDealIdForAdmin;
        private UUID nonExistingDealId;
        private Deal testDealForUser,testDealForAdmin;
        private DealDto testDealForUserDto,testDealForAdminDto;
        private User regularUser;
        private User adminUser;
        private UUID userId;
        private UUID adminId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            adminId = UUID.randomUUID();

            regularUser = User.builder()
                    .id(userId)
                    .roles(List.of(Role.builder().role("ROLE_USER").build()))
                    .build();

            adminUser = User.builder()
                    .id(adminId)
                    .roles(List.of(Role.builder().role("ROLE_ADMIN").build()))
                    .build();
            existingDealIdForUser = UUID.randomUUID();
            existingDealIdForAdmin = UUID.randomUUID();
            nonExistingDealId = UUID.randomUUID();
            testDealForUser = Deal.builder()
                    .id(existingDealIdForUser)
                    .user(regularUser)
                    .fromCurrencyCode("USD")
                    .toCurrencyCode("EUR")
                    .amountFrom(BigDecimal.valueOf(100))
                    .build();
            testDealForAdmin = Deal.builder()
                    .id(existingDealIdForAdmin)
                    .user(adminUser)
                    .fromCurrencyCode("USD")
                    .toCurrencyCode("EUR")
                    .amountFrom(BigDecimal.valueOf(100))
                    .build();

            testDealForUserDto = DealDto.builder()
                    .id(existingDealIdForUser)
                    .userId(userId)
                    .fromCurrencyCode("USD")
                    .toCurrencyCode("EUR")
                    .amountFrom(BigDecimal.valueOf(100))
                    .amountTo(BigDecimal.valueOf(85))
                    .exchangeRate(BigDecimal.valueOf(0.85))
                    .timestamp(LocalDateTime.now())
                    .build();

            testDealForAdminDto = DealDto.builder()
                    .id(existingDealIdForAdmin)
                    .userId(adminId)
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
            when(dealRepository.findById(existingDealIdForUser))
                    .thenReturn(Optional.of(testDealForUser));
            when(dealMapper.toDealDto(testDealForUser)).thenReturn(testDealForUserDto);
            when(securityUtils.getCurrentUser()).thenReturn(regularUser);

            DealDto result = dealService.findDealById(existingDealIdForUser);

            assertThat(result)
                    .isNotNull()
                    .isEqualTo(testDealForUserDto);

            verify(dealRepository).findById(existingDealIdForUser);
            verify(dealMapper).toDealDto(testDealForUser);
        }

        @Test
        void findDealById_NonExistingId_ThrowsNotFoundException() {
            when(dealRepository.findById(nonExistingDealId))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,() -> dealService.findDealById(nonExistingDealId));
            verify(dealRepository).findById(nonExistingDealId);
            verify(dealMapper, never()).toDealDto(any());
        }

        @Test
        void findDealById_withUser_ThrowsAccessDeniedException() {
            UUID dealId = testDealForAdmin.getId();

            when(dealRepository.findById(dealId))
                    .thenReturn(Optional.of(testDealForAdmin));
            when(dealMapper.toDealDto(testDealForAdmin)).thenReturn(testDealForAdminDto);
            when(securityUtils.getCurrentUser()).thenReturn(regularUser);

            assertThrows(AccessDeniedException.class, () -> dealService.findDealById(dealId));

            verify(dealRepository).findById(dealId);
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
