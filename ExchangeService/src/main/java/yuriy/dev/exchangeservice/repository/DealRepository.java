package yuriy.dev.exchangeservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yuriy.dev.exchangeservice.model.Deal;

import java.util.UUID;

@Repository
public interface DealRepository extends JpaRepository<Deal, UUID> {
}
