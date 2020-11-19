package com.danielsimonchin.fxbeans;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The JavaFX bean for a row in the email table.
 *
 * @author Daniel Simon Chin
 * @version Oct 31, 2020
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
    public EmailTableFXBean(int emailId, String fromField, String subjectField, LocalDateTime dateField) {
        this.emailId = new SimpleIntegerProperty(emailId);
        this.fromField = new SimpleStringProperty(fromField);
        this.subjectField = new SimpleStringProperty(subjectField);
        this.dateField = new SimpleObjectProperty(dateField);
    }

    /**
     * Default constructor which calls the first constructor
     */
    public EmailTableFXBean() {
        this(0, "", "", null);
    }

    /**
     * @return the id of an email
     */
    public int getEmailId() {
        return emailId.get();
    }

    /**
     * @param emailId
     */
    public void setEmailId(int emailId) {
        this.emailId.set(emailId);
    }

    /**
     * @return The EmailIDProperty
     */
    public IntegerProperty getEmailIdProperty() {
        return emailId;
    }

    /**
     * @return The from field
     */
    public String getFromField() {
        return fromField.get();
    }

    /**
     * @param fromField
     */
    public void setFromField(String fromField) {
        this.fromField.set(fromField);
    }

    /**
     * @return the FromFieldProperty
     */
    public StringProperty getFromFieldProperty() {
        return this.fromField;
    }

    /**
     * @return the email's subject
     */
    public String getSubjectField() {
        return subjectField.get();
    }

    /**
     * @param subjectField
     */
    public void setSubjectField(String subjectField) {
        this.subjectField.set(subjectField);
    }

    /**
     * @return the subjectFieldProperty
     */
    public StringProperty getSubjectFieldProperty() {
        return this.subjectField;
    }

    /**
     * @return the date field
     */
    public Object getDateField() {
        return dateField.get();
    }

    /**
     * @param dateField
     */
    public void setDateField(Timestamp dateField) {
        this.dateField.set(dateField);
    }

    /**
     * @return the date field property.
     */
    public ObjectProperty getDateFieldProperty() {
        return this.dateField;
    }
}
