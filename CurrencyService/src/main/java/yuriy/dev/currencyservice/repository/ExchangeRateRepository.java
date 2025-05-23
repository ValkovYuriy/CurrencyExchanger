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

    @Query("SELECT e from ExchangeRate e" +
            " where e.baseCurrency.id = :base " +
            "and e.targetCurrency.id = :target " +
            "and (e.date = :date or e.date = (select max(e2.date) from ExchangeRate e2 " +
            "where e2.baseCurrency.id = :base " +
            "and e2.targetCurrency.id = :target))")
    Optional<ExchangeRate> getExchangeRateForCurrencies(@Param("base") UUID baseCurrencyId, @Param("target") UUID targetCurrencyId, @Param("date") LocalDate date);

    @Query("SELECT er FROM ExchangeRate er where er.baseCurrency.id = :baseCurrencyId and er.targetCurrency.id = :targetCurrencyId")
    Optional<ExchangeRate> findByBaseCurrencyAndTargetCurrency(UUID baseCurrencyId, UUID targetCurrencyId);
}
