package yuriy.dev.cashbalanceservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yuriy.dev.cashbalanceservice.dto.CashBalanceDto;
import yuriy.dev.cashbalanceservice.model.CashBalance;
import yuriy.dev.cashbalanceservice.repository.CashBalanceRepository;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashBalanceService {

    private final CashBalanceRepository cashBalanceRepository;


    @Transactional
    public void updateCashBalance(UUID cashBalanceId,CashBalanceDto cashBalanceDto) {
        CashBalance cashBalance = cashBalanceRepository.findById(cashBalanceId).orElse(null);
        if (cashBalance != null) {
            cashBalance.setAmount(cashBalanceDto.amount());
            cashBalance.setDate(LocalDate.now());
            CashBalance savedBalance = cashBalanceRepository.save(cashBalance);
            log.info("Обновлен баланс: {}", savedBalance);
        }
    }

}
