/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.exceptions;

/**
 * Throw this in the sendEmail method when checking to see if the mailConfigBean's email address is of invalid email address format.
 * @author Daniel
 */
public class InvalidMailConfigBeanUsernameException extends Exception{
    public InvalidMailConfigBeanUsernameException(String errorMessage) {
        super(errorMessage);
    }
}
