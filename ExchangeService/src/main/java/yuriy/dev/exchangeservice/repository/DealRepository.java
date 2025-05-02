package yuriy.dev.exchangeservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yuriy.dev.exchangeservice.model.Deal;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DealRepository extends JpaRepository<Deal, UUID> {

    @Query(value = "SELECT d.id,d.user_id,d.amount_from,d.amount_to,d.from_currency_code,d.to_currency_code,d.exchange_rate,d.timestamp from deal d where d.timestamp between :fromDate and :toDate offset :from limit :size",nativeQuery = true)
    List<Deal> findDealsBetween(int from,int size,LocalDate fromDate, LocalDate toDate);
}
