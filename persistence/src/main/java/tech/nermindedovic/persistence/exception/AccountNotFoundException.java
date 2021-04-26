package tech.nermindedovic.persistence.exception;

public class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String errorMessage) { super(errorMessage); }
}
