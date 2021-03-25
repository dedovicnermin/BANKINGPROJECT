package tech.nermindedovic.persistence.exception;


public class InvalidTransferMessageException extends Exception {

    public InvalidTransferMessageException(String message) {
        super(message);
    }

    public InvalidTransferMessageException(String message, Throwable err) {
        super(message, err);
    }

}
