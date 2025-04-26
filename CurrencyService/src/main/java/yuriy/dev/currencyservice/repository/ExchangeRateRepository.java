package yuriy.dev.currencyservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yuriy.dev.currencyservice.model.ExchangeRate;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {

    @Query("SELECT e from ExchangeRate e where e.baseCurrency.id = :base and e.targetCurrency.id = :target and e.date = :date")
    Optional<ExchangeRate> getExchangeRateForCurrencies(@Param("base") UUID baseCurrencyId, @Param("target") UUID targetCurrencyId, @Param("date") LocalDate date);
}
