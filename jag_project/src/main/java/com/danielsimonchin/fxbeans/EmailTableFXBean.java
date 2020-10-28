/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.fxbeans;

import java.sql.Timestamp;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Daniel
 */
public class EmailTableFXBean {

    private IntegerProperty emailId;
    private StringProperty fromField;
    private StringProperty subjectField;
    private ObjectProperty dateField;

    /**
     * Constructor which takes inputs for the necessary email field and sets
     * them to their corresponding JavaFX Bean field.
     *
     * @param emailId
     * @param fromField
     * @param subjectField
     * @param dateField
     */
    public EmailTableFXBean(int emailId, String fromField, String subjectField, Timestamp dateField) {
        this.emailId = new SimpleIntegerProperty(emailId);
        this.fromField = new SimpleStringProperty(fromField);
        this.subjectField = new SimpleStringProperty(subjectField);
        this.dateField = new SimpleObjectProperty(dateField);
    }
    
    /**
     * Default constructor which calls the first constructor
     */
    public EmailTableFXBean(){
        this(0,"","",new Timestamp(0));
    }

    public int getEmailId() {
        return emailId.get();
    }

    public void setEmailId(int emailId) {
        this.emailId.set(emailId);
    }
    
    public IntegerProperty getEmailIdProperty(){
        return emailId;
    }

    public String getFromField() {
        return fromField.get();
    }

    public void setFromField(String fromField) {
        this.fromField.set(fromField);
    }
    
    public StringProperty getFromFieldProperty(){
        return this.fromField;
    }

    public String getSubjectField() {
        return subjectField.get();
    }

    public void setSubjectField(String subjectField) {
        this.subjectField.set(subjectField);
    }

    public StringProperty getSubjectFieldProperty(){
        return this.subjectField;
    }
    
    public Object getDateField() {
        return dateField.get();
    }

    public void setDateField(Timestamp dateField) {
        this.dateField.set(dateField);
    }
    
    public ObjectProperty getDateFieldProperty(){
        return this.dateField;
    }
}
