package yuriy.dev.exchangeservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yuriy.dev.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "deal")
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "from_currency_code", nullable = false)
    private String fromCurrencyCode;

    @Column(name = "to_currency_code", nullable = false)
    private String toCurrencyCode;

    @Column(name = "amount_from",nullable = false)
    private BigDecimal amountFrom;

    @Column(name = "amount_to",nullable = false)
    private BigDecimal amountTo;

    @Column(name = "exchange_rate",nullable = false)
    private BigDecimal exchangeRate;

    @Column(name = "timestamp",nullable = false)
    private LocalDateTime timestamp;

}


