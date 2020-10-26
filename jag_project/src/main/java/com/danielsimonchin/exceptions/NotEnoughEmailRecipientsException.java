package com.danielsimonchin.exceptions;

/**
 * Throw this exception when trying to construct an Email object to send, but there is not a minimum of 1 recipient in either the TO,CC OR BCC.
 * @author Daniel
 */
public class NotEnoughEmailRecipientsException extends Exception{
    public NotEnoughEmailRecipientsException(String errorMessage) {
        super(errorMessage);
    }
}
