package yuriy.dev.cashbalanceservice.exception;

public class NegativeBalanceException extends RuntimeException {

    public NegativeBalanceException(String message) {
        super(message);
    }
}
