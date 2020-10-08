/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.exceptions;

/**
 * Throw this exception when someone is trying to move an email into the DRAFT folder
 * @author Daniel
 */
public class CannotMoveToDraftsException extends Exception {
    public CannotMoveToDraftsException(String errorMessage) {
        super(errorMessage);
    }
}
