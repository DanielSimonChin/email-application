/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.exceptions;

/**
 * Throw this exception if the email address we wish to send an email is null
 * @author Daniel
 */
public class RecipientEmailAddressNullException extends Exception {
    public RecipientEmailAddressNullException(String errorMessage) {
        super(errorMessage);
    }
}
