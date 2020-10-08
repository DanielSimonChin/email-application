/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.exceptions;

/**
 * Throw this exception when a user tries to delete a folder that is SENT,INBOX OR DRAFT
 * @author Daniel
 */
public class CannotDeleteFolderException extends Exception{
    public CannotDeleteFolderException(String errorMessage) {
        super(errorMessage);
    }
}
