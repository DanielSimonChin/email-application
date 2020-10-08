/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.exceptions;

/**
 * Throw this exception when someone tries to send an email with no recipients
 * @author Daniel
 */
public class NotEnoughRecipientsException extends Exception {
    public NotEnoughRecipientsException(String errorMessage) {
        super(errorMessage);
    }
}
