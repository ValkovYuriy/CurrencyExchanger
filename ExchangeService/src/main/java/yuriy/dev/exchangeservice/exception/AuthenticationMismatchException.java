package yuriy.dev.exchangeservice.exception;

public class AuthenticationMismatchException extends RuntimeException{

    public AuthenticationMismatchException(String message) {
        super(message);
    }
}
