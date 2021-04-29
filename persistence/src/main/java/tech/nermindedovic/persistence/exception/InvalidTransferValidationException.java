package tech.nermindedovic.persistence.exception;

public class InvalidTransferValidationException extends Exception{
    public InvalidTransferValidationException(String errorMessage) {
        super(errorMessage);
    }
}
