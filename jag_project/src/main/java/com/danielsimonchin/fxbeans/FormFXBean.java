package com.danielsimonchin.fxbeans;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The JavaFX bean for the form in which a user sets up an email to be sent.
 *
 * @author Daniel Simon Chin
 * @version Oct 31, 2020
 */
public class FormFXBean {

    private StringProperty toField;
    private StringProperty ccField;
    private StringProperty bccField;
    private StringProperty subjectField;

    /**
     * Takes the multiple inputs for the email form and sets them in the
     * FormFXBean
     *
     * @param toField
     * @param ccField
     * @param bccField
     * @param subjectField
     */
    public FormFXBean(String toField, String ccField, String bccField, String subjectField) {
        this.toField = new SimpleStringProperty(toField);
        this.ccField = new SimpleStringProperty(ccField);
        this.bccField = new SimpleStringProperty(bccField);
        this.subjectField = new SimpleStringProperty(subjectField);
    }

    /**
     * Default constructor which ensures we don't have any nulls and sets every
     * field as an empty string.
     */
    public FormFXBean() {
        this("", "", "", "");
    }

    /**
     * @return the toField
     */
    public String getToField() {
        return this.toField.get();
    }

    /**
     * @param toField
     */
    public void setToField(String toField) {
        this.toField.set(toField);
    }

    /**
     * @return the StringProperty toField
     */
    public StringProperty getToFieldProperty() {
        return toField;
    }

    /**
     * @return the ccField
     */
    public String getCcField() {
        return this.ccField.get();
    }

    /**
     * @param ccField
     */
    public void setCcField(String ccField) {
        this.ccField.set(ccField);
    }

    /**
     * @return the StringProperty ccField
     */
    public StringProperty getCcFieldProperty() {
        return ccField;
    }

    /**
     * @return the bccField
     */
    public String getBccField() {
        return this.bccField.get();
    }

    /**
     * @param bccField
     */
    public void setBccField(String bccField) {
        this.bccField.set(bccField);
    }

    /**
     * @return the StringProperty bccField
     */
    public StringProperty getBccFieldProperty() {
        return bccField;
    }

    /**
     * @return the subjectField
     */
    public String getSubjectField() {
        return this.subjectField.get();
    }

    /**
     * @param subjectField
     */
    public void setSubjectField(String subjectField) {
        this.subjectField.set(subjectField);
    }

    /**
     * @return the StringProperty subjectField
     */
    public StringProperty getSubjectFieldProperty() {
        return subjectField;
    }

    /**
     * The toString override method which returns all the fields for the
     * FormFXBean
     *
     * @return the toString of the FormFXBean
     */
    @Override
    public String toString() {
        return "FormFXBean{" + "toField=" + toField + ", ccField=" + ccField + ", bccField=" + bccField + ", subjectField=" + subjectField + '}';
    }
}
