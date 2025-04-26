package yuriy.dev.currencyservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yuriy.dev.currencyservice.model.Currency;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, UUID> {

    Optional<Currency> findByCode(String code);
}
