/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.exceptions;

/**
 * Throw this exception when someone tries to create a folder that already exists
 * @author Daniel
 */
public class FolderAlreadyExistsException extends Exception {

    public FolderAlreadyExistsException(String errorMessage) {
        super(errorMessage);
    }
}
