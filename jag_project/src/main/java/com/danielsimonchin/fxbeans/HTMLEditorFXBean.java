/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.fxbeans;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The JAVAFX Bean for the HTMLEditor's Html message.
 *
 * @author Daniel Simon Chin
 * @version October 31, 2020
 */
public class HTMLEditorFXBean {

    private StringProperty htmlMessage;

    /**
     * The constructor will take a string message so we can initialize the field
     * with a new SimpleStringProperty object representing the message.
     *
     * @param htmlMessage
     */
    public HTMLEditorFXBean(String htmlMessage) {
        this.htmlMessage = new SimpleStringProperty(htmlMessage);
    }

    /**
     * Default constructor that calls the first constructor with an empty
     * string.
     */
    public HTMLEditorFXBean() {
        this("");
    }
    
    /**
     * @return the html message string
     */
    public String getHtmlMessage() {
        return this.htmlMessage.get();
    }

    /**
     * @param htmlMessage
     */
    public void setHtmlMessage(String htmlMessage) {
        this.htmlMessage.set(htmlMessage);
    }

    /**
     * @return the StringProperty htmlMessage
     */
    public StringProperty getHtmlMessageProperty() {
        return htmlMessage;
    }
}
