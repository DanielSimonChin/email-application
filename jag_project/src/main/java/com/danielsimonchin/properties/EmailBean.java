/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.properties;

import java.sql.Timestamp;
import jodd.mail.Email;

/**
 * A composition bean class that allows us to have information relating to
 * folder and receivedDate
 *
 * @author Daniel Simon Chin
 * @version Oct 5th, 2020
 */
public class EmailBean {

    private int id;
    private int folderKey;
    private Timestamp receivedDate;
    public Email email;

    /**
     * Default constructor, assign empty values to the fields.
     */
    public EmailBean() {
        this.id = 0;
        this.folderKey = 0;
        this.receivedDate = new Timestamp(0);
        this.email = new Email();
    }

    /**
     * Initialize the fields for the composition bean
     * 
     * @param id
     * @param folderKey
     * @param receivedDate
     * @param email 
     */
    public EmailBean(int id, int folderKey, Timestamp receivedDate, Email email) {
        this.id = id;
        this.folderKey = folderKey;
        this.receivedDate = receivedDate;
        this.email = email;
    }

    /**
     * @return The EmailBean's id
     */
    public int getId() {
        return this.id;
    }

    /**
     * @param id Set the id of the EmailBean
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return The EmailBean's folderKey 
     */
    public int getFolderKey() {
        return this.folderKey;
    }
    /**
     * @param folderKey 
     */
    public void setFolderKey(int folderKey) {
        this.folderKey = folderKey;
    }

    public Timestamp getReceivedDate() {
        return this.receivedDate;
    }

    public void setReceivedDate(Timestamp receivedDate) {
        this.receivedDate = receivedDate;
    }

  

}
