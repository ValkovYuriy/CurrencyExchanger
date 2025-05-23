package yuriy.dev.cashbalanceservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yuriy.dev.cashbalanceservice.model.CashBalance;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CashBalanceRepository extends JpaRepository<CashBalance, UUID> {

    Optional<CashBalance> findCashBalanceByCurrencyId(UUID currencyId);
}
