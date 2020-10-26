/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.exceptions;

/**
 * Throw this when the list of email recipient addresses is equal to null
 * @author Daniel
 */
public class RecipientListNullException extends Exception{
    public RecipientListNullException(String errorMessage) {
        super(errorMessage);
    }
}
