/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.exceptions;

/**
 *
 * @author Daniel
 */
public class RecipientInvalidFormatException extends Exception {
    public RecipientInvalidFormatException(String errorMessage) {
        super(errorMessage);
    }
}
