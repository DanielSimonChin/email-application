/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.exceptions;

/**
 * Throw this exception when the imap url of the recipient we will retrieve from does not equals to imap.gmail.com
 * @author Daniel
 */
public class InvalidRecipientImapURLException extends Exception{
    public InvalidRecipientImapURLException(String errorMessage) {
        super(errorMessage);
    }
}
