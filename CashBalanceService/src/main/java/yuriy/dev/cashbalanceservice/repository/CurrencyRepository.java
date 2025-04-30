package yuriy.dev.cashbalanceservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yuriy.dev.cashbalanceservice.model.Currency;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, UUID> {

    @Query("SELECT c from Currency c where c.code = upper(:code) ")
    Optional<Currency> findByCode(String code);
}
